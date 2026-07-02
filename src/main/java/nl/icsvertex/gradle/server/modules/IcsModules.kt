package nl.icsvertex.gradle.server.modules

import nl.icsvertex.gradle.server.modules.config.IcsModuleConfig
import nl.icsvertex.gradle.server.extensions.addDependencies
import nl.icsvertex.gradle.server.extensions.addPlugins
import nl.icsvertex.gradle.server.extensions.addRepositories
import nl.icsvertex.gradle.server.modules.extensions.addModulesPlugins
import nl.icsvertex.gradle.server.modules.tasks.CompileModules
import nl.icsvertex.gradle.server.modules.tasks.CopyDependencies
import nl.icsvertex.gradle.server.modules.tasks.ModulesCleanUp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import java.io.File


/**
 * A Gradle plugin that provides a modular structure for a Ktor application.
 * This plugin sets up necessary configurations, tasks, and dependencies for a Ktor application.
 *
 * @author Mike Dirven
 */
abstract class IcsModules : Plugin<Project> {

    /**
     * Applies the KtorModules plugin to the given project.
     *
     * @param project The Gradle project to apply the plugin to.
     */
    override fun apply(project: Project) {
        // Create a configuration object for the plugin
        val config = project.extensions.create("icsModule", IcsModuleConfig::class.java)

        // Add needed gradle plugins
        addModulesPlugins(project)
        addRepositories(project)
        addDependencies(project)
//        configureVersionCatalog(project, config)

        // Setup ksp dependency
        project.pluginManager.withPlugin("com.google.devtools.ksp") {
            project.dependencies.add("ksp", "nl.icsvertex.ktor:processor:0.2.0")
        }

        // Configure compile tasks
        CopyDependencies.registerCopyDependenciesTask(project, config)
        CompileModules.registerCompileModulesTask(project, config)
        ModulesCleanUp.registerModulesCleanUpTask(project, config)

        // Enable sources jar
        project.pluginManager.withPlugin("java") {
            project.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) { java ->
                java.withSourcesJar()
            }
        }

        // Setup task destinations using effective build location from Server config (if present)
        project.gradle.projectsEvaluated {
            val serverProject = project.rootProject.allprojects.find { it.plugins.hasPlugin("nl.icsvertex.ktor.server") }
            val serverConfig = serverProject?.extensions?.findByType(nl.icsvertex.gradle.server.config.IcsServerConfig::class.java)
            val effectiveLocation = serverConfig?.buildLocation ?: config.buildLocation

            project.tasks.withType(CompileModules::class.java).configureEach { it.buildLocation.set(File(effectiveLocation, "modules")) }
            project.tasks.withType(CopyDependencies::class.java).configureEach { it.buildLocation.set(File(effectiveLocation, "modules/dependencies")) }
            project.tasks.withType(ModulesCleanUp::class.java).configureEach { it.buildLocation.set(File(effectiveLocation, "modules")) }

            project.tasks.withType(Jar::class.java).configureEach { task ->
                task.outputs.upToDateWhen { false }
                
                if (task.name == "sourcesJar") {
                    task.destinationDirectory.set(File(effectiveLocation, "sources"))
                } else if (!task.name.contains("sources", ignoreCase = true)) {
                    // Set the destination directory for the jar file
                    task.destinationDirectory.set(File(effectiveLocation, "modules"))

                    // Configure the manifest of the jar file
                    task.manifest { manifest ->
                        // Set the main class and version attributes in the manifest
                        if (config.mainClass.isNotBlank()) {
                            manifest.attributes["Main-Class"] = config.mainClass
                        }
                        manifest.attributes["Version"] = project.version
                        manifest.attributes["Module-Name"] = project.name
                    }
                }
            }
        }
    }
}


