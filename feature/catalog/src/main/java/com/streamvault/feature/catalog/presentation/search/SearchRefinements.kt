package com.streamvault.feature.catalog.presentation.search

import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

enum class SearchSort { RELEVANCE, NAME_ASC, NEWEST, RATING }

/**
 * Decade filter values are the decade start year (2020, 2010, ...). Years before [OLDER_DECADE_CUTOFF]
 * share one bucket so the chip row stays short on a D-pad.
 */
data class SearchFilters(
    val decades: Set<Int> = emptySet(),
    val genres: Set<String> = emptySet(),
    val minRating: Int = 0,
    val categories: Set<String> = emptySet(),
    val guideOnly: Boolean = false
) {
    val activeCount: Int
        get() = decades.size + genres.size + categories.size +
            (if (minRating > 0) 1 else 0) + (if (guideOnly) 1 else 0)
    val isActive: Boolean get() = activeCount > 0
}

data class SearchRefinements(
    val sort: SearchSort = SearchSort.RELEVANCE,
    val filters: SearchFilters = SearchFilters()
)

data class SearchResultSet(
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList()
) {
    val total: Int get() = channels.size + movies.size + series.size
}

/** Choices offered in the filter sheet. A group with no entries has no data and is not shown. */
data class SearchFilterOptions(
    val decades: List<Int> = emptyList(),
    val genres: List<String> = emptyList(),
    val hasRatings: Boolean = false,
    val categories: List<String> = emptyList(),
    val hasGuide: Boolean = false
)

const val OLDER_DECADE = 0
private const val OLDER_DECADE_CUTOFF = 1990
private const val MAX_GENRE_OPTIONS = 12
private const val MAX_CATEGORY_OPTIONS = 12
val SearchRatingThresholds = listOf(6, 7, 8)

private val yearPattern = Regex("""(?<!\d)(\d{4})(?:-(\d{2})-(\d{2}))?""")

internal fun parseYear(releaseDate: String?, year: String? = null): Int? {
    val fromYear = year?.let { yearPattern.find(it)?.groupValues?.get(1)?.toIntOrNull() }
    val fromDate = releaseDate?.let { yearPattern.find(it)?.groupValues?.get(1)?.toIntOrNull() }
    return (fromYear ?: fromDate)?.takeIf { it in 1800..2200 }
}

/** Sortable yyyyMMdd value; items with no usable date get 0 and sort last. */
internal fun releaseSortKey(releaseDate: String?, year: String? = null): Int {
    val match = releaseDate?.let { yearPattern.find(it) }
    val dateYear = match?.groupValues?.get(1)?.toIntOrNull()
    if (match != null && dateYear != null && dateYear in 1800..2200) {
        val month = match.groupValues[2].toIntOrNull() ?: 0
        val day = match.groupValues[3].toIntOrNull() ?: 0
        return dateYear * 10_000 + month * 100 + day
    }
    return (parseYear(null, year) ?: 0) * 10_000
}

fun decadeOf(year: Int): Int = if (year < OLDER_DECADE_CUTOFF) OLDER_DECADE else year / 10 * 10

internal fun parseGenres(genre: String?): List<String> =
    genre.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }

private fun Movie.yearValue(): Int? = parseYear(releaseDate, year)
private fun Series.yearValue(): Int? = parseYear(releaseDate)

private fun matchesVod(
    filters: SearchFilters,
    year: Int?,
    genre: String?,
    rating: Float
): Boolean {
    if (filters.decades.isNotEmpty() && (year == null || decadeOf(year) !in filters.decades)) return false
    if (filters.genres.isNotEmpty()) {
        val itemGenres = parseGenres(genre)
        if (filters.genres.none { wanted -> itemGenres.any { it.equals(wanted, ignoreCase = true) } }) return false
    }
    if (filters.minRating > 0 && rating < filters.minRating) return false
    return true
}

private fun matchesChannel(filters: SearchFilters, channel: Channel): Boolean {
    if (filters.categories.isNotEmpty() &&
        filters.categories.none { it.equals(channel.categoryName?.trim(), ignoreCase = true) }
    ) return false
    if (filters.guideOnly && channel.currentProgram == null) return false
    return true
}

