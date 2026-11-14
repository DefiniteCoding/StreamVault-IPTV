package com.streamvault.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Result
import com.streamvault.domain.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RemoveContinueWatchingItemTest {

    @Test
    fun movie_removal_deletes_only_that_row() = runTest {
        val movie = history(contentId = 1L, type = ContentType.MOVIE)
        val other = history(contentId = 2L, type = ContentType.MOVIE)
        val repository = FakeRepository(listOf(movie, other))

        val result = RemoveContinueWatchingItem(repository)(movie)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.removed).containsExactly(ContentType.MOVIE to 1L)
    }

    @Test
    fun series_removal_deletes_every_row_of_the_series_so_an_older_episode_cannot_take_the_card() = runTest {
        val latest = history(contentId = 11L, type = ContentType.SERIES_EPISODE, seriesId = 5L)
        val older = history(contentId = 10L, type = ContentType.SERIES_EPISODE, seriesId = 5L)
        val otherSeries = history(contentId = 20L, type = ContentType.SERIES_EPISODE, seriesId = 6L)
        val repository = FakeRepository(listOf(latest, older, otherSeries))

        RemoveContinueWatchingItem(repository)(latest)

        assertThat(repository.removed).containsExactly(
            ContentType.SERIES_EPISODE to 11L,
            ContentType.SERIES_EPISODE to 10L
        )
    }

    @Test
    fun series_row_without_a_series_id_matches_episodes_by_its_own_content_id() = runTest {
        val seriesRow = history(contentId = 5L, type = ContentType.SERIES)
        val episode = history(contentId = 10L, type = ContentType.SERIES_EPISODE, seriesId = 5L)
        val repository = FakeRepository(listOf(seriesRow, episode))

        RemoveContinueWatchingItem(repository)(seriesRow)

        assertThat(repository.removed).containsExactly(
            ContentType.SERIES to 5L,
            ContentType.SERIES_EPISODE to 10L
        )
    }

    @Test
    fun the_cards_own_row_is_removed_even_when_the_history_window_no_longer_lists_it() = runTest {
        val card = history(contentId = 11L, type = ContentType.SERIES_EPISODE, seriesId = 5L)
        val repository = FakeRepository(emptyList())

        RemoveContinueWatchingItem(repository)(card)

        assertThat(repository.removed).containsExactly(ContentType.SERIES_EPISODE to 11L)
    }

    @Test
    fun a_failed_removal_stops_and_is_returned() = runTest {
        val latest = history(contentId = 11L, type = ContentType.SERIES_EPISODE, seriesId = 5L)
        val older = history(contentId = 10L, type = ContentType.SERIES_EPISODE, seriesId = 5L)
        val repository = FakeRepository(listOf(latest, older), failOnContentId = 11L)

        val result = RemoveContinueWatchingItem(repository)(latest)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat(repository.removed).isEmpty()
    }

    private fun history(contentId: Long, type: ContentType, seriesId: Long? = null) = PlaybackHistory(
        contentId = contentId,
        contentType = type,
        providerId = 1L,
        title = "Title $contentId",
        streamUrl = "https://example.com/$contentId",
        resumePositionMs = 10_000L,
        totalDurationMs = 100_000L,
        lastWatchedAt = 1L,
        seriesId = seriesId
    )

    private class FakeRepository(
        private val rows: List<PlaybackHistory>,
        private val failOnContentId: Long? = null
    ) : PlaybackHistoryRepository {
        val removed = mutableListOf<Pair<ContentType, Long>>()

        override fun getRecentlyWatched(limit: Int): Flow<List<PlaybackHistory>> = flowOf(rows)
        override fun getRecentlyWatchedByProvider(providerId: Long, limit: Int): Flow<List<PlaybackHistory>> = flowOf(rows)
        override fun getRecentlyWatchedByProviders(providerIds: Set<Long>, limit: Int): Flow<List<PlaybackHistory>> = flowOf(rows)
        override fun getUnwatchedCount(providerId: Long, seriesId: Long): Flow<Int> = flowOf(0)
        override suspend fun getPlaybackHistory(
            contentId: Long,
            contentType: ContentType,
            providerId: Long,
            seriesId: Long?,
            seasonNumber: Int?,
            episodeNumber: Int?
        ): PlaybackHistory? = null
        override suspend fun markAsWatched(history: PlaybackHistory) = Result.success(Unit)
        override suspend fun recordPlayback(history: PlaybackHistory) = Result.success(Unit)
        override suspend fun updateResumePosition(history: PlaybackHistory) = Result.success(Unit)
        override suspend fun flushPendingProgress() = Result.success(Unit)
        override suspend fun removeFromHistory(contentId: Long, contentType: ContentType, providerId: Long): Result<Unit> {
            if (contentId == failOnContentId) return Result.error("boom")
            removed += contentType to contentId
            return Result.success(Unit)
        }
        override suspend fun clearAllHistory() = Result.success(Unit)
        override suspend fun clearHistoryForProvider(providerId: Long) = Result.success(Unit)
        override suspend fun clearLiveHistoryForProvider(providerId: Long) = Result.success(Unit)
    }
}
