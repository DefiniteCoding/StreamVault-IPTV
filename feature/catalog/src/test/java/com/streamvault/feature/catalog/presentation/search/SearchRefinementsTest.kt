package com.streamvault.feature.catalog.presentation.search

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.Series
import org.junit.Test

class SearchRefinementsTest {

    private fun movie(
        id: Long,
        name: String,
        year: String? = null,
        releaseDate: String? = null,
        genre: String? = null,
        rating: Float = 0f
    ) = Movie(id = id, name = name, year = year, releaseDate = releaseDate, genre = genre, rating = rating)

    private fun series(
        id: Long,
        name: String,
        releaseDate: String? = null,
        genre: String? = null,
        rating: Float = 0f
    ) = Series(id = id, name = name, releaseDate = releaseDate, genre = genre, rating = rating)

    private fun channel(id: Long, name: String, category: String? = null, withGuide: Boolean = false) = Channel(
        id = id,
        name = name,
        categoryName = category,
        currentProgram = if (withGuide) Program(id = id, channelId = "c$id", title = "Show", startTime = 0, endTime = 1) else null
    )

    @Test
    fun `relevance sort and empty filters leave results untouched`() {
        val set = SearchResultSet(movies = listOf(movie(1, "B"), movie(2, "A")))

        assertThat(set.refined(SearchRefinements()).movies.map { it.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `name sort is case insensitive and covers channels`() {
        val set = SearchResultSet(
            channels = listOf(channel(1, "cnn"), channel(2, "ABC")),
            movies = listOf(movie(3, "batman"), movie(4, "Alien"))
        )

        val result = set.refined(SearchRefinements(sort = SearchSort.NAME_ASC))

        assertThat(result.channels.map { it.name }).containsExactly("ABC", "cnn").inOrder()
        assertThat(result.movies.map { it.name }).containsExactly("Alien", "batman").inOrder()
    }

    @Test
    fun `newest sort uses release date then year and puts undated items last`() {
        val set = SearchResultSet(
            movies = listOf(
                movie(1, "undated"),
                movie(2, "old", releaseDate = "1992-06-19"),
                movie(3, "new", releaseDate = "2022-03-04"),
                movie(4, "year only", year = "2010")
            )
        )

        val result = set.refined(SearchRefinements(sort = SearchSort.NEWEST))

        assertThat(result.movies.map { it.id }).containsExactly(3L, 4L, 2L, 1L).inOrder()
    }

    @Test
    fun `rating sort is descending and keeps channel order`() {
        val set = SearchResultSet(
            channels = listOf(channel(1, "Z"), channel(2, "A")),
            series = listOf(series(3, "low", rating = 5f), series(4, "high", rating = 8.5f))
        )

        val result = set.refined(SearchRefinements(sort = SearchSort.RATING))

        assertThat(result.series.map { it.id }).containsExactly(4L, 3L).inOrder()
        assertThat(result.channels.map { it.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `decade filter buckets years before 1990 as older`() {
        val set = SearchResultSet(
            movies = listOf(
                movie(1, "a", year = "2021"),
                movie(2, "b", year = "1985"),
                movie(3, "c", releaseDate = "2014-01-01"),
                movie(4, "no year")
            )
        )

        val recent = set.refined(SearchRefinements(filters = SearchFilters(decades = setOf(2020))))
        val older = set.refined(SearchRefinements(filters = SearchFilters(decades = setOf(OLDER_DECADE))))

        assertThat(recent.movies.map { it.id }).containsExactly(1L)
        assertThat(older.movies.map { it.id }).containsExactly(2L)
    }

    @Test
    fun `genre filter matches any selected genre ignoring case and csv spacing`() {
        val set = SearchResultSet(
            movies = listOf(
                movie(1, "a", genre = "Action, Crime"),
                movie(2, "b", genre = "drama"),
                movie(3, "c", genre = null)
            ),
            series = listOf(series(4, "d", genre = "Crime,Drama"))
        )

        val result = set.refined(SearchRefinements(filters = SearchFilters(genres = setOf("crime"))))

        assertThat(result.movies.map { it.id }).containsExactly(1L)
        assertThat(result.series.map { it.id }).containsExactly(4L)
    }

    @Test
    fun `rating filter keeps items at or above the threshold`() {
        val set = SearchResultSet(movies = listOf(movie(1, "a", rating = 6f), movie(2, "b", rating = 5.9f), movie(3, "c")))

        val result = set.refined(SearchRefinements(filters = SearchFilters(minRating = 6)))

        assertThat(result.movies.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `filters combine with AND across groups`() {
        val set = SearchResultSet(
            movies = listOf(
                movie(1, "a", year = "2021", genre = "Action"),
                movie(2, "b", year = "2021", genre = "Drama"),
                movie(3, "c", year = "2005", genre = "Action")
            )
        )

        val result = set.refined(
            SearchRefinements(filters = SearchFilters(decades = setOf(2020), genres = setOf("Action")))
        )

        assertThat(result.movies.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `vod filters leave channels alone and channel filters leave vod alone`() {
        val set = SearchResultSet(
            channels = listOf(channel(1, "a", category = "News", withGuide = true), channel(2, "b", category = "Sports")),
            movies = listOf(movie(3, "m", genre = "Action"))
        )

        val vodOnly = set.refined(SearchRefinements(filters = SearchFilters(genres = setOf("Drama"))))
        val channelOnly = set.refined(SearchRefinements(filters = SearchFilters(categories = setOf("news"), guideOnly = true)))

        assertThat(vodOnly.channels).hasSize(2)
        assertThat(vodOnly.movies).isEmpty()
        assertThat(channelOnly.channels.map { it.id }).containsExactly(1L)
        assertThat(channelOnly.movies).hasSize(1)
    }

    @Test
    fun `active count sums selected values`() {
        val filters = SearchFilters(
            decades = setOf(2020, 2010),
            genres = setOf("Action"),
            minRating = 7,
            guideOnly = true
        )

        assertThat(filters.activeCount).isEqualTo(5)
        assertThat(SearchFilters().isActive).isFalse()
    }

    @Test
    fun `genre options are the top 12 by frequency and merge casing`() {
        val movies = (1..14).flatMap { i -> List(i) { n -> movie(i * 100L + n, "m", genre = "G$i") } } +
            movie(9000, "x", genre = "g14")

        val options = SearchResultSet(movies = movies).filterOptions()

        assertThat(options.genres).hasSize(12)
        assertThat(options.genres.first()).isEqualTo("G14")
        assertThat(options.genres).doesNotContain("G1")
    }

    @Test
    fun `selected values stay in the options even when absent from results`() {
        val set = SearchResultSet(movies = listOf(movie(1, "a", year = "2021", genre = "Action")))

        val options = set.filterOptions(SearchFilters(decades = setOf(1990), genres = setOf("Western")))

        assertThat(options.decades).containsExactly(2020, 1990).inOrder()
        assertThat(options.genres).containsExactly("Action", "Western").inOrder()
    }

    @Test
    fun `groups without data are empty so the sheet can hide them`() {
        val options = SearchResultSet(movies = listOf(movie(1, "a")), channels = listOf(channel(2, "c"))).filterOptions()

        assertThat(options.decades).isEmpty()
        assertThat(options.genres).isEmpty()
        assertThat(options.hasRatings).isFalse()
        assertThat(options.categories).isEmpty()
        assertThat(options.hasGuide).isFalse()
    }

    @Test
    fun `older decade sorts after the real decades`() {
        val set = SearchResultSet(
            movies = listOf(movie(1, "a", year = "1985"), movie(2, "b", year = "2021"), movie(3, "c", year = "1999"))
        )

        assertThat(set.filterOptions().decades).containsExactly(2020, 1990, OLDER_DECADE).inOrder()
    }
}
