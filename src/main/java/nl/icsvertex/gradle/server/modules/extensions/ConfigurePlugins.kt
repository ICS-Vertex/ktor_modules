package nl.icsvertex.gradle.server.modules.extensions

import nl.icsvertex.gradle.server.modules.KtorModules
import org.gradle.api.Project


/**
 * Adds the base plugins to the given [project].
 *
 * This function is intended to be used within the context of a [KtorModules] instance.
 * It applies a list of base plugins to the provided [project] using the Gradle plugin manager.
 *
 * @param project The Gradle project to which the base plugins will be applied.
 *
 * @return Nothing is returned by this function.
 */
fun KtorModules.addPlugins(project: Project) {
    val basePlugins = listOf(
        "org.jetbrains.kotlin.jvm"
    )

    basePlugins.forEach { plugin ->
        project.pluginManager.apply(plugin)
    }
}