package com.ott.platform.web

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.ott.platform.platform.web.webPlayerModule
import com.ott.platform.shared.OttApp
import com.ott.platform.shared.initKoin
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(extraModules = listOf(webPlayerModule))
    val container = document.getElementById("ott-compose-root") ?: document.body ?: return
    ComposeViewport(container) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 840.dp
            OttApp(isTv = isWideScreen)
        }
    }
}
