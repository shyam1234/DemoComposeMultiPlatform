package com.ott.platform.domain.playback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerControllerTest {

    private class RecordingEngine : PlayerEngine {
        var playCount = 0
        var pauseCount = 0
        private val _state = MutableStateFlow(PlayerState(status = PlayerStatus.Ready))
        override val playerState: StateFlow<PlayerState> = _state

        override fun prepare(media: MediaItem) {
            _state.value = PlayerState(status = PlayerStatus.Ready, media = media, durationMs = 10_000)
        }
        override fun play() { playCount++; _state.value = _state.value.copy(status = PlayerStatus.Playing) }
        override fun pause() { pauseCount++; _state.value = _state.value.copy(status = PlayerStatus.Paused) }
        override fun seekTo(positionMs: Long) { _state.value = _state.value.copy(positionMs = positionMs) }
        override fun selectSubtitle(trackId: String?) = Unit
        override fun selectAudioTrack(trackId: String) = Unit
        override fun selectVideoQuality(qualityId: String) = Unit
        override fun release() { _state.value = PlayerState() }
    }

    @Test
    fun `toggle play pause switches between playing and paused`() {
        val engine = RecordingEngine()
        val controller = PlayerController(engine)
        controller.prepare(MediaItem("1", "Demo", "https://example.com/a.m3u8"))
        controller.togglePlayPause()
        assertEquals(1, engine.playCount)
        assertEquals(PlayerStatus.Playing, controller.state.value.status)
        controller.togglePlayPause()
        assertEquals(1, engine.pauseCount)
        assertEquals(PlayerStatus.Paused, controller.state.value.status)
    }

    @Test
    fun `seek coerces negative positions to zero`() {
        val engine = RecordingEngine()
        val controller = PlayerController(engine)
        controller.prepare(MediaItem("1", "Demo", "https://example.com/a.m3u8"))
        controller.seekTo(-50)
        assertEquals(0L, controller.state.value.positionMs)
    }
}
