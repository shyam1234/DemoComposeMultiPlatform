plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(projects.core.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.di)
            implementation(projects.features.homeImpl)
            implementation(projects.features.searchImpl)
            implementation(projects.features.detailsImpl)
            implementation(projects.features.playerImpl)
            implementation(projects.features.watchlistImpl)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        val desktopMain by getting {

            dependencies { implementation(compose.desktop.currentOs) }

        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.ott.platform.shared.generated.resources"
    generateResClass = always
}

android {
    namespace = "com.ott.platform.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
