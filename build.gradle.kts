plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
    kotlin("jvm") version "2.0.20"
}

group = "nl.icsvertex.ktor"
version = "0.1.8"

gradlePlugin {
    website = "https://github.com/MikeDirven/ktor_modules"
    vcsUrl = "https://github.com/MikeDirven/ktor_modules"

    plugins {
        create("ktor-modules") {
            id = "nl.icsvertex.ktor.modules"
            displayName = "Gradle plugin for ktor modules system"
            description = "Gradle plugin to help out building the ktor modules, that have been build with the ktor modules implementation"
            tags = listOf("ktor", "modules", "jetbrains", "plugins")
            implementationClass = "nl.icsvertex.gradle.server.modules.KtorModules"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}