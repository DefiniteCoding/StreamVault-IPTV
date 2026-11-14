package com.streamvault.feature.catalog.presentation.movies

import com.streamvault.domain.model.Movie
import com.streamvault.domain.repository.MovieRepository
import kotlinx.coroutines.delay

/**
 * The hero's view of a movie: the full local row, plus one provider details call (saved by the repository) when the
 * plot is missing and focus stays put. The shelf rows load a slim projection without a plot.
 *
 * [resolve] is cancelled by its caller when focus moves on, so scrolling past a title costs no provider traffic.
 */
internal class HeroMovieResolver(
    private val movieRepository: MovieRepository,
    private val dwellMs: Long = DEFAULT_DWELL_MS
) {
    // Titles already requested this session, so a title the provider has no plot for is not asked again on every focus.
    private val attempted = mutableSetOf<Long>()

    suspend fun resolve(movie: Movie): Movie {
        val local = movieRepository.getMovie(movie.id) ?: return movie
        if (!local.plot.isNullOrBlank() || local.providerId <= 0L) return local
        delay(dwellMs)
        if (!attempted.add(movie.id)) return local
        return movieRepository.getMovieDetails(local.providerId, movie.id).getOrNull() ?: local
    }

    companion object {
        const val DEFAULT_DWELL_MS = 750L
    }
}
