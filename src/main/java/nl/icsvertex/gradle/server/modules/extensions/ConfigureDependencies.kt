package nl.icsvertex.gradle.server.modules.extensions

import nl.icsvertex.gradle.server.modules.KtorModules
import org.gradle.api.Project

/**
 * Adds the necessary dependencies to the given [project] for the KtorModules.
 *
 * This function takes a [Project] instance as a parameter and adds a list of base dependencies
 * to the project's implementation configuration. The base dependencies are defined in the
 * [baseDependencies] list.
 *
 * @param project The Gradle project to which the dependencies will be added.
 *
 * @return Nothing. The function modifies the given [project] directly.
 */
fun KtorModules.addDependencies(project: Project) {
    val baseDependencies = listOf(
        "nl.icsvertex.server:modules:1.0.0.3"
    )

    baseDependencies.forEach { dependency ->
        project.dependencies.add("implementation", dependency)
    }
}