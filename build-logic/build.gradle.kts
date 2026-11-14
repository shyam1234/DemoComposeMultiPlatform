plugins {
    `kotlin-dsl`
}

group = "com.ott.platform.buildlogic"

dependencies {
    compileOnly(libs.plugins.android.library.get().toString().let {
        // Resolve via classpath plugins below
        null
    })
}

dependencies {
    implementation(libs.plugins.kotlinMultiplatform.get().toMarker())
    implementation(libs.plugins.androidLibrary.get().toMarker())
    implementation(libs.plugins.androidApplication.get().toMarker())
    implementation(libs.plugins.composeMultiplatform.get().toMarker())
    implementation(libs.plugins.composeCompiler.get().toMarker())
    implementation(libs.plugins.kotlinSerialization.get().toMarker())
}

fun Provider<PluginDependency>.toMarker(): String {
    val plugin = get()
    return "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}"
}
