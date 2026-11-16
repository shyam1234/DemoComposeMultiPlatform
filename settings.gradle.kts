rootProject.name = "OttPlatform"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":apps:androidApp")
include(":apps:androidTvApp")
include(":apps:desktopApp")
include(":apps:shared")

include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:network")
include(":core:designsystem")
include(":core:navigation")
include(":core:logging")
include(":core:testing")
include(":core:player-api")
include(":core:di")
include(":core:ui")

// Unique Gradle project names (avoid colliding ":…:impl" module names)
listOf("home", "search", "details", "player", "watchlist").forEach { feature ->
    include(":features:$feature-api")
    include(":features:$feature-impl")
    project(":features:$feature-api").projectDir = file("features/$feature/api")
    project(":features:$feature-impl").projectDir = file("features/$feature/impl")
}

include(":platform:android")
include(":platform:ios")
