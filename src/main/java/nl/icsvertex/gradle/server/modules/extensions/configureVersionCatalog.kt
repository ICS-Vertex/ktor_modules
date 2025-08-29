package nl.icsvertex.gradle.server.modules.extensions

import nl.icsvertex.gradle.server.modules.KtorModules
import nl.icsvertex.gradle.server.modules.config.KtorModuleConfig
import org.gradle.api.Project
import org.gradle.api.initialization.resolve.DependencyResolutionManagement
import java.net.URI

fun KtorModules.configureVersionCatalog(project: Project, config: KtorModuleConfig) {
    project.rootProject.extensions.configure(DependencyResolutionManagement::class.java) {
        it.repositories { repos ->
            repos.maven { maven ->
                maven.url = URI("https://maven.pkg.github.com/ICS-Vertex/*")
                maven.credentials { cred ->
                    cred.username = System.getenv("GITHUB_USER")
                    cred.password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
                }
            }
        }
        
        it.versionCatalogs { catalogs ->
            config.catalogList.forEach { catalog ->
                catalogs.create(catalog.name) { newCatalog ->
                    newCatalog.from("${catalog.catalog}:${catalog.version}")
                }
            }
        }
    }
}