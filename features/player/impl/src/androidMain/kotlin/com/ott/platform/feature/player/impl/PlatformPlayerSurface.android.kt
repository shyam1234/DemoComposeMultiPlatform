package com.ott.platform.feature.player.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ott.platform.domain.playback.PlayerController
import org.koin.compose.koinInject

@Composable
actual fun PlatformPlayerSurface(modifier: Modifier) {
    val controller: PlayerController = koinInject()
    val state by controller.state.collectAsState()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                // Compose owns chrome; keep Media3 surface-only for a clean OTT layout.
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                setShutterBackgroundColor(android.graphics.Color.BLACK)
                keepScreenOn = true
                player = controller.nativePlayer() as? ExoPlayer
            }
        },
        update = { view ->
            val latest = controller.nativePlayer() as? ExoPlayer
            if (view.player !== latest) {
                view.player = latest
            }
            @Suppress("UNUSED_EXPRESSION")
            state.status
        },
    )
}
