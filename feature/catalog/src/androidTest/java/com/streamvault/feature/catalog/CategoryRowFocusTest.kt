package com.streamvault.feature.catalog

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.streamvault.core.ui.theme.StreamVaultTheme
import com.streamvault.feature.catalog.presentation.components.CategoryRow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class CategoryRowFocusTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun upFromAnyPosterReachesSeeAllAndDownReturnsToThatPoster() {
        setRow(withActions = true)
        poster(5).requestFocus()
        poster(5).assertIsFocused()

        pressKey(Key.DirectionUp)
        seeAll().assertIsFocused()

        pressKey(Key.DirectionDown)
        poster(5).assertIsFocused()
    }

    @Test
    fun secondUpLeavesForTheRowAbove() {
        setRow(withActions = true)
        poster(5).requestFocus()

        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionUp)
        composeRule.onNodeWithTag(ABOVE_TAG).assertIsFocused()
    }

    @Test
    fun rightFromSeeAllReachesThePinAndStopsThere() {
        setRow(withActions = true)
        poster(5).requestFocus()
        pressKey(Key.DirectionUp)

        pressKey(Key.DirectionRight)
        pin().assertIsFocused()
        pressKey(Key.DirectionRight)
        pin().assertIsFocused()
        pressKey(Key.DirectionLeft)
        seeAll().assertIsFocused()
    }

    @Test
    fun rowWithoutActionsGoesStraightToTheRowAbove() {
        setRow(withActions = false)
        poster(5).requestFocus()

        pressKey(Key.DirectionUp)
        composeRule.onNodeWithTag(ABOVE_TAG).assertIsFocused()
    }

    private fun setRow(withActions: Boolean) {
        composeRule.setContent {
            StreamVaultTheme {
                var pinned by remember { mutableStateOf(false) }
                Column {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag(ABOVE_TAG)
                            .focusable()
                    )
                    CategoryRow(
                        title = "Row",
                        items = (0 until POSTER_COUNT).toList(),
                        onSeeAll = if (withActions) ({}) else null,
                        onPinToggle = if (withActions) ({ pinned = !pinned }) else null,
                        isPinned = pinned,
                        keySelector = { it }
                    ) { index ->
                        Box(
                            Modifier
                                .size(width = 160.dp, height = 90.dp)
                                .testTag("poster-$index")
                                .focusable()
                        )
                    }
                }
            }
        }
    }

    private fun poster(index: Int) = composeRule.onNodeWithTag("poster-$index")

    private fun seeAll(): SemanticsNodeInteraction =
        composeRule.onNode(hasText(string(R.string.category_see_all)) and hasClickAction())

    private fun pin(): SemanticsNodeInteraction =
        composeRule.onNode(hasContentDescription(string(R.string.category_pin)) and hasClickAction())

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun pressKey(key: Key) {
        composeRule.onRoot().performKeyInput { pressKey(key) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val ABOVE_TAG = "above"
        const val POSTER_COUNT = 30
    }
}
