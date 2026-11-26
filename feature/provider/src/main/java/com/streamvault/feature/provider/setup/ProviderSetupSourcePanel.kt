package com.streamvault.feature.provider.setup

import com.streamvault.core.ui.design.BrowseShapes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface as TvSurface
import androidx.tv.material3.SurfaceDefaults
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
import com.streamvault.core.ui.design.browse.*

@Composable
internal fun SourceTypeSelectorPanel(
    sourceType: SourceType,
    isEditing: Boolean,
    isEditLabel: String,
    onSelect: (SourceType) -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedFocusRequester: FocusRequester? = null
) {
    val selectedFocusModifier = if (selectedFocusRequester != null) Modifier.focusRequester(selectedFocusRequester) else Modifier
    TvSurface(
        modifier = modifier,
        shape = BrowseShapes.Panel,
        colors = SurfaceDefaults.colors(containerColor = Surface.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = isEditLabel,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.setup_shell_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.setup_source_type_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            if (!isEditing || sourceType == SourceType.XTREAM) {
                SourceTypeCard(
                    title = androidx.compose.ui.res.stringResource(R.string.setup_xtream),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.setup_info_xtream_body),
                    selected = sourceType == SourceType.XTREAM,
                    modifier = if (sourceType == SourceType.XTREAM) selectedFocusModifier else Modifier,
                    enabled = !isEditing,
                    onClick = { onSelect(SourceType.XTREAM) }
                )
            }
            if (!isEditing || sourceType == SourceType.STALKER) {
                SourceTypeCard(
                    title = androidx.compose.ui.res.stringResource(R.string.setup_stalker),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.setup_stalker_card_hint),
                    selected = sourceType == SourceType.STALKER,
                    modifier = if (sourceType == SourceType.STALKER) selectedFocusModifier else Modifier,
                    enabled = !isEditing,
                    onClick = { onSelect(SourceType.STALKER) }
                )
            }
            if (!isEditing || sourceType == SourceType.M3U_URL) {
                SourceTypeCard(
                    title = androidx.compose.ui.res.stringResource(R.string.setup_tab_url),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.setup_info_m3u_body),
                    selected = sourceType == SourceType.M3U_URL,
                    modifier = if (sourceType == SourceType.M3U_URL) selectedFocusModifier else Modifier,
                    enabled = !isEditing,
                    onClick = { onSelect(SourceType.M3U_URL) }
                )
            }
            if (!isEditing || sourceType == SourceType.M3U_FILE) {
                SourceTypeCard(
                    title = androidx.compose.ui.res.stringResource(R.string.setup_tab_file),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.setup_file_browse_hint),
                    selected = sourceType == SourceType.M3U_FILE,
                    modifier = if (sourceType == SourceType.M3U_FILE) selectedFocusModifier else Modifier,
                    enabled = !isEditing,
                    onClick = { onSelect(SourceType.M3U_FILE) }
                )
            }
            if (!isEditing || sourceType == SourceType.JELLYFIN) {
                SourceTypeCard(
                    title = androidx.compose.ui.res.stringResource(R.string.setup_tab_jellyfin),
                    subtitle = stringResource(R.string.setup_jellyfin_source_subtitle),
                    selected = sourceType == SourceType.JELLYFIN,
                    modifier = if (sourceType == SourceType.JELLYFIN) selectedFocusModifier else Modifier,
                    enabled = !isEditing,
                    onClick = { onSelect(SourceType.JELLYFIN) }
                )
            }
            if (!isEditing) {
                ImportOptionsButton(
                    text = stringResource(R.string.settings_restore_data),
                    onClick = onImportClick,
                    compact = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
internal fun SourceTypeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvSurface(
        onClick = { if (enabled) onClick() },
        modifier = modifier.fillMaxWidth().mouseClickable(enabled = enabled, onClick = onClick),
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Artwork),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) BrowseColors.SelectedContainer else SurfaceElevated,
            focusedContainerColor = if (selected) BrowseColors.SelectedContainer else SurfaceHighlight
        ),
        // No resting stroke; the ring is the focus state. Selection is the tonal fill plus a check, never color alone.
        border = ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(BrowseFocusSpec.SelectableRingWidth, FocusBorder))
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrowseColors.OnSelectedContainer)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) BrowseColors.OnSelectedContainer else TextPrimary
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) BrowseColors.OnSelectedContainer else OnSurfaceDim,
                maxLines = 2
            )
        }
    }
}
