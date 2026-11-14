package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens

data class OttMenuItem(
    val id: String,
    val label: String,
)

/**
 * Top nav: blue highlighter only on focused item.
 * Selected (current page) stays white text — not a permanent blue fill.
 */
@Composable
fun OttTopMenu(
    items: List<OttMenuItem>,
    selectedId: String?,
    onItemSelected: (OttMenuItem) -> Unit,
    modifier: Modifier = Modifier,
    brand: String = "OTT",
    isTv: Boolean = false,
    onSearchClick: (() -> Unit)? = null,
) {
    val horizontal = if (isTv) OttDimens.ScreenPadding else OttDimens.PhoneScreenPadding
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OttColors.Background)
            .padding(horizontal = horizontal, vertical = if (isTv) 16.dp else 10.dp)
            .testTag("ott_top_menu"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = brand,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("ott_brand"),
        )
        Spacer(Modifier.width(16.dp))
        items.forEach { item ->
            GoogleTvTab(
                label = item.label,
                selected = item.id == selectedId,
                onClick = { onItemSelected(item) },
                modifier = Modifier.testTag("ott_menu_${item.id}"),
            )
        }
        Spacer(Modifier.weight(1f))
        if (onSearchClick != null) {
            var searchFocused by remember { mutableStateOf(false) }
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .onFocusChanged { searchFocused = it.isFocused }
                    .clip(CircleShape)
                    .background(if (searchFocused) OttColors.Focus else Color.Transparent)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onSearchClick,
                    )
                    .testTag("ott_menu_search_icon"),
                contentAlignment = Alignment.Center,
            ) {
                Text("⌕", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun GoogleTvTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(if (focused) OttColors.Focus else Color.Transparent, shape)
            .then(
                if (selected && !focused) {
                    Modifier.border(2.dp, OttColors.Focus, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = when {
                focused || selected -> FontWeight.SemiBold
                else -> FontWeight.Medium
            },
        )
    }
}
