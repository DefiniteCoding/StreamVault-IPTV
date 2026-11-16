package com.streamvault.feature.catalog.presentation.vod

import android.view.KeyEvent
import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.Favorite
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.ProviderType
import com.streamvault.feature.catalog.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CatalogVodHelpersTest {
    @Test
    fun modernPreview_isHiddenWhileReorderingWithoutSelectedCategory() {
        assertThat(shouldShowVodPreview(selectedCategory = null, isReorderMode = false)).isEqualTo(true)
        assertThat(shouldShowVodPreview(selectedCategory = null, isReorderMode = true)).isEqualTo(false)
        assertThat(shouldShowVodPreview(selectedCategory = "Favorites", isReorderMode = true)).isEqualTo(false)
    }

    @Test
    fun reorderMoveDirection_treatsGridArrowsAsEarlierOrLater() {
        assertThat(vodReorderMoveDirection(KeyEvent.KEYCODE_DPAD_UP))
            .isEqualTo(VodReorderMoveDirection.EARLIER)
        assertThat(vodReorderMoveDirection(KeyEvent.KEYCODE_DPAD_LEFT))
            .isEqualTo(VodReorderMoveDirection.EARLIER)
        assertThat(vodReorderMoveDirection(KeyEvent.KEYCODE_DPAD_DOWN))
            .isEqualTo(VodReorderMoveDirection.LATER)
        assertThat(vodReorderMoveDirection(KeyEvent.KEYCODE_DPAD_RIGHT))
            .isEqualTo(VodReorderMoveDirection.LATER)
        assertThat(vodReorderMoveDirection(KeyEvent.KEYCODE_BACK)).isNull()
    }

    @Test
    fun reorderMovement_preservesIdentityAndNoOpEdges() {
        val items = listOf("a", "b", "c")
        assertThat(moveVodItemUp(items, "b")).containsExactly("b", "a", "c").inOrder()
        assertThat(moveVodItemDown(items, "b")).containsExactly("a", "c", "b").inOrder()
        assertThat(moveVodItemUp(items, "a")).isEqualTo(items)
        assertThat(moveVodItemDown(items, "c")).isEqualTo(items)
        assertThat(moveVodItemDown(items, "missing")).isEqualTo(items)
    }

    @Test
    fun filterAndSortDetail_isNullOnlyAtDefaults() {
        val labels = mapOf(
            R.string.library_chip_favorites to "Favorites",
            R.string.library_chip_unwatched to "Unwatched",
            R.string.library_chip_recent to "Recent",
            R.string.library_chip_updated to "Updated",
            R.string.library_chip_sort_rating to "Rating",
            R.string.library_chip_sort_title to "A-Z",
            R.string.library_chip_sort_updated_short to "Updated short"
        )
        val label: (Int) -> String = { id -> labels.getValue(id) }

        assertThat(vodActiveFilterSortDetail(LibraryFilterType.ALL, LibrarySortBy.LIBRARY, false, label)).isNull()
        assertThat(vodActiveFilterSortDetail(LibraryFilterType.FAVORITES, LibrarySortBy.RATING, false, label))
            .isEqualTo("Favorites \u00B7 Rating")
        assertThat(vodActiveFilterSortDetail(LibraryFilterType.UNWATCHED, LibrarySortBy.TITLE, false, label))
            .isEqualTo("Unwatched \u00B7 A-Z")
    }

    @Test
    fun filterAndSortDetail_namesRecentlyUpdatedPerContentKind() {
        val label: (Int) -> String = { id ->
            when (id) {
                R.string.library_chip_recent -> "Recent"
                R.string.library_chip_updated -> "Updated"
                R.string.library_chip_sort_updated_short -> "Updated short"
                else -> error("unexpected label $id")
            }
        }

        assertThat(vodActiveFilterSortDetail(LibraryFilterType.RECENTLY_UPDATED, LibrarySortBy.LIBRARY, false, label))
            .isEqualTo("Recent")
        assertThat(vodActiveFilterSortDetail(LibraryFilterType.RECENTLY_UPDATED, LibrarySortBy.LIBRARY, true, label))
            .isEqualTo("Updated")
        assertThat(vodActiveFilterSortDetail(LibraryFilterType.ALL, LibrarySortBy.UPDATED, false, label))
            .isEqualTo("Updated short")
    }

    @Test
    fun categorySelection_resetsPageAndCarriesCurrentFilterAndSort() {
        data class State(val category: String?, val filter: LibraryFilterType, val sort: LibrarySortBy, val loading: Boolean)
        val limit = MutableStateFlow(999)
        val filter = MutableStateFlow(LibraryFilterType.FAVORITES)
        val sort = MutableStateFlow(LibrarySortBy.RATING)
        val state = MutableStateFlow(State(null, LibraryFilterType.ALL, LibrarySortBy.LIBRARY, false))

        selectVodCategory("Drama", limit, filter, sort, state) { category, filterType, sortBy, loading ->
            copy(category = category, filter = filterType, sort = sortBy, loading = loading)
        }

        assertThat(limit.value).isEqualTo(VodBrowseDefaults.SELECTED_CATEGORY_PAGE_SIZE)
        assertThat(state.value).isEqualTo(State("Drama", LibraryFilterType.FAVORITES, LibrarySortBy.RATING, true))
    }

    @Test
    fun providerWideCategory_isKeptWhenItIsTheOnlyCategory() {
        val wildcard = "*"

        assertThat(
            keepNonProviderWideCategoriesOrAll(listOf(wildcard)) { it == wildcard }
        ).containsExactly(wildcard)
        assertThat(
            keepNonProviderWideCategoriesOrAll(listOf(wildcard, "Action")) { it == wildcard }
        ).containsExactly("Action")
    }

    @Test
    fun portalSearch_requiresTwoCharactersButDoesNotTreatShortQueryAsPortalSearch() {
        assertThat(isPortalVodSearchQuery(ProviderType.STALKER_PORTAL, true, "a")).isFalse()
        assertThat(isPortalVodSearchQuery(ProviderType.STALKER_PORTAL, true, "ab")).isTrue()
        assertThat(isPortalVodSearchQuery(ProviderType.XTREAM_CODES, true, "ab")).isFalse()
    }

    @Test
    fun portalSearchSnapshot_isRejectedAfterProviderChanges() {
        assertThat(portalVodSearchBelongsToProvider(7L, 7L)).isTrue()
        assertThat(portalVodSearchBelongsToProvider(7L, 8L)).isFalse()
        assertThat(portalVodSearchBelongsToProvider(null, 7L)).isFalse()
    }

    @Test
    fun incrementLoadLimit_isSuppressedWhenNoMoreItems() {
        val limit = MutableStateFlow(VodBrowseDefaults.SELECTED_CATEGORY_PAGE_SIZE)
        incrementVodSelectedCategoryLoadLimit(canLoadMore = false, selectedCategoryLoadLimit = limit)
        assertThat(limit.value).isEqualTo(VodBrowseDefaults.SELECTED_CATEGORY_PAGE_SIZE)
        incrementVodSelectedCategoryLoadLimit(canLoadMore = true, selectedCategoryLoadLimit = limit)
        assertThat(limit.value).isEqualTo(VodBrowseDefaults.SELECTED_CATEGORY_PAGE_SIZE * 2)
    }

    @Test
    fun groupMembership_normalizesVirtualAndStoredIds() {
        assertThat(matchesVodGroupMembership(7L, -7L)).isTrue()
        assertThat(matchesVodGroupMembership(-7L, 7L)).isTrue()
        assertThat(matchesVodGroupMembership(null, 7L)).isFalse()
        assertThat(matchesVodGroupMembership(7L, 8L)).isFalse()
    }

    @Test
    fun previewFavorites_preservesFavoritePositionWhenLoaderReturnsDifferentOrder() = runBlocking {
        val movieOne = Movie(id = 1L, name = "Movie One")
        val movieTwo = Movie(id = 2L, name = "Movie Two")
        val result = buildVodPreviewCatalog(
            allFavorites = listOf(
                Favorite(id = 1L, providerId = 1L, contentId = 1L, contentType = ContentType.MOVIE, position = 1024),
                Favorite(id = 2L, providerId = 1L, contentId = 2L, contentType = ContentType.MOVIE, position = 0)
            ),
            customCategories = emptyList(),
            providerCategories = emptyList(),
            providerCategoryCounts = emptyMap(),
            libraryCount = 2,
            hiddenProviderCategoryIds = emptySet(),
            loadItemsByIds = { listOf(movieOne, movieTwo) },
            providerPreviews = emptyMap(),
            itemIds = Movie::letId,
            itemCategoryId = Movie::categoryId,
            copyWithFavorite = { movie, isFavorite -> movie.copy(isFavorite = isFavorite) }
        )

        assertThat(result.grouped[VodBrowseDefaults.FAVORITES_CATEGORY].orEmpty().map(Movie::id))
            .containsExactly(2L, 1L)
            .inOrder()
    }
}

private fun Movie.letId(): List<Long> = listOf(id)
