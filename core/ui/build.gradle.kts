plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget()

        iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)
            }
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
        val desktopMain by getting {
            dependencies { implementation(compose.desktop.currentOs) }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)
            }
        }
    }
}

android {
    namespace = "com.ott.platform.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
