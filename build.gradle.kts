plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
    kotlin("jvm") version "2.2.20"
}

group = "nl.icsvertex.ktor"
version = "0.3.2"

val user: String = System.getenv("GITHUB_USER")
val key: String = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")

gradlePlugin {
    website = "https://github.com/MikeDirven/ktor_modules"
    vcsUrl = "https://github.com/MikeDirven/ktor_modules"

    plugins {
        create("ics-modules") {
            id = "nl.icsvertex.ktor.modules"
            displayName = "Gradle plugin for ktor modules system"
            description = "Gradle plugin to help out building the ktor modules, that have been build with the ktor modules implementation"
            tags = listOf("ktor", "modules", "jetbrains", "plugins")
            implementationClass = "nl.icsvertex.gradle.server.modules.IcsModules"
        }

        create("ics-server") {
            id = "nl.icsvertex.ktor.server"
            displayName = "Gradle plugin for ktor server system"
            description = "Gradle plugin to help out building the ktor server, that have been build with the ktor server implementation"
            tags = listOf("ktor", "server", "jetbrains", "plugins")
            implementationClass = "nl.icsvertex.gradle.server.IcsServer"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "ICSGithub"
            url = uri("https://maven.pkg.github.com/ICS-Vertex/ktor_modules") // Github Package
            credentials {
                //Fetch these details from the properties file or from Environment variables
                username = user
                password = key
            }
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.pkg.github.com/ics-vertex/*") {
        credentials {
            username = System.getenv("GITHUB_USER")
            password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor.plugin:plugin:3.3.0")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.2.20-2.0.3")
}

kotlin {
    jvmToolchain(21)
}