package com.ott.platform.feature.watchlist.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel
import com.ott.platform.ui.components.ChannelCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WatchlistRoute(
    onOpenDetails: (Channel) -> Unit,
    isTv: Boolean = false,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val items by viewModel.items.collectAsState()
    WatchlistScreen(items = items, onOpenDetails = onOpenDetails, isTv = isTv)
}

@Composable
fun WatchlistScreen(
    items: List<Channel>,
    onOpenDetails: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    isTv: Boolean = false,
) {
    val edge = if (isTv) OttDimens.ScreenPadding else OttDimens.PhoneScreenPadding
    Column(modifier.fillMaxSize().padding(edge)) {
        Text("Watchlist", style = MaterialTheme.typography.headlineMedium)
        if (items.isEmpty()) {
            Text(
                "Save channels from Details to watch later.",
                modifier = Modifier.padding(top = 16.dp).testTag("watchlist_empty"),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("watchlist_grid"),
            ) {
                items(items, key = { it.id.value }) { channel ->
                    ChannelCard(channel = channel, onClick = { onOpenDetails(channel) })
                }
            }
        }
    }
}
