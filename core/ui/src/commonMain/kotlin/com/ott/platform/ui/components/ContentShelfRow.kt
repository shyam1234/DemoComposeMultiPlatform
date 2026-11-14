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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.ShelfCardStyle

@Composable
fun ContentShelfRow(
    shelf: ContentShelf,
    onChannelClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    isTv: Boolean = false,
) {
    val horizontal = if (isTv) OttDimens.ScreenPadding else OttDimens.PhoneScreenPadding
    val gap = when (shelf.cardStyle) {
        ShelfCardStyle.Portrait -> if (isTv) 24.dp else 12.dp
        ShelfCardStyle.Landscape -> if (isTv) OttDimens.RowGap else OttDimens.PhoneRowGap
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shelf_${shelf.id}"),
    ) {
        Text(
            text = shelf.title,
            style = if (isTv) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            color = OttColors.OnSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = horizontal),
        )
        LazyRow(
            contentPadding = PaddingValues(
                horizontal = horizontal,
                vertical = if (isTv) 16.dp else 8.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            items(shelf.items, key = { it.id.value }) { channel ->
                ChannelCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    style = shelf.cardStyle,
                    isTv = isTv,
                )
            }
        }
    }
}
