dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.github.com/ics-vertex/*") {
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
            }
        }
    }

    versionCatalogs {
        create("server") {
            from("nl.icsvertex.server:catalog:1.0.0.65")
        }
    }
}

rootProject.name = "ktor_modules"
include("processor")