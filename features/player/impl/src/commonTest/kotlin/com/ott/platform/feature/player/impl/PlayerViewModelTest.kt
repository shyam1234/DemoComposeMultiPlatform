package com.ott.platform.feature.player.impl

import com.ott.platform.domain.playback.PlayerController
import com.ott.platform.domain.playback.PlayerStatus
import com.ott.platform.domain.usecase.GetChannelDetailsUseCase
import com.ott.platform.testing.FakeCatalogRepository
import com.ott.platform.testing.FakePlayerEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @Test
    fun `prepares and plays playable asset`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeCatalogRepository()
            val vm = PlayerViewModel(
                contentId = "new:the-rip",
                getChannelDetails = GetChannelDetailsUseCase(repo),
                playerController = PlayerController(FakePlayerEngine()),
            )
            advanceUntilIdle()
            val state = vm.playerState.first { it.status == PlayerStatus.Playing }
            assertEquals("The RIP", state.media?.title)
            assertEquals("https://example.com/rip.m3u8", state.media?.streamUrl)
            advanceTimeBy(6_000)
            advanceUntilIdle()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `reports friendly error when cms asset has no playback stream url`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeCatalogRepository()
            val vm = PlayerViewModel(
                contentId = "shows:colony",
                getChannelDetails = GetChannelDetailsUseCase(repo),
                playerController = PlayerController(FakePlayerEngine()),
            )
            advanceUntilIdle()
            val state = vm.playerState.first { it.status == PlayerStatus.Error }
            assertNotNull(state.errorMessage)
            assertEquals(
                "No playback stream URL published for \"Beyond Earth Colony\" yet.",
                state.errorMessage,
            )
            advanceTimeBy(6_000)
            advanceUntilIdle()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
