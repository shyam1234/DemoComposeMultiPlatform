package com.ott.platform.domain.playback

import kotlinx.coroutines.flow.StateFlow

data class MediaItem(
    val id: String,
    val title: String,
    val streamUrl: String,
    val posterUrl: String? = null,
    val referrer: String? = null,
    val userAgent: String? = null,
)

enum class PlayerStatus {
    Idle,
    Buffering,
    Ready,
    Playing,
    Paused,
    Ended,
    Error,
}

data class SubtitleTrack(val id: String, val label: String, val language: String? = null)
data class AudioTrack(val id: String, val label: String, val language: String? = null)
data class VideoQuality(val id: String, val label: String, val height: Int? = null)

data class PlayerState(
    val status: PlayerStatus = PlayerStatus.Idle,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val media: MediaItem? = null,
    val errorMessage: String? = null,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val videoQualities: List<VideoQuality> = emptyList(),
    val selectedSubtitleId: String? = null,
    val selectedAudioId: String? = null,
    val selectedQualityId: String? = null,
)

interface PlayerEngine {
    val playerState: StateFlow<PlayerState>

    fun prepare(media: MediaItem)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun selectSubtitle(trackId: String?)
    fun selectAudioTrack(trackId: String)
    fun selectVideoQuality(qualityId: String)
    fun release()
}
