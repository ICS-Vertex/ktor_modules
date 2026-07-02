package nl.icsvertex.gradle.server.modules.tasks

import nl.icsvertex.gradle.server.modules.config.IcsModuleConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * A Gradle task that copies dependencies from the runtime classpath to a specified location.
 *
 * This task is intended to be used within a Gradle project that utilizes the Ktor framework.
 * It is registered as a custom task named "copyDependencies" and belongs to the "ktor Modules" group.
 *
 * @see CopyDependencies.copyDependencies
 * @see CopyDependencies.registerCopyDependenciesTask
 */
abstract class CopyDependencies : DefaultTask() {

    @get:OutputDirectory
    abstract val buildLocation: Property<File>

    @get:Input
    abstract val includeDependencies: Property<Boolean>

    /**
     * The main action of the task.
     *
     * This function retrieves the [IcsModuleConfig] extension from the project and checks if the
     * `includeDependencies` flag is set to true. If it is, it copies all dependencies from the
     * "runtimeClasspath" configuration to a specified location within the project's build directory.
     *
     * @see IcsModuleConfig
     */
    @TaskAction
    fun copyDependencies() {
        if(includeDependencies.get()) {
            project.copy {
                it.exclude { it.path.contains("-sources") }
                it.from(project.configurations.getByName("runtimeClasspath"))
                it.into(buildLocation.get())
            }
        }
    }

    /**
     * A companion object that contains a function to register the "copyDependencies" task.
     *
     * This function registers the custom task with the provided project, sets its group to "ktor Modules",
     * and associates it with the [CopyDependencies] class.
     *
     * @param project The Gradle project to which the task will be registered.
     * @see CopyDependencies
     */
    companion object {
        fun registerCopyDependenciesTask(project: Project, config: IcsModuleConfig) {
            val compileTask = project.tasks.withType(CompileModules::class.java)
            project.tasks.register("copyDependencies", CopyDependencies::class.java){
                it.outputs.upToDateWhen { false }
                it.group = "ktor Modules"
                it.buildLocation.set(File(config.buildLocation, "modules/dependencies"))
                it.includeDependencies.set(config.includeDependencies)

                it.mustRunAfter(compileTask)
            }
        }
    }
}