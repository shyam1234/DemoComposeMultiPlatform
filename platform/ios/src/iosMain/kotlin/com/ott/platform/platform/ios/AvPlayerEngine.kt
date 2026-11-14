package com.ott.platform.platform.ios

import com.ott.platform.domain.playback.MediaItem
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.domain.playback.PlayerState
import com.ott.platform.domain.playback.PlayerStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.error
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.status
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.removeObserver
import platform.darwin.NSObjectProtocol

/**
 * AVPlayer-backed [PlayerEngine] for iPhone / iPad.
 */
@OptIn(ExperimentalForeignApi::class)
class AvPlayerEngine : PlayerEngine {

    private val _state = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _state.asStateFlow()

    private val player: AVPlayer = AVPlayer()
    private var endObserver: NSObjectProtocol? = null

    override fun prepare(media: MediaItem) {
        clearObservers()
        val url = NSURL.URLWithString(media.streamUrl)
        if (url == null) {
            _state.update {
                it.copy(
                    media = media,
                    status = PlayerStatus.Error,
                    errorMessage = "Invalid stream URL",
                )
            }
            return
        }
        val item = AVPlayerItem(uRL = url)
        _state.update {
            it.copy(
                media = media,
                status = PlayerStatus.Buffering,
                errorMessage = null,
                positionMs = 0L,
                durationMs = 0L,
            )
        }
        player.replaceCurrentItemWithPlayerItem(item)
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null,
        ) { _ ->
            _state.update { it.copy(status = PlayerStatus.Ended) }
        }
        player.play()
        syncFromPlayer()
    }

    fun syncFromPlayer() {
        val item = player.currentItem
        val durationSec = item?.let { CMTimeGetSeconds(it.duration) }
            ?.takeIf { !it.isNaN() && it.isFinite() && it > 0.0 }
            ?: 0.0
        val positionSec = CMTimeGetSeconds(player.currentTime())
            .takeIf { !it.isNaN() && it.isFinite() }
            ?: 0.0
        val status = when {
            item?.status == AVPlayerItemStatusFailed -> PlayerStatus.Error
            player.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate ->
                PlayerStatus.Buffering
            player.timeControlStatus == AVPlayerTimeControlStatusPlaying -> PlayerStatus.Playing
            item?.status == AVPlayerItemStatusReadyToPlay -> {
                if (_state.value.status == PlayerStatus.Ended) PlayerStatus.Ended else PlayerStatus.Paused
            }
            else -> _state.value.status
        }
        _state.update {
            it.copy(
                status = status,
                errorMessage = if (status == PlayerStatus.Error) {
                    item?.error?.localizedDescription ?: "Playback error"
                } else {
                    it.errorMessage
                },
                durationMs = (durationSec * 1000.0).toLong(),
                positionMs = (positionSec * 1000.0).toLong().coerceAtLeast(0L),
            )
        }
    }

    override fun play() {
        player.play()
        syncFromPlayer()
    }

    override fun pause() {
        player.pause()
        syncFromPlayer()
    }

    override fun seekTo(positionMs: Long) {
        val time = CMTimeMakeWithSeconds(positionMs / 1000.0, 600)
        player.seekToTime(time)
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun selectSubtitle(trackId: String?) = Unit

    override fun selectAudioTrack(trackId: String) = Unit

    override fun selectVideoQuality(qualityId: String) = Unit

    override fun release() {
        clearObservers()
        player.pause()
        player.replaceCurrentItemWithPlayerItem(null)
        _state.value = PlayerState()
    }

    override fun nativePlayer(): Any = player

    private fun clearObservers() {
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
    }
}
