package nl.icsvertex.gradle.server.modules.extensions

import nl.icsvertex.gradle.server.modules.KtorModules
import org.gradle.api.Project
import java.net.URI

/**
 * Adds necessary repositories to the given [Project] for using Ktor Modules.
 *
 * This function configures the provided [Project] to include the following repositories:
 * - Maven Local: For local Maven artifacts.
 * - Maven Central: For Maven artifacts hosted on Maven Central.
 * - Google: For Google's Maven repository.
 * - Gradle Plugin Portal: For Gradle plugin artifacts.
 * - Custom GitHub repository: For ICS-Vertex's Ktor Modules artifacts.
 *
 * The custom GitHub repository is configured with credentials obtained from environment variables:
 * - GITHUB_USER: The username for authentication.
 * - GITHUB_KEY: The personal access token for authentication. If not provided, GITHUB_PASS will be used.
 *
 * @param project The [Project] to which the repositories will be added.
 */
fun KtorModules.addRepositories(project: Project) {
    project.repositories.mavenLocal()
    project.repositories.mavenCentral()
    project.repositories.google()
    project.repositories.gradlePluginPortal()

    project.repositories.maven { maven ->
        maven.url = URI("https://maven.pkg.github.com/ICS-Vertex/*")
        maven.credentials { cred ->
            cred.username = System.getenv("GITHUB_USER")
            cred.password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
        }
    }
}