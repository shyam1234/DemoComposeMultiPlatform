package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors

/**
 * Phone bottom navigation — replaces the leanback top tab strip on mobile.
 */
@Composable
fun OttBottomMenu(
    items: List<OttMenuItem>,
    selectedId: String?,
    onItemSelected: (OttMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.id == selectedId
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = { onItemSelected(item) })
                        .padding(vertical = 8.dp)
                        .testTag("ott_bottom_${item.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) OttColors.Focus else OttColors.OnSurfaceMuted,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Spacer(
                        Modifier
                            .fillMaxWidth(0.28f)
                            .height(2.dp)
                            .background(if (selected) OttColors.Focus else Color.Transparent),
                    )
                }
            }
        }
    }
}
