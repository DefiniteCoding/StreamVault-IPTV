package com.streamvault.feature.live.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LiveTvQuickFilterVisibilityMode
import com.streamvault.domain.model.VirtualCategoryIds
import com.streamvault.feature.live.R
import com.streamvault.feature.live.presentation.components.LiveChipRowItem
import com.streamvault.feature.live.presentation.components.LiveChipRowSection

private const val PILL_QUICK_FILTERS = "quick_filters"
private const val PILL_GUIDE = "guide"

/** Categories that live in the pill row instead of the sidebar list. */
internal fun isFilterPillCategory(categoryId: Long): Boolean =
    categoryId == VirtualCategoryIds.FAVORITES || categoryId == VirtualCategoryIds.RECENT

internal fun List<Category>.withoutFilterPillCategories(): List<Category> =
    filterNot { isFilterPillCategory(it.id) }

internal fun shouldShowQuickFiltersPill(
    mode: LiveTvQuickFilterVisibilityMode,
    savedFilters: List<String>
): Boolean = when (mode) {
    LiveTvQuickFilterVisibilityMode.HIDE -> false
    LiveTvQuickFilterVisibilityMode.SHOW_WHEN_FILTERS_AVAILABLE -> savedFilters.isNotEmpty()
    LiveTvQuickFilterVisibilityMode.ALWAYS_VISIBLE -> true
}

/** Only one pill reads as selected at a time; TV Guide navigates away, so it never holds a selected state. */
internal fun filterPillSelectedKey(selectedCategoryId: Long?, activeCategoryFilter: String?): String? = when {
    selectedCategoryId != null && isFilterPillCategory(selectedCategoryId) -> selectedCategoryId.toString()
    activeCategoryFilter != null -> PILL_QUICK_FILTERS
    else -> null
}

@Composable
fun LiveFilterPillRow(
    favorites: Category?,
    recent: Category?,
    selectedCategoryId: Long?,
    activeCategoryFilter: String?,
    showQuickFilters: Boolean,
    categoryFocusRequesters: MutableMap<Long, FocusRequester>,
    onCategory: (Category) -> Unit,
    onFocusedCategory: (Category) -> Unit,
    onCategoryOptions: (Category) -> Unit,
    onQuickFilters: () -> Unit,
    onGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickFiltersLabel = stringResource(R.string.home_quick_filters_button)
    val guideLabel = stringResource(R.string.epg_title)
    val favoritesLabel = favorites?.localizedName()
    val recentLabel = recent?.localizedName()
    val currentOnCategory by rememberUpdatedState(onCategory)
    val currentOnFocusedCategory by rememberUpdatedState(onFocusedCategory)
    val currentOnCategoryOptions by rememberUpdatedState(onCategoryOptions)
    val currentOnQuickFilters by rememberUpdatedState(onQuickFilters)
    val currentOnGuide by rememberUpdatedState(onGuide)

    val chips = remember(
        favorites, recent, favoritesLabel, recentLabel, showQuickFilters,
        quickFiltersLabel, guideLabel
    ) {
        buildList {
            favorites?.let { category ->
                add(
                    LiveChipRowItem(
                        key = category.id.toString(),
                        label = favoritesLabel.orEmpty(),
                        count = category.count,
                        focusRequester = categoryFocusRequesters.getOrPut(category.id) { FocusRequester() },
                        onFocused = { currentOnFocusedCategory(category) },
                        onLongClick = { currentOnCategoryOptions(category) },
                        onClick = { currentOnCategory(category) }
                    )
                )
            }
            recent?.let { category ->
                add(
                    LiveChipRowItem(
                        key = category.id.toString(),
                        label = recentLabel.orEmpty(),
                        count = category.count,
                        focusRequester = categoryFocusRequesters.getOrPut(category.id) { FocusRequester() },
                        onFocused = { currentOnFocusedCategory(category) },
                        onLongClick = { currentOnCategoryOptions(category) },
                        onClick = { currentOnCategory(category) }
                    )
                )
            }
            if (showQuickFilters) {
                add(LiveChipRowItem(key = PILL_QUICK_FILTERS, label = quickFiltersLabel, onClick = { currentOnQuickFilters() }))
            }
            add(LiveChipRowItem(key = PILL_GUIDE, label = guideLabel, onClick = { currentOnGuide() }))
        }
    }

    LiveChipRowSection(
        chips = chips,
        selectedKey = filterPillSelectedKey(selectedCategoryId, activeCategoryFilter),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp)
    )
}
