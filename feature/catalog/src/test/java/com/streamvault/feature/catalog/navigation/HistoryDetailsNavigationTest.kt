package com.streamvault.feature.catalog.navigation

import com.google.common.truth.Truth.assertThat
import com.streamvault.core.navigation.AppDestination
import com.streamvault.core.navigation.NavigationActions
import com.streamvault.core.navigation.NavigationOptions
import com.streamvault.core.navigation.PlayerNavigationRequest
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import org.junit.Test

class HistoryDetailsNavigationTest {

    private class RecordingActions : NavigationActions {
        val destinations = mutableListOf<AppDestination>()
        override fun navigate(destination: AppDestination, options: NavigationOptions) {
            destinations += destination
        }
        override fun openPlayer(request: PlayerNavigationRequest) = Unit
        override fun back(): Boolean = false
        override fun returnTo(destination: AppDestination?): Boolean = false
    }

    private fun history(type: ContentType, contentId: Long, seriesId: Long? = null) = PlaybackHistory(
        contentId = contentId,
        contentType = type,
        providerId = 1L,
        title = "Title",
        streamUrl = "http://example.test/stream",
        seriesId = seriesId
    )

    @Test
    fun `movie history opens the movie detail by its content id`() {
        val actions = RecordingActions()

        openHistoryDetails(actions, history(ContentType.MOVIE, contentId = 42L), AppDestination.Movies)

        assertThat(actions.destinations).containsExactly(AppDestination.MovieDetail(42L, AppDestination.Movies))
    }

    @Test
    fun `episode history opens its parent series, not the episode id`() {
        val actions = RecordingActions()

        openHistoryDetails(
            actions,
            history(ContentType.SERIES_EPISODE, contentId = 900L, seriesId = 7L),
            AppDestination.Home
        )

        assertThat(actions.destinations).containsExactly(AppDestination.SeriesDetail(7L, AppDestination.Home))
    }

    @Test
    fun `series history without a series id falls back to its content id`() {
        val actions = RecordingActions()

        openHistoryDetails(actions, history(ContentType.SERIES, contentId = 55L), AppDestination.Home)

        assertThat(actions.destinations).containsExactly(AppDestination.SeriesDetail(55L, AppDestination.Home))
    }

    @Test
    fun `live history has no detail page`() {
        val actions = RecordingActions()

        openHistoryDetails(actions, history(ContentType.LIVE, contentId = 3L), AppDestination.Home)

        assertThat(actions.destinations).isEmpty()
    }
}
