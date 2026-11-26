package com.streamvault.feature.provider.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.Icon
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.BrowseFocusSpec
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.Text
import com.streamvault.feature.provider.R
import com.streamvault.core.ui.interaction.mouseClickable
import com.streamvault.core.ui.design.browse.FocusBorder
import com.streamvault.core.ui.design.browse.OnSurface
import com.streamvault.core.ui.design.browse.Primary
import com.streamvault.core.ui.design.browse.Surface as SurfaceColor
import com.streamvault.core.ui.design.browse.SurfaceHighlight
import com.streamvault.core.ui.design.browse.TextPrimary

internal enum class SourceType { XTREAM, STALKER, M3U_URL, M3U_FILE, JELLYFIN }

@Composable
internal fun SourceTypeTabRow(
    sourceType: SourceType,
    isEditing: Boolean,
    onSelect: (SourceType) -> Unit,
    modifier: Modifier = Modifier,
    selectedFocusRequester: FocusRequester? = null
) {
    val selectedFocusModifier = if (selectedFocusRequester != null) Modifier.focusRequester(selectedFocusRequester) else Modifier
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!isEditing || sourceType == SourceType.XTREAM) {
            TabButton(
                text = androidx.compose.ui.res.stringResource(R.string.setup_xtream),
                isSelected = sourceType == SourceType.XTREAM,
                modifier = if (sourceType == SourceType.XTREAM) selectedFocusModifier else Modifier,
                onClick = { if (!isEditing) onSelect(SourceType.XTREAM) }
            )
        }
        if (!isEditing || sourceType == SourceType.STALKER) {
            TabButton(
                text = androidx.compose.ui.res.stringResource(R.string.setup_stalker),
                isSelected = sourceType == SourceType.STALKER,
                modifier = if (sourceType == SourceType.STALKER) selectedFocusModifier else Modifier,
                onClick = { if (!isEditing) onSelect(SourceType.STALKER) }
            )
        }
        if (!isEditing || sourceType == SourceType.M3U_URL) {
            TabButton(
                text = androidx.compose.ui.res.stringResource(R.string.setup_tab_url),
                isSelected = sourceType == SourceType.M3U_URL,
                modifier = if (sourceType == SourceType.M3U_URL) selectedFocusModifier else Modifier,
                onClick = { if (!isEditing) onSelect(SourceType.M3U_URL) }
            )
        }
        if (!isEditing || sourceType == SourceType.M3U_FILE) {
            TabButton(
                text = androidx.compose.ui.res.stringResource(R.string.setup_tab_file),
                isSelected = sourceType == SourceType.M3U_FILE,
                modifier = if (sourceType == SourceType.M3U_FILE) selectedFocusModifier else Modifier,
                onClick = { if (!isEditing) onSelect(SourceType.M3U_FILE) }
            )
        }
        if (!isEditing || sourceType == SourceType.JELLYFIN) {
            TabButton(
                text = androidx.compose.ui.res.stringResource(R.string.setup_tab_jellyfin),
                isSelected = sourceType == SourceType.JELLYFIN,
                modifier = if (sourceType == SourceType.JELLYFIN) selectedFocusModifier else Modifier,
                onClick = { if (!isEditing) onSelect(SourceType.JELLYFIN) }
            )
        }
    }
}

@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = modifier.onFocusEvent { isFocused = it.hasFocus }.mouseClickable(onClick = onClick),
        shape = ClickableSurfaceDefaults.shape(androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) BrowseColors.SelectedContainer else SurfaceColor,
            focusedContainerColor = if (isSelected) BrowseColors.SelectedContainer else SurfaceHighlight
        ),
        // No resting stroke; the ring is the focus state. Selection is the tonal fill plus a check, never color alone.
        border = ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(BrowseFocusSpec.SelectableRingWidth, FocusBorder))
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrowseColors.OnSelectedContainer)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) BrowseColors.OnSelectedContainer else if (isFocused) TextPrimary else OnSurface
            )
        }
    }
}
