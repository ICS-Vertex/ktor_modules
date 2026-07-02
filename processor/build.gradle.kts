plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "nl.icsvertex.ktor"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/ics-vertex/*") {
        credentials {
            username = System.getenv("GITHUB_USER")
            password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
        }
    }
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.25")
    implementation(server.ics.modules)
    implementation(server.ics.controllers)
    implementation(server.ics.services)
    implementation(server.ics.schedules)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "ICSGithub"
            url = uri("https://maven.pkg.github.com/ICS-Vertex/ktor_modules")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
            }
        }
    }
}
