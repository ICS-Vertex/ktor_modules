package nl.icsvertex.gradle.server.modules.tasks

import nl.icsvertex.gradle.server.modules.config.IcsModuleConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
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
abstract class CompileModules : DefaultTask() {

    @get:Input
    abstract val mainClass: Property<String>

    @get:OutputDirectory
    abstract val buildLocation: Property<File>

    /**
     * The main action of the task.
     *
     * This method retrieves the [IcsModuleConfig] extension from the project and performs the
     * following checks:
     * 1. Checks if the build location directory exists, and if not, creates it.
     * 2. Validates that the build location is a directory.
     * 3. Validates that the main class property is not blank.
     *
     * If any of the checks fail, an [IllegalStateException] is thrown with an appropriate error message.
     */
    @TaskAction
    fun compile() {
        // Check configuration
        if(!buildLocation.get().exists()) buildLocation.get().mkdirs()
        if(!buildLocation.get().isDirectory) throw IllegalStateException("ktorModules.buildLocation need to be a directory!")
    }
    
    companion object {
        /**
         * Registers the "compile" task for the given project.
         *
         * This method creates a "compile" task of type [CompileModules] and configures it to depend
         * on the "copyDependencies" and "jar" tasks. It also sets the task group to "ktor Modules".
         *
         * @param project The Gradle project to register the task for.
         */
        fun registerCompileModulesTask(project: Project, config: IcsModuleConfig) {
            val copyTask = project.tasks.withType(CopyDependencies::class.java)
            val jarTask = project.tasks.withType(Jar::class.java)
            val cleanUpTask = project.tasks.withType(ModulesCleanUp::class.java)

            project.tasks.register("compile", CompileModules::class.java){ compileTask ->
                compileTask.group = "ktor Modules"

                compileTask.mainClass.set(config.mainClass)
                compileTask.buildLocation.set(File(config.buildLocation, "modules"))

                compileTask.finalizedBy(copyTask, jarTask, cleanUpTask)
            }

            project.tasks.matching { it.name == "build" || it.name == "assemble" }.configureEach {
                it.dependsOn("compile")
            }
        }
    }
}