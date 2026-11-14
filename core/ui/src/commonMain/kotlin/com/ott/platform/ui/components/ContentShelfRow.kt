package com.ott.platform.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentShelf

@Composable
fun ContentShelfRow(
    shelf: ContentShelf,
    onChannelClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    isTv: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shelf_${shelf.id}"),
    ) {
        Text(
            text = shelf.title,
            style = if (isTv) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = OttDimens.ScreenPadding),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = OttDimens.ScreenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(shelf.items, key = { it.id.value }) { channel ->
                ChannelCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    width = if (isTv) OttDimens.TvCardWidth else OttDimens.CardWidth,
                    height = if (isTv) OttDimens.TvCardHeight else OttDimens.CardHeight,
                )
            }
        }
    }
}
