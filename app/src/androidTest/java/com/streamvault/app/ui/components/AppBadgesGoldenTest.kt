package com.streamvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.streamvault.app.ui.test.assertAgainstGolden
import com.streamvault.core.ui.components.shell.BadgeTone
import com.streamvault.core.ui.components.shell.EpisodeBadge
import com.streamvault.core.ui.components.shell.QualityBadge
import com.streamvault.core.ui.components.shell.RatingBadge
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.BrowseVisualStyle
import com.streamvault.core.ui.design.browseBackdrop
import com.streamvault.core.ui.theme.StreamVaultTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Every badge in both tones: over the brightest and darkest artwork (plain tiles stand in for posters, since the scrim
 * must hold on any image) and on a browse surface.
 */
@RunWith(AndroidJUnit4::class)
class AppBadgesGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val currentThemeId = mutableStateOf(AppPalette.CLASSIC_BLUE_ID)
    private var contentSet = false

    @Test
    fun badges_matchGoldensInEveryReferenceTheme() {
        REFERENCE_THEMES.forEach { themeId ->
            setBadges(themeId, LtrLabels)
            compare("app_badges_$themeId")
        }
    }

    @Test
    fun badges_rtl_matchGolden() {
        setBadges(AppPalette.CLASSIC_BLUE_ID, RtlLabels, LayoutDirection.Rtl)
        compare("app_badges_rtl")
    }

    // Badges sit inside focusable cards and rows, so they must never take focus and must read their description.
    // The merged tree is what accessibility reads; the raw "7.8" must not reach it.
    @Test
    fun badges_announceTheirDescriptionAndNeverTakeFocus() {
        setBadges(AppPalette.CLASSIC_BLUE_ID, LtrLabels)
        listOf(LtrLabels.quality, LtrLabels.ratingDescription, LtrLabels.episodeDescription).forEach { description ->
            composeRule.onAllNodesWithContentDescription(description).assertCountEquals(BADGE_SETS)
        }
        composeRule.onAllNodes(isFocusable()).assertCountEquals(0)
        composeRule.onAllNodesWithText(LtrLabels.rating).assertCountEquals(0)
    }

    // One composition per test: switching the theme state recomposes it instead of calling setContent again.
    private fun setBadges(themeId: String, labels: BadgeLabels, layoutDirection: LayoutDirection = LayoutDirection.Ltr) {
        currentThemeId.value = themeId
        if (contentSet) return
        contentSet = true
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                StreamVaultTheme(themeId = currentThemeId.value) {
                    BrowseVisualStyle {
                        Row(
                            modifier = Modifier
                                .testTag(GOLDEN_TAG)
                                .browseBackdrop()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            BadgeSet(labels, BadgeTone.OverArtwork, Modifier.background(Color.White))
                            BadgeSet(labels, BadgeTone.OverArtwork, Modifier.background(Color.Black))
                            BadgeSet(labels, BadgeTone.OnSurface, Modifier.background(BrowseColors.Surface))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BadgeSet(labels: BadgeLabels, tone: BadgeTone, backdrop: Modifier) {
        Column(
            modifier = backdrop.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QualityBadge(text = labels.quality, tone = tone)
            RatingBadge(value = labels.rating, contentDescription = labels.ratingDescription, tone = tone)
            EpisodeBadge(text = labels.episode, contentDescription = labels.episodeDescription, tone = tone)
        }
    }

    private fun compare(goldenName: String) {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(GOLDEN_TAG).assertAgainstGolden(goldenName)
    }

    private data class BadgeLabels(
        val quality: String,
        val rating: String,
        val ratingDescription: String,
        val episode: String,
        val episodeDescription: String,
    )

    private companion object {
        const val GOLDEN_TAG = "golden"
        const val BADGE_SETS = 3
        val REFERENCE_THEMES = listOf(AppPalette.CLASSIC_BLUE_ID, AppPalette.LIGHT_ID, AppPalette.AMOLED_BLACK_ID)
        val LtrLabels = BadgeLabels("4k", "7.8", "Rated 7.8", "S1 . E4", "Season 1, episode 4")
        val RtlLabels = BadgeLabels("4k", "٧٫٨", "التقييم ٧٫٨", "م١ . ح٤", "الموسم ١، الحلقة ٤")
    }
}
