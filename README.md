# Ktor Modules Gradle Plugins

This repository provides a powerful, modular Gradle plugin system for building extensible [Ktor](https://ktor.io/) applications. It is split into two primary Gradle plugins and a KSP (Kotlin Symbol Processing) processor that automatically wires your controllers, services, and schedules into a unified Ktor module.

## Features

*   **Modular Architecture:** Separate your Ktor application into a core server project and multiple independent module projects.
*   **Automatic Code Generation:** Use KSP to automatically generate module wiring code. No more manual `routing { }` boilerplate for every new controller!
*   **Automated Packaging:** Automatically packages modules into isolated `.jar` files and copies their runtime dependencies into a central `modules` and `modules/dependencies` folder for the server to load at runtime.
*   **Release Automation:** Generates compressed `.zip` releases containing your server, modules, and a `release_info.txt` file with Git commit and environment details.

---

## 1. Setup the Server Project

The Server project acts as the host for your Ktor application. It will automatically load the compiled `.jar` modules at runtime.

### `build.gradle.kts`

Apply the `ics-server` plugin in your server project's build file:

```kotlin
plugins {
    id("nl.icsvertex.ktor.server") version "0.3.2"
}

icsServer {
    // Defines where all compiled modules and dependencies will be placed
    buildLocation = layout.buildDirectory.dir("server-runtime").get().asFile
    
    // (Optional) Include project sources in the release
    includeSources = true
}
```

When you run `./gradlew create_release` on the server project, it will automatically find all subprojects applying the `ics-modules` plugin, compile them, gather their dependencies, and bundle everything into a release zip!

---

## 2. Setup a Module Project

Module projects contain your actual route definitions (Controllers), business logic (Services), and cron jobs (Schedules).

### `build.gradle.kts`

Apply the `ics-modules` plugin and the KSP plugin in your module's build file:

```kotlin
plugins {
    id("nl.icsvertex.ktor.modules") version "0.3.2"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" // Match your Kotlin version
}

dependencies {
    // Add the KSP processor
    ksp("nl.icsvertex.ktor:processor:1.0.2")
    
    // Add your module dependencies
    // implementation(...)
}

icsModule {
    // (Optional) Define the main class if you are not using KSP annotations
    // mainClass = "com.example.module.MainKt"
}
```

*Note: The `ics-modules` plugin automatically detects if the server project has defined a `buildLocation` and will route all compiled module `.jar` files and dependencies to the server's build location automatically.*

---

## 3. Using Annotations (KSP)

The `nl.icsvertex.ktor:processor` provides annotations that drastically simplify creating Ktor modules.

### `@KtorModule`

Marks the entry point function for your Ktor module. KSP will scan your project for this function and generate a `KM_YourFunctionName` file that aggregates all controllers, services, and schedules.

```kotlin
package com.example.article

import io.ktor.server.application.*
import nl.icsvertex.server.modules.annotations.KtorModule

@KtorModule
fun Application.articleModule() {
    // Standard Ktor application configuration (e.g., installing plugins)
    install(ContentNegotiation) { ... }
}
```

### `@KtorController`

Marks a class as a route controller. KSP automatically registers this controller within the generated `ktorModule` block.

```kotlin
package com.example.article.controllers

import nl.icsvertex.server.controllers.annotations.KtorController

@KtorController(path = "/api/v1/articles")
class ArticleController {
    // Your routing logic
}
```

### `@KtorService` and `@KtorSchedule`

Similarly, use `@KtorService` and `@KtorSchedule` to mark business logic services and background tasks.

```kotlin
package com.example.article.services

import nl.icsvertex.server.services.annotations.KtorService
import nl.icsvertex.server.schedules.annotations.KtorSchedule

@KtorService
class ArticleService {
    // Service logic
}

@KtorSchedule
class ArticleCleanupSchedule {
    // Cron logic
}
```

### Generated Code

When you compile the module, KSP will automatically generate a file containing a function like this:

```kotlin
fun KM_articleModule(): KtorModule = ktorModule {
    articleModule()
    
    controllers {
        this path "/api/v1/articles" init com.example.article.controllers.ArticleController()
    }
    
    services {
        this service com.example.article.services.ArticleService()
    }
    
    schedules {
        this schedule com.example.article.services.ArticleCleanupSchedule()
    }
}
```

This generated function is completely fully-qualified, preventing any `Unresolved reference` errors, and gracefully handles multiple KSP compilation rounds.

---