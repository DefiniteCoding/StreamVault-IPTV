package com.streamvault.feature.catalog

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.streamvault.core.ui.design.BrowseVisualStyle
import com.streamvault.core.ui.theme.StreamVaultTheme
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series
import com.streamvault.feature.catalog.presentation.series.seasonAndEpisodeItems
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The D-pad map of the Series detail episode area: chips, episode cards and their download icons (rule 18). */
@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class SeriesEpisodeRowFocusTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val seasonTwoEpisodes = (1..4).map { number ->
        Episode(id = 200L + number, title = "Second $number", episodeNumber = number, seasonNumber = 2)
    }
    private val series = Series(
        id = 1L,
        name = "Fixture",
        seasons = listOf(
            Season(seasonNumber = 1, episodes = listOf(Episode(id = 101L, title = "First 1", episodeNumber = 1, seasonNumber = 1))),
            Season(seasonNumber = 2, episodes = seasonTwoEpisodes),
        ),
    )

    @Test
    fun downFromACardReachesItsDownloadIconAndUpReturns() {
        setContent()
        card(2).requestFocus()

        pressKey(Key.DirectionDown)
        downloadIcon(2).assertIsFocused()

        pressKey(Key.DirectionUp)
        card(2).assertIsFocused()
    }

    @Test
    fun downOnTheDownloadIconIsANoOp() {
        setContent()
        downloadIcon(3).requestFocus()

        pressKey(Key.DirectionDown)
        downloadIcon(3).assertIsFocused()
    }

    @Test
    fun downFromTheHeaderReachesTheSelectedChipThenTheEpisodeRow() {
        setContent()
        composeRule.onNodeWithTag(HEADER_TAG).requestFocus()

        pressKey(Key.DirectionDown)
        seasonChip(2).assertIsFocused()

        pressKey(Key.DirectionDown)
        card(1).assertIsFocused()

        pressKey(Key.DirectionRight)
        card(2).assertIsFocused()
    }

    @Test
    fun upFromTheEpisodeRowReturnsToTheSeasonChips() {
        setContent()
        card(1).requestFocus()

        pressKey(Key.DirectionUp)
        seasonChip(2).assertIsFocused()
    }

    @Test
    fun reenteringTheEpisodeRowRestoresTheLastFocusedCard() {
        setContent()
        card(3).requestFocus()

        pressKey(Key.DirectionUp)
        seasonChip(2).assertIsFocused()
        pressKey(Key.DirectionDown)
        card(3).assertIsFocused()
    }

    private fun setContent() {
        composeRule.setContent {
            StreamVaultTheme {
                BrowseVisualStyle {
                    LazyColumn {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .testTag(HEADER_TAG)
                                    .focusable()
                            )
                        }
                        seasonAndEpisodeItems(
                            series = series,
                            selectedSeason = series.seasons[1],
                            onSeasonSelected = {},
                            onEpisodeClick = {},
                            onDownloadEpisode = {},
                        )
                    }
                }
            }
        }
    }

    private fun card(number: Int): SemanticsNodeInteraction =
        composeRule.onNode(hasText("Second $number") and hasClickAction())

    private fun downloadIcon(number: Int): SemanticsNodeInteraction {
        val description = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.series_episode_download_cd, "Second $number")
        return composeRule.onNode(hasContentDescription(description) and hasClickAction())
    }

    private fun seasonChip(number: Int): SemanticsNodeInteraction =
        composeRule.onNode(hasText("Season $number") and hasClickAction())

    private fun pressKey(key: Key) {
        composeRule.onRoot().performKeyInput { pressKey(key) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val HEADER_TAG = "header"
    }
}
