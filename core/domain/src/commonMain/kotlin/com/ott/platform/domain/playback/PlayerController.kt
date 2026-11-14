package com.ott.platform.domain.playback

import kotlinx.coroutines.flow.StateFlow

class PlayerController(
    private val engine: PlayerEngine,
) {
    val state: StateFlow<PlayerState> = engine.playerState

    fun prepare(media: MediaItem) = engine.prepare(media)
    fun play() = engine.play()
    fun pause() = engine.pause()

    fun togglePlayPause() {
        when (engine.playerState.value.status) {
            PlayerStatus.Playing -> engine.pause()
            PlayerStatus.Paused, PlayerStatus.Ready, PlayerStatus.Ended -> engine.play()
            else -> Unit
        }
    }

    fun seekTo(positionMs: Long) = engine.seekTo(positionMs.coerceAtLeast(0L))
    fun selectSubtitle(trackId: String?) = engine.selectSubtitle(trackId)
    fun selectAudioTrack(trackId: String) = engine.selectAudioTrack(trackId)
    fun selectVideoQuality(qualityId: String) = engine.selectVideoQuality(qualityId)
    fun release() = engine.release()
}
