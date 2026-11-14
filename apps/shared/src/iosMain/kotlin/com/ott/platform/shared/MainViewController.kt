package com.ott.platform.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.ott.platform.platform.ios.iosPlayerModule
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        runCatching {
            initKoin(extraModules = listOf(iosPlayerModule))
        }
        koinStarted = true
    }
    return ComposeUIViewController {
        OttApp(isTv = false)
    }
}
