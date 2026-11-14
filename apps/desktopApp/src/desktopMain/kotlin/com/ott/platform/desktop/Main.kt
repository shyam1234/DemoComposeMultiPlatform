package com.ott.platform.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ott.platform.shared.initKoin
import com.ott.platform.shared.OttApp

fun main() {
    initKoin()
    application {
        Window(onCloseRequest = ::exitApplication, title = "OttPlatform") {
            OttApp(isTv = false)
        }
    }
}
