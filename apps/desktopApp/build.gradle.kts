import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(projects.apps.shared)
                implementation(projects.core.di)
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.ott.platform.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OttPlatform"
            packageVersion = "1.0.0"
        }
    }
}
