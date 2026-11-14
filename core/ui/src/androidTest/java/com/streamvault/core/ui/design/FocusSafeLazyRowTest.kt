package com.streamvault.core.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pixel checks of [FocusSafeLazyRow] on a 600dp wide black screen with white 200dp items: cards must run past the
 * screen padding to the screen edge, and the edge mask must show only on a side that has more items behind it.
 */
@RunWith(AndroidJUnit4::class)
class FocusSafeLazyRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val screenWidth = 600.dp
    private val gutter = 56.dp

    @Test
    fun cardsRunPastTheScreenPaddingToTheScreenEdge() {
        setRow(fadeWidth = 24.dp)

        // 560dp is inside the 56dp gutter (a plain row clipped here) and outside the 24dp fade.
        assertThat(redAt(560.dp)).isGreaterThan(0.9f)
        // The last pixel column is the end of the fade: the cut card has faded out.
        assertThat(redAt(599.dp)).isLessThan(0.25f)
    }

    @Test
    fun theFirstCardStillLinesUpWithTheScreenPadding() {
        setRow(fadeWidth = 24.dp)

        assertThat(redAt(gutter - 4.dp)).isLessThan(0.05f)
        assertThat(redAt(gutter + 4.dp)).isGreaterThan(0.9f)
    }

    @Test
    fun theStartEdgeFadesOnlyAfterTheRowScrolled() {
        setRow(fadeWidth = 56.dp)
        composeRule.onNodeWithTag(ROW).performScrollToIndex(1)
        composeRule.waitForIdle()

        // Item 0 now sits under the gutter: half way up the ramp it is half visible.
        assertHalfVisible(redAt(28.dp))
    }

    @Test
    fun aRowWithoutGuttersDoesNotFadeAnEdgeThatHasNothingBehindIt() {
        setRow(fadeWidth = 56.dp, gutterStart = 0.dp, gutterEnd = 0.dp, itemCount = 2)

        // Two 200dp items fit: nothing to scroll to, so no mask on either side.
        assertThat(redAt(2.dp)).isGreaterThan(0.9f)
        assertThat(redAt(398.dp)).isGreaterThan(0.9f)
    }

    @Test
    fun rightToLeftMirrorsTheFadeAndTheStartPadding() {
        setRow(fadeWidth = 56.dp, direction = LayoutDirection.Rtl)

        // Item 0 is at the right padding; more items lie to the left, so the left edge is the one that fades.
        assertThat(redAt(screenWidth - gutter - 4.dp)).isGreaterThan(0.9f)
        assertThat(redAt(screenWidth - gutter + 4.dp)).isLessThan(0.05f)
        assertHalfVisible(redAt(28.dp))
    }

    private fun assertHalfVisible(red: Float) {
        assertThat(red).isGreaterThan(0.2f)
        assertThat(red).isLessThan(0.8f)
    }

    private fun setRow(
        fadeWidth: Dp,
        gutterStart: Dp = gutter,
        gutterEnd: Dp = gutter,
        itemCount: Int = 6,
        direction: LayoutDirection = LayoutDirection.Ltr
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                Screen(gutterStart, gutterEnd, fadeWidth, itemCount)
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun Screen(gutterStart: Dp, gutterEnd: Dp, fadeWidth: Dp, itemCount: Int) {
        Box(Modifier.size(screenWidth, 120.dp).background(Color.Black).testTag(SCREEN)) {
            // The screen's own padding: a plain row would clip its cards at this box.
            Box(Modifier.fillMaxSize().padding(start = gutterStart, end = gutterEnd)) {
                FocusSafeLazyRow(
                    modifier = Modifier.testTag(ROW),
                    edgeGutterStart = gutterStart,
                    edgeGutterEnd = gutterEnd,
                    fadeWidth = fadeWidth
                ) {
                    items((0 until itemCount).toList()) { index ->
                        Box(Modifier.size(200.dp, 120.dp).background(Color.White).testTag("item_$index"))
                    }
                }
            }
        }
    }

    /** Red channel (0 to 1) of the pixel at [x] on the vertical middle of the screen. */
    private fun redAt(x: Dp): Float {
        val density = composeRule.density
        val bitmap = composeRule.onNodeWithTag(SCREEN).captureToImage().asAndroidBitmap()
        val px = with(density) { x.toPx() }.toInt().coerceIn(0, bitmap.width - 1)
        return android.graphics.Color.red(bitmap.getPixel(px, bitmap.height / 2)) / 255f
    }

    private companion object {
        const val SCREEN = "screen"
        const val ROW = "row"
    }
}
