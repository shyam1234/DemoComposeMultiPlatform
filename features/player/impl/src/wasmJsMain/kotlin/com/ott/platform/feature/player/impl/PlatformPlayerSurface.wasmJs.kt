package com.ott.platform.feature.player.impl

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.platform.web.WebPlayerEngine
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
actual fun PlatformPlayerSurface(modifier: Modifier) {
    val engine: PlayerEngine = koinInject()
    val webEngine = engine as? WebPlayerEngine

    LaunchedEffect(webEngine) {
        webEngine?.setSurfaceVisible(true)
        while (true) {
            webEngine?.syncFromDom()
            delay(400)
        }
    }

    DisposableEffect(webEngine) {
        onDispose {
            webEngine?.setSurfaceVisible(false)
        }
    }

    // Punch a transparent hole in the Compose canvas so the HTML5 <video> behind it shows through.
    Canvas(modifier = modifier) {
        drawRect(color = Color.Transparent, blendMode = BlendMode.Clear)
    }
}
