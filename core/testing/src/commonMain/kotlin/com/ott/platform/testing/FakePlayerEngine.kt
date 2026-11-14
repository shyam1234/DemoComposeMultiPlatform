package com.ott.platform.testing

import com.ott.platform.domain.playback.AudioTrack
import com.ott.platform.domain.playback.MediaItem
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.domain.playback.PlayerState
import com.ott.platform.domain.playback.PlayerStatus
import com.ott.platform.domain.playback.SubtitleTrack
import com.ott.platform.domain.playback.VideoQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePlayerEngine(
    private val fakeDurationMs: Long = 120_000L,
) : PlayerEngine {
    private val _state = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _state.asStateFlow()

    override fun prepare(media: MediaItem) {
        _state.value = PlayerState(
            status = PlayerStatus.Ready,
            media = media,
            durationMs = fakeDurationMs,
            positionMs = 0L,
            subtitles = listOf(SubtitleTrack("off", "Off"), SubtitleTrack("en", "English", "en")),
            audioTracks = listOf(AudioTrack("a1", "Default", "und")),
            videoQualities = listOf(
                VideoQuality("auto", "Auto"),
                VideoQuality("720", "720p", 720),
            ),
            selectedSubtitleId = "off",
            selectedAudioId = "a1",
            selectedQualityId = "auto",
        )
    }

    override fun play() {
        _state.update { it.copy(status = PlayerStatus.Playing, errorMessage = null) }
    }

    override fun pause() {
        _state.update { it.copy(status = PlayerStatus.Paused) }
    }

    override fun seekTo(positionMs: Long) {
        _state.update {
            it.copy(positionMs = positionMs.coerceIn(0L, it.durationMs.coerceAtLeast(0L)))
        }
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

    override fun release() {
        _state.value = PlayerState(status = PlayerStatus.Idle)
    }
}
