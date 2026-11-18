package com.streamvault.feature.catalog.presentation.components

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.feature.catalog.R
import com.streamvault.feature.catalog.presentation.vod.vodActiveFilterSortDetail

// Shown in this order; LibraryFilterType declares TOP_RATED before RECENTLY_UPDATED.
private val FilterChipOrder = listOf(
    LibraryFilterType.ALL,
    LibraryFilterType.FAVORITES,
    LibraryFilterType.IN_PROGRESS,
    LibraryFilterType.UNWATCHED,
    LibraryFilterType.RECENTLY_UPDATED,
    LibraryFilterType.TOP_RATED
)

/**
 * Series calls "recently updated" titles "Updated"; movies and VOD call them "Recent".
 */
@StringRes
internal fun LibraryFilterType.labelRes(recentAsUpdated: Boolean): Int = when (this) {
    LibraryFilterType.ALL -> R.string.library_chip_all
    LibraryFilterType.FAVORITES -> R.string.library_chip_favorites
    LibraryFilterType.IN_PROGRESS -> R.string.library_chip_resume
    LibraryFilterType.UNWATCHED -> R.string.library_chip_unwatched
    LibraryFilterType.RECENTLY_UPDATED ->
        if (recentAsUpdated) R.string.library_chip_updated else R.string.library_chip_recent
    LibraryFilterType.TOP_RATED -> R.string.library_chip_top_rated
}

/**
 * The summary beside the Filters & Sort button has little room, so it uses the short "Updated".
 */
@StringRes
internal fun LibrarySortBy.labelRes(short: Boolean = false): Int = when (this) {
    LibrarySortBy.LIBRARY -> R.string.library_chip_sort_library_order
    LibrarySortBy.TITLE -> R.string.library_chip_sort_title
    LibrarySortBy.RELEASE -> R.string.library_chip_sort_newest
    LibrarySortBy.UPDATED ->
        if (short) R.string.library_chip_sort_updated_short else R.string.library_chip_sort_recently_updated
    LibrarySortBy.RATING -> R.string.library_chip_sort_rating
    LibrarySortBy.WATCH_COUNT -> R.string.library_chip_sort_recent_activity
}

@Composable
fun rememberLibraryFilterChips(recentAsUpdated: Boolean = false): List<SelectionChip> {
    val resources = LocalResources.current
    val configuration = LocalConfiguration.current
    return remember(configuration, recentAsUpdated) {
        FilterChipOrder.map { filter ->
            SelectionChip(filter.name, resources.getString(filter.labelRes(recentAsUpdated)))
        }
    }
}

@Composable
fun rememberLibrarySortChips(): List<SelectionChip> {
    val resources = LocalResources.current
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        LibrarySortBy.entries.map { sort ->
            SelectionChip(sort.name, resources.getString(sort.labelRes()))
        }
    }
}

@Composable
fun rememberLibraryFilterSortDetail(
    filter: LibraryFilterType,
    sort: LibrarySortBy,
    recentAsUpdated: Boolean = false
): String? {
    val resources = LocalResources.current
    val configuration = LocalConfiguration.current
    return remember(filter, sort, recentAsUpdated, configuration) {
        vodActiveFilterSortDetail(filter, sort, recentAsUpdated, resources::getString)
    }
}

// The sorts offered as chips on the grid row; the dialog keeps the full list.
private val InlineSortOrder = listOf(LibrarySortBy.RELEASE, LibrarySortBy.TITLE, LibrarySortBy.RATING)

@Composable
fun rememberInlineLibrarySortChips(): List<SelectionChip> {
    val sortChips = rememberLibrarySortChips()
    return remember(sortChips) {
        InlineSortOrder.mapNotNull { sort -> sortChips.firstOrNull { it.key == sort.name } }
    }
}

@Composable
fun libraryCountLabel(@PluralsRes plural: Int, count: Int): String =
    pluralStringResource(plural, count, count)
