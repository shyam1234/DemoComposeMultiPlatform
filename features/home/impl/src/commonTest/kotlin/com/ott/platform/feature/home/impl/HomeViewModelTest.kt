package com.ott.platform.feature.home.impl

import com.ott.platform.domain.usecase.GetHomeFeedUseCase
import com.ott.platform.testing.FakeCatalogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Test
    fun `loads home feed from catalog`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val vm = HomeViewModel(GetHomeFeedUseCase(FakeCatalogRepository()))
            advanceUntilIdle()
            val state = vm.state.value
            assertFalse(state.isLoading)
            assertNotNull(state.feed)
            assertEquals("Ott News 24", state.feed?.hero?.title)
            assertEquals(3, state.feed?.shelves?.size)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
