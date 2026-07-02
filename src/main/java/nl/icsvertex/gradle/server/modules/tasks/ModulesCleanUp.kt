package nl.icsvertex.gradle.server.modules.tasks

import nl.icsvertex.gradle.server.modules.config.IcsModuleConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.gradle.jvm.tasks.Jar
import java.io.File

/**
 * A Gradle task that compiles Ktor modules.
 *
 * This task extends the DefaultTask class and is responsible for compiling the Ktor modules.
 * It checks the configuration provided by the [IcsModuleConfig] extension and performs necessary
 * actions such as creating the build location directory and validating the main class property.
 *
 * @see IcsModuleConfig
 */
abstract class ModulesCleanUp : DefaultTask() {
    @get:OutputDirectory
    abstract val buildLocation: Property<File>

    @get:Input
    abstract val includeSources: Property<Boolean>

    @TaskAction
    fun cleanup() {
        if(includeSources.get()) {
            val sourceDir = buildLocation.get()
            val pattern = { file: FileTreeElement -> file.name.endsWith("-sources.jar") }

            // 1. Copy the files to the new location
            project.copy {
                it.include(pattern)
                it.from(sourceDir)
                it.into(File(sourceDir.parent, "sources"))
            }

            // 2. Delete the original files from the old location
            // We use a fileTree to find the exact same files we just copied
            project.delete(project.fileTree(sourceDir).matching {
                it.include(pattern)
            })
        } else {
            val sourceFiles = buildLocation.get().listFiles { file -> file.name.endsWith("-sources.jar") }
            project.delete(*sourceFiles)
        }
    }
    
    companion object {
        fun registerModulesCleanUpTask(project: Project, config: IcsModuleConfig) {
            val jarTask = project.tasks.withType(Jar::class.java)

            project.tasks.register("modulesCleanup", ModulesCleanUp::class.java){
                it.outputs.upToDateWhen { false }
                it.group = "ics Modules"
                it.includeSources.set(config.includeSources)
                it.buildLocation.set(File(config.buildLocation, "modules"))

                it.mustRunAfter(jarTask)
            }
        }
    }
}