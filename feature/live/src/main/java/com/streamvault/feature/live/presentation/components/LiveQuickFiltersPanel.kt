package com.streamvault.feature.live.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.BrowseFocusSpec
import com.streamvault.core.ui.interaction.TvButton
import com.streamvault.core.ui.design.browse.OnSurfaceDim

const val LIVE_ALL_CATEGORY_FILTER_KEY = "__all_categories__"

fun liveQuickFilterKeys(savedFilters: List<String>): List<String> = buildList {
    add(LIVE_ALL_CATEGORY_FILTER_KEY)
    addAll(savedFilters)
}

fun liveQuickFilterSelection(activeFilter: String?, categorySearchQuery: String): String? =
    activeFilter ?: LIVE_ALL_CATEGORY_FILTER_KEY.takeIf { categorySearchQuery.isBlank() }

/** Saved filters, add-filter and hidden-item shortcuts. Shown in a dialog opened from the Quick filters pill. */
@Composable
fun LiveQuickFiltersContent(
    savedFilters: List<String>,
    categorySearchQuery: String,
    activeFilter: String?,
    hiddenCategoriesButtonLabel: String?,
    hiddenChannelsButtonLabel: String?,
    chipTitle: String,
    allLabel: String,
    emptyLabel: String,
    addChipLabel: String,
    onShowHiddenCategories: () -> Unit,
    onShowHiddenChannels: () -> Unit,
    onAddFilter: () -> Unit,
    onAllSelected: () -> Unit,
    onSavedFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasFilterContent = savedFilters.isNotEmpty() || categorySearchQuery.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BrowseFocusSpec.SelectableRingWidth)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hiddenCategoriesButtonLabel?.let { label ->
            TvButton(
                onClick = onShowHiddenCategories,
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = label) }
        }
        hiddenChannelsButtonLabel?.let { label ->
            TvButton(
                onClick = onShowHiddenChannels,
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = label) }
        }
        TvButton(
            onClick = onAddFilter,
            modifier = Modifier.fillMaxWidth()
        ) { Text(text = addChipLabel) }

        if (hasFilterContent) {
            LiveSelectionChipRow(
                title = chipTitle,
                chips = buildList {
                    add(LiveSelectionChip(LIVE_ALL_CATEGORY_FILTER_KEY, allLabel))
                    addAll(savedFilters.map { filter -> LiveSelectionChip(filter, filter) })
                },
                selectedKey = liveQuickFilterSelection(activeFilter, categorySearchQuery),
                onChipSelected = { key ->
                    if (key == LIVE_ALL_CATEGORY_FILTER_KEY) onAllSelected() else onSavedFilterSelected(key)
                },
                modifier = Modifier.padding(bottom = 10.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            )
        } else {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
        }
    }
}
