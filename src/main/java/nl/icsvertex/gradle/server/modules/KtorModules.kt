package nl.icsvertex.gradle.server.modules

import nl.icsvertex.gradle.server.modules.config.KtorModuleConfig
import nl.icsvertex.gradle.server.modules.extensions.addDependencies
import nl.icsvertex.gradle.server.modules.extensions.addPlugins
import nl.icsvertex.gradle.server.modules.extensions.addRepositories
import nl.icsvertex.gradle.server.modules.tasks.CompileModules
import nl.icsvertex.gradle.server.modules.tasks.CopyDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar


/**
 * A Gradle plugin that provides a modular structure for a Ktor application.
 * This plugin sets up necessary configurations, tasks, and dependencies for a Ktor application.
 *
 * @author Mike Dirven
 */
abstract class KtorModules : Plugin<Project> {

    /**
     * Applies the KtorModules plugin to the given project.
     *
     * @param project The Gradle project to apply the plugin to.
     */
    override fun apply(project: Project) {
        // Create a configuration object for the plugin
        val config = project.extensions.create("ktorModule", KtorModuleConfig::class.java)

        // Add needed gradle plugins
        addPlugins(project)
        addRepositories(project)
        addDependencies(project)

        // Configure compile tasks
        CopyDependencies.Companion.registerCopyDependenciesTask(project)
        CompileModules.Companion.registerCompileModulesTask(project)

        // Setup jar task
        project.tasks.withType(Jar::class.java) { task ->
            task.outputs.upToDateWhen { false }
            task.doFirst {
                // Set the destination directory for the jar file
                task.destinationDirectory.set(config.buildLocation)

                // Configure the manifest of the jar file
                task.manifest { manifest ->
                    // Set the main class and version attributes in the manifest
                    manifest.attributes["Main-Class"] = config.mainClass
                    manifest.attributes["Version"] = project.version
                }
            }
        }
    }
}