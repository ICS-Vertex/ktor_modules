package nl.icsvertex.gradle.server.modules.extensions

import nl.icsvertex.gradle.server.modules.IcsModules
import org.gradle.api.Project


/**
 * Adds the base plugins to the given [project].
 *
 * This function is intended to be used within the context of a [IcsModules] instance.
 * It applies a list of base plugins to the provided [project] using the Gradle plugin manager.
 *
 * @param project The Gradle project to which the base plugins will be applied.
 *
 * @return Nothing is returned by this function.
 */
fun addModulesPlugins(project: Project) {
    val basePlugins = listOf(
        "org.jetbrains.kotlin.jvm",
        "com.google.devtools.ksp"
    )

    basePlugins.forEach { plugin ->
        project.pluginManager.apply(plugin)
    }
}