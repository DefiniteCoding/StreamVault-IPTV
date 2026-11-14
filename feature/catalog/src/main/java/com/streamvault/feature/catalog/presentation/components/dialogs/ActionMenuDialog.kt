package com.streamvault.feature.catalog.presentation.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.components.AppDialogButton
import com.streamvault.core.ui.components.dialogs.AppDialog
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.BrowseShapes
import com.streamvault.core.ui.design.LocalAppSpacing
import kotlinx.coroutines.delay

// The long press that opens the menu ends with a key-up on the first row; ignore input until it has settled.
private const val InteractionDelayMs = 500L
private const val MenuWidthFraction = 0.34f

data class ActionMenuEntry(val label: String, val onClick: () -> Unit)

/**
 * Menu of equal actions opened by a long press. There is no primary action, so every row is a secondary
 * button (project rules 12 and 13); the first row takes initial focus. Choosing a row runs it, then dismisses.
 */
@Composable
fun ActionMenuDialog(
    title: String,
    entries: List<ActionMenuEntry>,
    onDismissRequest: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    var canInteract by remember { mutableStateOf(false) }
    val firstEntryFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        firstEntryFocusRequester.requestFocus()
        delay(InteractionDelayMs)
        canInteract = true
    }

    AppDialog(
        onDismissRequest = { if (canInteract) onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(MenuWidthFraction)
                .background(BrowseColors.Surface, BrowseShapes.Panel)
                .padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = BrowseColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = spacing.xs)
            )
            entries.forEachIndexed { index, entry ->
                AppDialogButton(
                    text = entry.label,
                    onClick = {
                        if (canInteract) {
                            onDismissRequest()
                            entry.onClick()
                        }
                    },
                    primary = false,
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = if (index == 0) firstEntryFocusRequester else null
                )
            }
        }
    }
}
