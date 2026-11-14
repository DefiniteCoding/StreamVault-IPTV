package com.streamvault.feature.catalog.presentation.movies

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Result
import com.streamvault.domain.repository.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HeroMovieResolverTest {
    private val dwellMs = 750L
    private val repository = mock<MovieRepository>()
    private val resolver = HeroMovieResolver(repository, dwellMs)

    private fun movie(plot: String? = null, providerId: Long = 7L) =
        Movie(id = 1L, name = "Title", plot = plot, providerId = providerId)

    @Test
    fun localRowWithAPlotIsReturnedWithoutAProviderCall() = runTest {
        val local = movie(plot = "Saved plot")
        whenever(repository.getMovie(1L)).thenReturn(local)

        assertThat(resolver.resolve(movie())).isEqualTo(local)

        verify(repository, never()).getMovieDetails(7L, 1L, null)
    }

    @Test
    fun missingLocalRowReturnsTheListItemUntouched() = runTest {
        val listItem = movie()
        whenever(repository.getMovie(1L)).thenReturn(null)

        assertThat(resolver.resolve(listItem)).isEqualTo(listItem)

        verify(repository, never()).getMovieDetails(7L, 1L, null)
    }

    @Test
    fun missingPlotIsFetchedOnceFocusHasRested() = runTest {
        val enriched = movie(plot = "From the provider")
        whenever(repository.getMovie(1L)).thenReturn(movie())
        whenever(repository.getMovieDetails(7L, 1L, null)).thenReturn(Result.Success(enriched))

        assertThat(resolver.resolve(movie())).isEqualTo(enriched)
        verify(repository, times(1)).getMovieDetails(7L, 1L, null)
    }

    @Test
    fun cancelledDuringTheWaitNeverReachesTheProviderAndIsNotRemembered() = runTest {
        whenever(repository.getMovie(1L)).thenReturn(movie())
        whenever(repository.getMovieDetails(7L, 1L, null)).thenReturn(Result.Success(movie(plot = "Later")))

        val job = launch { resolver.resolve(movie()) }
        runCurrent()
        advanceTimeBy(dwellMs - 1)
        job.cancel()
        runCurrent()
        verify(repository, never()).getMovieDetails(7L, 1L, null)

        // Focus returns later: the title still gets its one request.
        assertThat(resolver.resolve(movie()).plot).isEqualTo("Later")
        verify(repository, times(1)).getMovieDetails(7L, 1L, null)
    }

    @Test
    fun aTitleIsAskedOnlyOncePerSessionEvenWhenTheProviderHasNoPlot() = runTest {
        val local = movie()
        whenever(repository.getMovie(1L)).thenReturn(local)
        whenever(repository.getMovieDetails(7L, 1L, null)).thenReturn(Result.Success(local))

        resolver.resolve(movie())
        resolver.resolve(movie())

        verify(repository, times(1)).getMovieDetails(7L, 1L, null)
    }

    @Test
    fun providerErrorFallsBackToTheLocalRow() = runTest {
        val local = movie()
        whenever(repository.getMovie(1L)).thenReturn(local)
        whenever(repository.getMovieDetails(7L, 1L, null)).thenReturn(Result.Error("offline"))

        assertThat(resolver.resolve(movie())).isEqualTo(local)
    }

    @Test
    fun rowWithoutAProviderIsNeverSentToTheProvider() = runTest {
        val local = movie(providerId = 0L)
        whenever(repository.getMovie(1L)).thenReturn(local)

        assertThat(resolver.resolve(movie())).isEqualTo(local)

        verify(repository, never()).getMovieDetails(0L, 1L, null)
    }
}
