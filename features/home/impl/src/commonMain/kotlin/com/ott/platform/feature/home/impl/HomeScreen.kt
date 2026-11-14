package com.ott.platform.feature.home.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel
import com.ott.platform.ui.components.ContentShelfRow
import com.ott.platform.ui.components.HeroBannerCarousel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    onOpenDetails: (Channel) -> Unit,
    onPlay: (Channel) -> Unit,
    isTv: Boolean = false,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HomeScreen(
        state = state,
        isTv = isTv,
        onRetry = viewModel::refresh,
        onOpenDetails = onOpenDetails,
        onPlay = onPlay,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onOpenDetails: (Channel) -> Unit,
    onPlay: (Channel) -> Unit,
    isTv: Boolean = false,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.feed == null -> {
            Box(
                modifier
                    .fillMaxSize()
                    .background(OttColors.Background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = OttColors.Accent,
                    modifier = Modifier.testTag("home_loading"),
                )
            }
        }
        state.error != null && state.feed == null -> {
            Box(
                modifier
                    .fillMaxSize()
                    .background(OttColors.Background),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = onRetry, modifier = Modifier.testTag("home_retry")) {
                    Text(state.error ?: "Retry")
                }
            }
        }
        else -> {
            val feed = state.feed ?: return
            val edge = if (isTv) OttDimens.ScreenPadding else OttDimens.PhoneScreenPadding
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(OttColors.Background)
                    .testTag("home_feed"),
                contentPadding = PaddingValues(bottom = if (isTv) 48.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(
                    if (isTv) OttDimens.ShelfSpacing else OttDimens.PhoneShelfSpacing,
                ),
            ) {
                if (feed.heroes.isNotEmpty()) {
                    item(key = "hero_carousel") {
                        HeroBannerCarousel(
                            channels = feed.heroes,
                            onPlay = onPlay,
                            onDetails = onOpenDetails,
                            height = if (isTv) OttDimens.TvHeroHeight else OttDimens.HeroHeight,
                            isTv = isTv,
                        )
                    }
                }
                items(feed.shelves, key = { it.id }) { shelf ->
                    ContentShelfRow(
                        shelf = shelf,
                        onChannelClick = onOpenDetails,
                        isTv = isTv,
                    )
                }
                if (state.error != null) {
                    item {
                        Text(
                            text = "Partial error: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = edge),
                        )
                    }
                }
            }
        }
    }
}
