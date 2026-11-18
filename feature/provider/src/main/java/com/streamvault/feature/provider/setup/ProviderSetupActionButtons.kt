package com.streamvault.feature.provider.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.interaction.TvClickableSurface
import com.streamvault.core.ui.design.browse.FocusBorder
import com.streamvault.core.ui.design.browse.OnBackground
import com.streamvault.core.ui.design.BrowseFocusSpec
import com.streamvault.core.ui.design.browse.Primary
import com.streamvault.core.ui.design.browse.SurfaceHighlight
import com.streamvault.core.ui.design.browse.TextPrimary

@Composable
internal fun ActionButton(
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    ProviderActionButton(
        text = text,
        height = 52.dp,
        isPrimary = true,
        isLoading = isLoading,
        onClick = onClick
    )
}

@Composable
internal fun SmallActionButton(
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    ProviderActionButton(
        text = text,
        height = 40.dp,
        isPrimary = false,
        isLoading = isLoading,
        onClick = onClick
    )
}

@Composable
private fun ProviderActionButton(
    text: String,
    height: androidx.compose.ui.unit.Dp,
    isPrimary: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        enabled = !isLoading,
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .height(height),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = when {
                isLoading -> SurfaceHighlight
                isPrimary -> Primary
                else -> BrowseColors.ControlContainer
            },
            focusedContainerColor = when {
                isLoading -> SurfaceHighlight
                isPrimary -> Primary
                else -> SurfaceHighlight
            }
        ),
        // Ring plus scale: a focused primary keeps its brand fill (rule 16), so the fill cannot carry focus.
        scale = ClickableSurfaceDefaults.scale(focusedScale = BrowseFocusSpec.FocusedScale),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow.None),
        border = ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(3.dp, FocusBorder))
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = OnBackground.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPrimary) com.streamvault.core.ui.design.BrowseColors.OnPrimary else TextPrimary
                )
            }
        }
    }
}
