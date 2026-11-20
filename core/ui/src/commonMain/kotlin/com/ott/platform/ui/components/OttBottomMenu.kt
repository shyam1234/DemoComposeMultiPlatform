package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors

/**
 * Phone bottom navigation — supports dynamic CMS menu tabs + Search/Library.
 * Automatically switches to horizontal scrolling when CMS adds > 5 items.
 */
@Composable
fun OttBottomMenu(
    items: List<OttMenuItem>,
    selectedId: String?,
    onItemSelected: (OttMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollable = items.size > 5
    val focusShape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OttColors.Surface)
            .navigationBarsPadding()
            .testTag("ott_bottom_menu"),
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (scrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = if (scrollable) Arrangement.spacedBy(8.dp) else Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.id == selectedId
                var focused by remember { mutableStateOf(false) }
                val interaction = remember { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .then(if (scrollable) Modifier.widthIn(min = 76.dp) else Modifier.weight(1f))
                        .clip(focusShape)
                        .background(
                            if (focused) OttColors.Focus.copy(alpha = 0.18f) else Color.Transparent,
                            focusShape,
                        )
                        .then(
                            if (focused) {
                                Modifier.border(1.5.dp, OttColors.Focus, focusShape)
                            } else {
                                Modifier
                            },
                        )
                        .onFocusChanged { focused = it.isFocused }
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = { onItemSelected(item) },
                        )
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                        .testTag("ott_bottom_${item.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            focused -> Color.White
                            selected -> OttColors.Focus
                            else -> OttColors.OnSurfaceMuted
                        },
                        fontWeight = if (selected || focused) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Spacer(
                        Modifier
                            .fillMaxWidth(if (scrollable) 0.7f else 0.28f)
                            .height(2.dp)
                            .background(if (selected) OttColors.Focus else Color.Transparent),
                    )
                }
            }
        }
    }
}
