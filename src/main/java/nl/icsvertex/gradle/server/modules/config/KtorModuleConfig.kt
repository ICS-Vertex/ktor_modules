package nl.icsvertex.gradle.server.modules.config

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import java.io.File


/**
 * This class represents the configuration for a Ktor module. It is used to define various settings
 * for building and packaging a Ktor module.
 *
 * @property mainClass The fully qualified name of the main class for the Ktor module.
 * @property buildLocation The directory where the built module artifacts will be located.
 * @property includeDependencies A flag indicating whether the module dependencies should be included
 * in the final package.
 */
open class KtorModuleConfig {
    @Input
    var mainClass: String = ""

    @InputDirectory
    var buildLocation: File = File("build/module")

    @Input
    var includeDependencies: Boolean = true
}