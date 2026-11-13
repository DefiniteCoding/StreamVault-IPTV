package com.streamvault.feature.catalog.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.streamvault.feature.catalog.R
import com.streamvault.core.ui.components.shell.AppSectionHeader
import com.streamvault.core.ui.components.shell.AppSectionToggleAction
import com.streamvault.core.ui.design.LocalAppSpacing

// ׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬ Netflix-style horizontal category row ׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬׳³ג€™׳’ג‚¬ֲ׳’ג€ֲ¬

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : Any> CategoryRow(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    onPinToggle: (() -> Unit)? = null,
    isPinned: Boolean = false,
    keySelector: (T) -> Any,
    contentTypeSelector: ((T) -> Any?)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val resolvedContentTypeSelector: (T) -> Any? = contentTypeSelector ?: { null }
    val seeAllFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .suppressParentVerticalScroll()
    ) {
        val headerPadding = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
        if (onSeeAll != null) {
            Row(
                modifier = headerPadding,
                horizontalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppSectionHeader(
                    title = title,
                    modifier = Modifier.weight(1f),
                    actionLabel = stringResource(R.string.category_see_all),
                    onActionClick = onSeeAll,
                    actionFocusRequester = seeAllFocusRequester
                )
                if (onPinToggle != null) {
                    AppSectionToggleAction(
                        icon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = stringResource(if (isPinned) R.string.category_unpin else R.string.category_pin),
                        selected = isPinned,
                        onClick = onPinToggle
                    )
                }
            }
        } else {
            AppSectionHeader(title = title, modifier = headerPadding)
        }

        // Posters sit far from the header actions, so geometry alone never reaches them on a scrolled row.
        // Up from any poster lands on "See all"; a second Up leaves for the row above. The property goes on a
        // wrapper per poster: focusProperties only reaches focusables up to the nearest enclosing focus group.
        val hasActions = onSeeAll != null
        val posterUp = remember(hasActions) {
            if (hasActions) Modifier.focusProperties { up = seeAllFocusRequester } else Modifier
        }
        LazyRow(
            modifier = Modifier.focusRestorer(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = items,
                key = keySelector,
                contentType = resolvedContentTypeSelector
            ) { item ->
                Box(modifier = posterUp) { itemContent(item) }
            }
        }
    }
}

