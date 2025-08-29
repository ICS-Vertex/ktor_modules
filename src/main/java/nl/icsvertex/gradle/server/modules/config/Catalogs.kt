package nl.icsvertex.gradle.server.modules.config

import nl.icsvertex.gradle.server.modules.config.models.Catalog
import org.gradle.api.tasks.Input

interface Catalogs {
    var catalogList: List<Catalog>

    fun add(catalog: Catalog) {
        catalogList = catalogList + catalog
    }

    fun add(vararg catalogs: Catalog) {
        catalogList = catalogList + catalogs.toList()
    }

    fun add(
        name: String,
        catalog: String,
        version: String
    ) {
        add(Catalog(name, catalog, version))
    }
}