/**
 * Filter groups only act on sections whose model carries the field: year, genre and rating narrow
 * movies and series, category and guide narrow channels. Sort is stable, so equal keys keep relevance order.
 */
fun SearchResultSet.refined(refinements: SearchRefinements): SearchResultSet {
    val filters = refinements.filters
    val filteredChannels = channels.filter { matchesChannel(filters, it) }
    val filteredMovies = movies.filter { matchesVod(filters, it.yearValue(), it.genre, it.rating) }
    val filteredSeries = series.filter { matchesVod(filters, it.yearValue(), it.genre, it.rating) }
    val sort = refinements.sort
    return SearchResultSet(
        // Channels carry no date or rating, so only A-Z reorders them.
        channels = if (sort == SearchSort.NAME_ASC) {
            filteredChannels.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        } else {
            filteredChannels
        },
        movies = filteredMovies.sortedMovies(sort),
        series = filteredSeries.sortedSeries(sort)
    )
}

private fun List<Movie>.sortedMovies(sort: SearchSort): List<Movie> = when (sort) {
    SearchSort.RELEVANCE -> this
    SearchSort.NAME_ASC -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    SearchSort.NEWEST -> sortedByDescending { releaseSortKey(it.releaseDate, it.year) }
    SearchSort.RATING -> sortedByDescending { it.rating }
}

private fun List<Series>.sortedSeries(sort: SearchSort): List<Series> = when (sort) {
    SearchSort.RELEVANCE -> this
    SearchSort.NAME_ASC -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    SearchSort.NEWEST -> sortedByDescending { releaseSortKey(it.releaseDate) }
    SearchSort.RATING -> sortedByDescending { it.rating }
}

/**
 * Options come from the unfiltered results so a chip never disappears because another chip is on.
 * Values already selected stay listed, so a filter kept across queries is always visible and removable.
 */
fun SearchResultSet.filterOptions(selected: SearchFilters = SearchFilters()): SearchFilterOptions {
    val years = movies.mapNotNull { it.yearValue() } + series.mapNotNull { it.yearValue() }
    val decades = (years.map(::decadeOf).toSet() + selected.decades)
        .sortedWith(compareBy { if (it == OLDER_DECADE) Int.MAX_VALUE else -it })

    val genreCounts = LinkedHashMap<String, Pair<String, Int>>()
    (movies.map { it.genre } + series.map { it.genre }).forEach { raw ->
        parseGenres(raw).forEach { name ->
            val key = name.lowercase()
            val current = genreCounts[key]
            genreCounts[key] = (current?.first ?: name) to ((current?.second ?: 0) + 1)
        }
    }
    val topGenres = genreCounts.values
        .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.first })
        .take(MAX_GENRE_OPTIONS)
        .map { it.first }
    val genres = topGenres + selected.genres.filter { picked -> topGenres.none { it.equals(picked, ignoreCase = true) } }

    val categoryCounts = channels
        .mapNotNull { it.categoryName?.trim()?.takeIf(String::isNotEmpty) }
        .groupingBy { it.lowercase() }
        .eachCount()
    val categoryNames = channels
        .mapNotNull { it.categoryName?.trim()?.takeIf(String::isNotEmpty) }
        .distinctBy { it.lowercase() }
    val topCategories = categoryNames
        .sortedWith(compareByDescending<String> { categoryCounts[it.lowercase()] ?: 0 }.thenBy(String.CASE_INSENSITIVE_ORDER) { it })
        .take(MAX_CATEGORY_OPTIONS)
    val categories = topCategories + selected.categories.filter { picked -> topCategories.none { it.equals(picked, ignoreCase = true) } }

    return SearchFilterOptions(
        decades = decades,
        genres = genres,
        hasRatings = movies.any { it.rating > 0f } || series.any { it.rating > 0f },
        categories = categories,
        hasGuide = channels.any { it.currentProgram != null }
    )
}
