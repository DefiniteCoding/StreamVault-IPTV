package com.streamvault.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.streamvault.data.local.RoomDatabaseTransactionRunner
import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.data.mapper.toEntity
import com.streamvault.data.preferences.PreferencesCorruptionRecovery
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.ProviderType
import com.streamvault.domain.usecase.ContinueWatchingResult
import com.streamvault.domain.usecase.GetContinueWatching
import com.streamvault.domain.usecase.RemoveContinueWatchingItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The Home remove path end to end on a real Room database: [RemoveContinueWatchingItem] through
 * [PlaybackHistoryRepositoryImpl] to the DAO, read back through the same [GetContinueWatching] the Home shelves use.
 */
@RunWith(AndroidJUnit4::class)
class RemoveContinueWatchingIntegrationTest {
    private lateinit var db: StreamVaultDatabase
    private lateinit var repository: PlaybackHistoryRepositoryImpl

    @Before
    fun createDb() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, StreamVaultDatabase::class.java).build()
        db.providerDao().insert(ProviderEntity(id = PROVIDER, name = "Provider", type = ProviderType.M3U))
        repository = PlaybackHistoryRepositoryImpl(
            dao = db.playbackHistoryDao(),
            preferencesRepository = preferencesRepository(context, db),
            movieDao = db.movieDao(),
            episodeDao = db.episodeDao(),
            transactionRunner = RoomDatabaseTransactionRunner(db)
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun removing_a_movie_card_drops_it_from_the_shelf_and_the_table() = runTest {
        seed(movie(contentId = 1L, lastWatchedAt = 2_000L), movie(contentId = 2L, lastWatchedAt = 1_000L))
        assertThat(shelfIds()).containsExactly(1L, 2L).inOrder()

        val card = shelf().first { it.contentId == 1L }
        assertThat(RemoveContinueWatchingItem(repository)(card).isSuccess).isTrue()

        assertThat(shelfIds()).containsExactly(2L)
        assertThat(db.playbackHistoryDao().get(1L, ContentType.MOVIE.name, PROVIDER)).isNull()
    }

    @Test
    fun removing_a_series_card_does_not_let_an_older_unfinished_episode_take_its_place() = runTest {
        seed(
            episode(contentId = 11L, seriesId = 5L, lastWatchedAt = 3_000L),
            episode(contentId = 10L, seriesId = 5L, lastWatchedAt = 1_000L),
            episode(contentId = 20L, seriesId = 6L, lastWatchedAt = 2_000L)
        )
        // One card per series: the latest unfinished episode of each.
        assertThat(shelfIds()).containsExactly(11L, 20L).inOrder()

        val card = shelf().first { it.contentId == 11L }
        assertThat(RemoveContinueWatchingItem(repository)(card).isSuccess).isTrue()

        assertThat(shelfIds()).containsExactly(20L)
        assertThat(db.playbackHistoryDao().get(10L, ContentType.SERIES_EPISODE.name, PROVIDER)).isNull()
    }

    private suspend fun seed(vararg rows: PlaybackHistory) =
        rows.forEach { db.playbackHistoryDao().insertOrUpdate(it.toEntity()) }

    private suspend fun shelf(): List<PlaybackHistory> {
        val result = GetContinueWatching(repository)(PROVIDER).first()
        return (result as ContinueWatchingResult.Items).items
    }

    private suspend fun shelfIds() = shelf().map { it.contentId }

    private fun movie(contentId: Long, lastWatchedAt: Long) = PlaybackHistory(
        contentId = contentId,
        contentType = ContentType.MOVIE,
        providerId = PROVIDER,
        title = "Movie $contentId",
        streamUrl = "https://provider.example.com/movie/$contentId",
        resumePositionMs = 600_000L,
        totalDurationMs = 6_000_000L,
        lastWatchedAt = lastWatchedAt
    )

    private fun episode(contentId: Long, seriesId: Long, lastWatchedAt: Long) = PlaybackHistory(
        contentId = contentId,
        contentType = ContentType.SERIES_EPISODE,
        providerId = PROVIDER,
        title = "Episode $contentId",
        streamUrl = "https://provider.example.com/episode/$contentId",
        resumePositionMs = 600_000L,
        totalDurationMs = 3_000_000L,
        lastWatchedAt = lastWatchedAt,
        seriesId = seriesId,
        seasonNumber = 1,
        episodeNumber = (contentId % 10).toInt()
    )

    private companion object {
        const val PROVIDER = 1L

        // DataStore allows one instance per file for the whole process, so every test shares this one.
        private var sharedPreferences: PreferencesRepository? = null

        @Synchronized
        fun preferencesRepository(context: Context, db: StreamVaultDatabase): PreferencesRepository =
            sharedPreferences ?: PreferencesRepository(
                context = context,
                channelPreferenceDao = db.channelPreferenceDao(),
                playbackQualityPreferenceDao = db.playbackQualityPreferenceDao(),
                searchHistoryDao = db.searchHistoryDao(),
                corruptionRecovery = PreferencesCorruptionRecovery(context)
            ).also { sharedPreferences = it }
    }
}
