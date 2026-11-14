package com.ott.platform.feature.search.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
fun SearchRoute(
    onOpenDetails: (Channel) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SearchScreen(state = state, onQueryChange = viewModel::onQueryChange, onOpenDetails = onOpenDetails)
}

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onOpenDetails: (Channel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(OttDimens.ScreenPadding)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().testTag("search_field"),
            singleLine = true,
            label = { Text("Search IPTV channels") },
            placeholder = { Text("news, sports, movies…") },
        )
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp).testTag("search_loading"))
        }
        state.error?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().testTag("search_results"),
        ) {
            items(state.results, key = { it.id.value }) { channel ->
                ChannelCard(channel = channel, onClick = { onOpenDetails(channel) })
            }
        }
    }
}
