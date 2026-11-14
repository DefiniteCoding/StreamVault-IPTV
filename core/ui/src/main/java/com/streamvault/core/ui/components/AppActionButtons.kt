package com.streamvault.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.components.shell.FocusPill
import com.streamvault.core.ui.components.shell.PillColors
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.LocalAppSpacing

/**
 * Dialog button colors. A focused primary stays filled `brand` and gains the ring (rule 16); a secondary keeps its
 * fill, so the ring and scale carry focus. Pairs are covered by ActionButtonContrastTest in every theme.
 */
internal fun dialogButtonColors(palette: AppPalette, primary: Boolean): PillColors =
    if (primary) {
        PillColors(
            restContainer = palette.brand,
            restContent = palette.onPrimary,
            selectedContainer = palette.brand,
            selectedContent = palette.onPrimary,
            focusedContainer = palette.brand,
            focusedContent = palette.onPrimary,
            ring = palette.focus
        )
    } else {
        PillColors(
            restContainer = palette.surfaceElevated,
            restContent = palette.textPrimary,
            selectedContainer = palette.surfaceElevated,
            selectedContent = palette.textPrimary,
            focusedContainer = palette.surfaceElevated,
            focusedContent = palette.textPrimary,
            ring = palette.focus
        )
    }

/**
 * Dialog action. A dialog has one [primary] button (filled brand) and any number of secondary ones (filled
 * surfaceElevated), per project rules 12 and 13. Set [focusRequester] on the button that takes initial focus.
 */
@Composable
fun AppDialogButton(
    text: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    val spacing = LocalAppSpacing.current
    val palette = BrowseColors.current
    val colors = remember(palette, primary) { dialogButtonColors(palette, primary) }
    val ownFocusRequester = remember { FocusRequester() }
    FocusPill(
        selected = null,
        colors = colors,
        role = Role.Button,
        onClick = onClick,
        modifier = modifier,
        focusRequester = focusRequester ?: ownFocusRequester
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs)
        )
    }
}
