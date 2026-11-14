package com.ott.platform.feature.player.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.ott.platform.domain.playback.PlayerController
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.platform.ios.AvPlayerEngine
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.backgroundColor

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformPlayerSurface(modifier: Modifier) {
    val controller: PlayerController = koinInject()
    val engine: PlayerEngine = koinInject()
    val state by controller.state.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            (engine as? AvPlayerEngine)?.syncFromPlayer()
            delay(400)
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            PlayerContainerView().apply {
                backgroundColor = UIColor.blackColor
                bindPlayer(controller.nativePlayer() as? AVPlayer)
            }
        },
        update = { view ->
            view.bindPlayer(controller.nativePlayer() as? AVPlayer)
            @Suppress("UNUSED_EXPRESSION")
            state.status
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PlayerContainerView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val playerLayer: AVPlayerLayer = AVPlayerLayer().apply {
        videoGravity = AVLayerVideoGravityResizeAspect
    }

    init {
        layer.addSublayer(playerLayer)
    }

    fun bindPlayer(avPlayer: AVPlayer?) {
        if (playerLayer.player !== avPlayer) {
            playerLayer.player = avPlayer
        }
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        playerLayer.frame = bounds
    }
}
