package nl.icsvertex.gradle.server.config

import nl.icsvertex.gradle.server.modules.config.Catalogs
import nl.icsvertex.gradle.server.modules.config.models.Catalog
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import java.io.File

open class IcsServerConfig : Catalogs {
    @Input
    var mainClass: String = ""

    @InputDirectory
    var buildLocation: File = File("compiled")

    @Input
    override var catalogList: List<Catalog> = listOf()

    fun catalogs(block: Catalogs.() -> Unit) {
        block(this)
    }
}

