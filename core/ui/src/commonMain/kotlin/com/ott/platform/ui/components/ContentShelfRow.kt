package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.RailContentType
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
        ShelfCardStyle.Portrait -> if (isTv) 22.dp else 12.dp
        ShelfCardStyle.Square -> if (isTv) 20.dp else 12.dp
        ShelfCardStyle.Banner -> if (isTv) 24.dp else 14.dp
        ShelfCardStyle.Landscape -> if (isTv) OttDimens.RowGap else OttDimens.PhoneRowGap
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shelf_${shelf.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = shelf.title,
                style = if (isTv) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                color = OttColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
            )
            val badge = when (shelf.contentType) {
                RailContentType.Top10 -> "TOP 10"
                RailContentType.New -> "NEW"
                RailContentType.Editorial -> "SPOTLIGHT"
                RailContentType.Grid -> "${shelf.items.size} TITLES"
                else -> null
            }
            if (badge != null) {
                Spacer(Modifier.width(10.dp))
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = OttColors.AccentSoft,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OttColors.SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        if (shelf.contentType == RailContentType.Grid && shelf.items.size >= 4) {
            val columns = remember(shelf.items) { shelf.items.chunked(2) }
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = horizontal,
                    vertical = if (isTv) 16.dp else 8.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                itemsIndexed(columns, key = { idx, col -> "${shelf.id}_col_${idx}_${col.first().id.value}" }) { _, columnItems ->
                    Column(verticalArrangement = Arrangement.spacedBy(if (isTv) 18.dp else 12.dp)) {
                        columnItems.forEach { channel ->
                            ChannelCard(
                                channel = channel,
                                onClick = { onChannelClick(channel) },
                                style = shelf.cardStyle,
                                contentType = shelf.contentType,
                                rawDisplayType = shelf.rawDisplayType,
                                isTv = isTv,
                            )
                        }
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = horizontal,
                    vertical = if (isTv) 16.dp else 8.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                itemsIndexed(shelf.items, key = { idx, item -> "${shelf.id}_${idx}_${item.id.value}" }) { index, channel ->
                    ChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        style = shelf.cardStyle,
                        contentType = shelf.contentType,
                        rawDisplayType = shelf.rawDisplayType,
                        rank = if (shelf.contentType == RailContentType.Top10) index + 1 else null,
                        isTv = isTv,
                    )
                }
            }
        }
    }
}
