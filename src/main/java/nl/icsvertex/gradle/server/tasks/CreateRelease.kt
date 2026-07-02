package nl.icsvertex.gradle.server.tasks

import nl.icsvertex.gradle.server.config.IcsServerConfig
import nl.icsvertex.gradle.server.modules.tasks.CompileModules
import nl.icsvertex.gradle.server.modules.tasks.CopyDependencies
import nl.icsvertex.gradle.server.modules.tasks.ModulesCleanUp
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import java.io.File

fun registerCreateReleaseTask(project: Project, config: IcsServerConfig) {
    project.gradle.projectsEvaluated {
            val compileTasks = project.rootProject.subprojects.flatMap { it.tasks.withType(CompileModules::class.java) }
            val cleanUpTasks = project.rootProject.subprojects.flatMap { it.tasks.withType(ModulesCleanUp::class.java) }
            val copyDependenciesTasks = project.rootProject.subprojects.flatMap { it.tasks.withType(CopyDependencies::class.java) }
            val jarTasks = project.rootProject.subprojects.flatMap { it.tasks.withType(org.gradle.jvm.tasks.Jar::class.java).matching { t -> t.name == "jar" || t.name == "sourcesJar" } }
            val fatJarTasks = project.tasks.matching { it.name == "shadowJar" || it.name == "buildFatJar" || it.name == "fatJar" }

            project.tasks.register("create_release", Zip::class.java) { zipTask ->
                zipTask.group = "ics"
                zipTask.description = "Packages the server.jar and modules into a zip file"

                println(
                    """
                        child tasks -> ${compileTasks.size}
                    """.trimIndent()
                )

                zipTask.dependsOn(compileTasks)
                zipTask.dependsOn(cleanUpTasks)
                zipTask.dependsOn(copyDependenciesTasks)
                zipTask.dependsOn(jarTasks)
                zipTask.dependsOn(fatJarTasks)

                // Ensure this runs only after your cleanup task has finished preparing the files
                zipTask.shouldRunAfter(compileTasks)
                zipTask.shouldRunAfter(cleanUpTasks)
                zipTask.shouldRunAfter(copyDependenciesTasks)
                zipTask.shouldRunAfter(jarTasks)
                zipTask.shouldRunAfter(fatJarTasks)

            // Configure the output file
            zipTask.archiveFileName.set("${project.rootProject.name}-${project.version}.zip")
            zipTask.destinationDirectory.set(config.buildLocation)

            zipTask.doLast {
                val releaseInfoFile = File(config.buildLocation, "release.txt")

                val gitCommit = try {
                    java.lang.Runtime.getRuntime().exec("git rev-parse --short HEAD").inputStream.bufferedReader().readText().trim()
                } catch (e: Exception) {
                    "Unknown"
                }

                val gitBranch = try {
                    java.lang.Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD").inputStream.bufferedReader().readText().trim()
                } catch (e: Exception) {
                    "Unknown"
                }

                val includedModules = project.rootProject.subprojects.joinToString("\n                    - ") { it.name }

                releaseInfoFile.writeText(
                    """
                    =========================================
                    Release Information
                    =========================================
                    Project Name: ${project.rootProject.name}
                    Group: ${project.group}
                    Build Version: ${project.version}
                    Build Date: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}
                    
                    Environment
                    -----------------------------------------
                    Java Version    : ${System.getProperty("java.version")}
                    Gradle Version  : ${project.gradle.gradleVersion}
                    OS              : ${System.getProperty("os.name")} (${System.getProperty("os.arch")})
                    
                    Git Info
                    -----------------------------------------
                    Branch          : $gitBranch
                    Commit          : $gitCommit
                    
                    Server Details
                    -----------------------------------------
                    Main Class      : ${config.mainClass.ifBlank { "Not Configured (Using KSP)" }}
                    Target File     : ${zipTask.archiveFileName.get()}
                    
                    Included Modules
                    -----------------------------------------
                    - $includedModules
                    =========================================
                    """.trimIndent()
                )
            }

            val modulesDir = File(config.buildLocation, "modules")
            val serverDir = File(config.buildLocation, "server")

            // 1. Take server.jar and put it at the ROOT of the zip
            zipTask.from(serverDir) {
                it.include("server.jar")
            }

            // 2. Take the rest of the 'modules' folder and put it inside a 'modules' folder in the zip
            zipTask.from(modulesDir) {
                // We exclude 'server' here because we already added server.jar above
                it.into("modules")
            }
        }
    }
}