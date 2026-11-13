package com.streamvault.core.ui.components

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.components.shell.FocusPill
import com.streamvault.core.ui.components.shell.PillColors
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.LocalAppSpacing

// Same as the top-nav tab icon, so chips and tabs share one height (36dp).
private val ChipIconSize = 18.dp

/** Chip colors on the browse page. Pairs are covered by ChipPaletteContrastTest in every theme. */
internal fun appChipColors(palette: AppPalette): PillColors = PillColors(
    restContainer = palette.controlContainer,
    restContent = palette.textPrimary,
    selectedContainer = palette.selectedContainer,
    selectedContent = palette.onSelectedContainer,
    focusedContainer = palette.focus,
    focusedContent = palette.onFocus,
    ring = palette.focus
)

/**
 * Single-choice filter chip. Selection shows as the tonal fill plus a check icon, so it never relies on color alone;
 * when a selected chip is focused the focus fill replaces the tonal one and the check carries the selection.
 * The check takes the leading icon's slot, so a chip with [leadingIcon] keeps its width when selected.
 */
@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val spacing = LocalAppSpacing.current
    val palette = BrowseColors.current
    val colors = remember(palette) { appChipColors(palette) }
    FocusPill(
        selected = selected,
        colors = colors,
        role = Role.RadioButton,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (selected) Icons.Default.Check else leadingIcon
            if (icon != null) {
                // The selected state and the label are announced through the chip's semantics.
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(ChipIconSize))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * A labelled run of chips on one line. Groups placed side by side in one row stay contiguous for the D-pad:
 * Left and Right cross from one group's last chip to the next group's first, with no wrap at either end.
 */
@Composable
fun AppChipGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val spacing = LocalAppSpacing.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = BrowseColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.focusGroup(),
            // Wider than the focus ring's outset, so a focused chip's ring never touches its neighbour.
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/** Pill that opens a picker: "label  value" with a trailing chevron. It has no selected state. */
@Composable
fun AppDropdownPill(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val palette = BrowseColors.current
    val colors = remember(palette) { appChipColors(palette) }
    FocusPill(
        selected = null,
        colors = colors,
        role = Role.DropdownList,
        onClick = onClick,
        modifier = modifier
    ) { focused ->
        Row(
            modifier = Modifier.padding(start = spacing.sm, end = spacing.xs, top = spacing.xs, bottom = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Normal,
                // At rest the label steps back to secondary ink; focused, it takes the focus ink like the value.
                color = if (focused) Color.Unspecified else palette.textSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(ChipIconSize))
        }
    }
}
