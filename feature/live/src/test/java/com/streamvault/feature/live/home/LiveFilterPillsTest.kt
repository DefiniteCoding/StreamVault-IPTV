package com.streamvault.feature.live.home

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.LiveTvQuickFilterVisibilityMode
import com.streamvault.domain.model.VirtualCategoryIds
import org.junit.Test

class LiveFilterPillsTest {
    private fun category(id: Long) = Category(id = id, name = "c$id", type = ContentType.LIVE)

    @Test
    fun sidebarListDropsOnlyFavoritesAndRecent() {
        val categories = listOf(
            category(VirtualCategoryIds.FAVORITES),
            category(VirtualCategoryIds.RECENT),
            category(7L),
            category(9L)
        )

        assertThat(categories.withoutFilterPillCategories().map { it.id }).containsExactly(7L, 9L).inOrder()
    }

    @Test
    fun selectedKeyFollowsPillCategoryThenQuickFilter() {
        assertThat(filterPillSelectedKey(VirtualCategoryIds.RECENT, null))
            .isEqualTo(VirtualCategoryIds.RECENT.toString())
        assertThat(filterPillSelectedKey(VirtualCategoryIds.FAVORITES, "Movies"))
            .isEqualTo(VirtualCategoryIds.FAVORITES.toString())
        assertThat(filterPillSelectedKey(7L, "Movies")).isEqualTo("quick_filters")
    }

    @Test
    fun noPillIsSelectedForProviderCategoryWithoutFilter() {
        assertThat(filterPillSelectedKey(7L, null)).isNull()
        assertThat(filterPillSelectedKey(null, null)).isNull()
    }

    @Test
    fun quickFiltersPillFollowsVisibilityMode() {
        assertThat(shouldShowQuickFiltersPill(LiveTvQuickFilterVisibilityMode.HIDE, listOf("Movies"))).isFalse()
        assertThat(shouldShowQuickFiltersPill(LiveTvQuickFilterVisibilityMode.SHOW_WHEN_FILTERS_AVAILABLE, emptyList())).isFalse()
        assertThat(shouldShowQuickFiltersPill(LiveTvQuickFilterVisibilityMode.SHOW_WHEN_FILTERS_AVAILABLE, listOf("Movies"))).isTrue()
        assertThat(shouldShowQuickFiltersPill(LiveTvQuickFilterVisibilityMode.ALWAYS_VISIBLE, emptyList())).isTrue()
    }
}
