package com.streamvault.domain.usecase

import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Result
import com.streamvault.domain.repository.PlaybackHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Takes one card off the Continue watching shelf.
 *
 * A series shows as one card for its latest unfinished episode, so removing only that episode's row would let an
 * older unfinished episode take the card's place. Every history row of the series is removed instead.
 */
class RemoveContinueWatchingItem @Inject constructor(
    private val playbackHistoryRepository: PlaybackHistoryRepository
) {
    suspend operator fun invoke(history: PlaybackHistory): Result<Unit> {
        val seriesKey = history.seriesKey() ?: return remove(history)
        val rows = playbackHistoryRepository
            .getRecentlyWatchedByProvider(history.providerId, SERIES_LOOKUP_LIMIT)
            .first()
            .filter { it.seriesKey() == seriesKey }
        // The card's own row is always removed, even if it fell outside the lookup window.
        val targets = (rows + history).distinctBy { it.contentType to it.contentId }
        for (target in targets) {
            val result = remove(target)
            if (result is Result.Error) return result
        }
        return Result.success(Unit)
    }

    private suspend fun remove(history: PlaybackHistory): Result<Unit> =
        playbackHistoryRepository.removeFromHistory(history.contentId, history.contentType, history.providerId)

    private fun PlaybackHistory.seriesKey(): Long? = when (contentType) {
        ContentType.SERIES,
        ContentType.SERIES_EPISODE -> seriesId?.takeIf { it > 0L } ?: contentId
        else -> null
    }

    private companion object {
        // Matches the widest history window the shelves read.
        const val SERIES_LOOKUP_LIMIT = 500
    }
}
