package com.streamvault.feature.catalog.presentation.components

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series
import org.junit.Test

class VodHeroMappingTest {

    @Test
    fun `first genre takes the first entry of a comma or slash list`() {
        assertThat(firstGenre("Drama, Thriller")).isEqualTo("Drama")
        assertThat(firstGenre("Comedy / Romance")).isEqualTo("Comedy")
        assertThat(firstGenre(" , Drama")).isNull()
        assertThat(firstGenre(null)).isNull()
    }

    @Test
    fun `release year picks the first four digit run`() {
        assertThat(releaseYear("2021-07-04")).isEqualTo("2021")
        assertThat(releaseYear("July 4, 1999")).isEqualTo("1999")
        assertThat(releaseYear("n/a")).isNull()
        assertThat(releaseYear(null)).isNull()
    }

    @Test
    fun `hero metadata skips a zero rating and blank parts`() {
        assertThat(heroMetadata(rating = 0f, year = " ", genre = null)).isEmpty()
        assertThat(heroMetadata(rating = 7.5f, year = "2020", genre = "Action, Drama"))
            .containsExactly(formatVodRatingLabel(7.5f), "2020", "Action")
            .inOrder()
    }

    @Test
    fun `movie hero prefers the year field over the release date and trims the plot`() {
        val hero = Movie(
            id = 1,
            name = "Title",
            categoryName = "Action",
            plot = "  A plot.  ",
            year = "2018",
            releaseDate = "2020-01-01",
            posterUrl = "   "
        ).toVodHeroModel()

        assertThat(hero.kicker).isEqualTo("Action")
        assertThat(hero.plot).isEqualTo("A plot.")
        assertThat(hero.metadata).containsExactly("2018")
        assertThat(hero.posterUrl).isNull()
    }

    @Test
    fun `movie hero falls back to the release date year`() {
        val hero = Movie(id = 1, name = "Title", releaseDate = "2020-01-01").toVodHeroModel()

        assertThat(hero.metadata).containsExactly("2020")
        assertThat(hero.kicker).isEmpty()
    }

    @Test
    fun `series hero maps poster and metadata`() {
        val hero = Series(
            id = 2,
            name = "Show",
            posterUrl = "https://img/poster.jpg",
            genre = "Crime/Drama",
            releaseDate = "2015-03-01"
        ).toVodHeroModel()

        assertThat(hero.posterUrl).isEqualTo("https://img/poster.jpg")
        assertThat(hero.metadata).containsExactly("2015", "Crime").inOrder()
    }

    @Test
    fun `parental levels 1 and 2 lock adult or protected titles in a locked category`() {
        listOf(1, 2).forEach { level ->
            assertThat(lock(isAdult = true, level = level)).isTrue()
            assertThat(lock(isUserProtected = true, level = level)).isTrue()
            assertThat(lock(level = level)).isFalse()
        }
    }

    @Test
    fun `other parental levels never lock at title level`() {
        listOf(0, 3).forEach { level ->
            assertThat(lock(isAdult = true, level = level)).isFalse()
        }
    }

    @Test
    fun `an unlocked category opens its titles, matching negative provider ids by magnitude`() {
        assertThat(lock(isAdult = true, categoryId = 7L, unlocked = setOf(7L))).isFalse()
        assertThat(lock(isAdult = true, categoryId = -7L, unlocked = setOf(7L))).isFalse()
        assertThat(lock(isAdult = true, categoryId = 8L, unlocked = setOf(7L))).isTrue()
    }

    @Test
    fun `a title without a category stays locked`() {
        assertThat(lock(isAdult = true, categoryId = null, unlocked = setOf(7L))).isTrue()
    }

    @Test
    fun `hero skips locked titles across rows and returns null when all are locked`() {
        val locked = setOf("a", "b")
        val isLocked: (String) -> Boolean = { it in locked }

        assertThat(firstUnlockedHeroTitle(listOf(listOf("a", "c"), listOf("d")), isLocked)).isEqualTo("c")
        assertThat(firstUnlockedHeroTitle(listOf(listOf("a"), listOf("b", "d")), isLocked)).isEqualTo("d")
        assertThat(firstUnlockedHeroTitle(listOf(listOf("a"), listOf("b")), isLocked)).isNull()
        assertThat(firstUnlockedHeroTitle(emptyList<List<String>>(), isLocked)).isNull()
    }

    private fun lock(
        isAdult: Boolean = false,
        isUserProtected: Boolean = false,
        categoryId: Long? = 1L,
        level: Int = 1,
        unlocked: Set<Long> = emptySet()
    ) = isVodTitleLocked(isAdult, isUserProtected, categoryId, level, unlocked)
}
