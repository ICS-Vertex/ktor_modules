package nl.icsvertex.gradle.server

import nl.icsvertex.gradle.server.config.IcsServerConfig
import nl.icsvertex.gradle.server.extensions.addDependencies
import nl.icsvertex.gradle.server.extensions.addPlugins
import nl.icsvertex.gradle.server.extensions.addRepositories
import nl.icsvertex.gradle.server.tasks.registerCreateReleaseTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.jvm.tasks.Jar

/**
 * A Gradle plugin that provides a modular structure for a Ktor application.
 * This plugin sets up necessary configurations, tasks, and dependencies for a Ktor application.
 *
 * @author Mike Dirven
 */
abstract class IcsServer : Plugin<Project> {

    /**
     * Applies the KtorModules plugin to the given project.
     *
     * @param project The Gradle project to apply the plugin to.
     */
    override fun apply(project: Project) {
        // Create a configuration object for the plugin
        val config = project.extensions.create("icsServer", IcsServerConfig::class.java)

        // Add needed gradle plugins
        addPlugins(project)
        addRepositories(project)
        addDependencies(project)
//        configureVersionCatalog(project, config)

        // Configure compile tasks
        registerCreateReleaseTask(project, config)

        project.pluginManager.withPlugin("application") {
            project.extensions.configure(JavaApplication::class.java) { javaApplication ->
                javaApplication.mainClass.set(config.mainClass)
            }
        }

        // Setup jar task
        project.afterEvaluate {
            project.tasks.withType(Jar::class.java).configureEach { task ->
                if (task.name == "shadowJar" || task.name == "buildFatJar" || task.name == "fatJar") {
                    task.archiveFileName.set("server.jar")
                    task.destinationDirectory.set(java.io.File(config.buildLocation, "server"))
                }
            }
        }
    }
}