package com.ott.platform.platform.web

import com.ott.platform.domain.playback.MediaItem
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.domain.playback.PlayerState
import com.ott.platform.domain.playback.PlayerStatus
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalWasmJsInterop::class)
class WebPlayerEngine : PlayerEngine {

    private val _state = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _state.asStateFlow()

    override fun prepare(media: MediaItem) {
        _state.update {
            it.copy(
                media = media,
                status = PlayerStatus.Buffering,
                errorMessage = null,
                positionMs = 0L,
                durationMs = 0L,
            )
        }
        jsPrepare(media.streamUrl)
        jsSetSurfaceVisible(true)
    }

    override fun play() {
        jsPlay()
        syncFromDom()
    }

    override fun pause() {
        jsPause()
        syncFromDom()
    }

    override fun seekTo(positionMs: Long) {
        jsSeekTo(positionMs.coerceAtLeast(0L).toDouble())
        _state.update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    override fun selectSubtitle(trackId: String?) {
        _state.update { it.copy(selectedSubtitleId = trackId) }
    }

    override fun selectAudioTrack(trackId: String) {
        _state.update { it.copy(selectedAudioId = trackId) }
    }

    override fun selectVideoQuality(qualityId: String) {
        _state.update { it.copy(selectedQualityId = qualityId) }
    }

    fun setSurfaceVisible(visible: Boolean) {
        jsSetSurfaceVisible(visible)
    }

    fun syncFromDom() {
        val status = when (jsGetStatus()) {
            "playing" -> PlayerStatus.Playing
            "paused" -> PlayerStatus.Paused
            "buffering" -> PlayerStatus.Buffering
            "ready" -> PlayerStatus.Ready
            "ended" -> PlayerStatus.Ended
            "error" -> PlayerStatus.Error
            else -> _state.value.status
        }
        val pos = jsGetPositionMs().toLong().coerceAtLeast(0L)
        val dur = jsGetDurationMs().toLong().coerceAtLeast(0L)
        val err = jsGetError().takeIf { it.isNotBlank() }

        _state.update { current ->
            current.copy(
                status = status,
                positionMs = pos,
                durationMs = dur,
                errorMessage = if (status == PlayerStatus.Error) err ?: current.errorMessage else null,
            )
        }
    }

    override fun release() {
        jsSetSurfaceVisible(false)
        jsRelease()
        _state.value = PlayerState()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsPrepare(url: String): Unit =
    js("window.__ottWebPlayer && window.__ottWebPlayer.prepare(url)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsPlay(): Unit =
    js("window.__ottWebPlayer && window.__ottWebPlayer.play()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsPause(): Unit =
    js("window.__ottWebPlayer && window.__ottWebPlayer.pause()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsSeekTo(positionMs: Double): Unit =
    js("window.__ottWebPlayer && window.__ottWebPlayer.seekTo(positionMs)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsRelease(): Unit =
    js("window.__ottWebPlayer && window.__ottWebPlayer.release()")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsSetSurfaceVisible(visible: Boolean): Unit =
    js("window.__ottWebPlayer && window.__ottWebPlayer.setVisible(visible)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsGetStatus(): String =
    js("(window.__ottWebPlayer && window.__ottWebPlayer.getStatus()) || 'idle'")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsGetPositionMs(): Double =
    js("(window.__ottWebPlayer && window.__ottWebPlayer.getPositionMs()) || 0")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsGetDurationMs(): Double =
    js("(window.__ottWebPlayer && window.__ottWebPlayer.getDurationMs()) || 0")

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsGetError(): String =
    js("(window.__ottWebPlayer && window.__ottWebPlayer.getError()) || ''")
