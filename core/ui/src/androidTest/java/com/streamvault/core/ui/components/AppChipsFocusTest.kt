package com.streamvault.core.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.streamvault.core.ui.theme.StreamVaultTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class AppChipsFocusTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val firstChipRequester = FocusRequester()

    @Test
    fun rightCrossesIntoTheNextGroupAndStopsAtTheEnd() {
        setChips()
        focusFirstChip()

        pressKey(Key.DirectionLeft)
        chip("Newest").assertIsFocused()

        pressKey(Key.DirectionRight)
        chip("A-Z").assertIsFocused()
        pressKey(Key.DirectionRight)
        chip("All").assertIsFocused()
        pressKey(Key.DirectionRight)
        chip("Unwatched").assertIsFocused()
        pressKey(Key.DirectionRight)
        dropdown().assertIsFocused()

        pressKey(Key.DirectionRight)
        dropdown().assertIsFocused()
    }

    @Test
    fun downLeavesTheChipRow() {
        setChips()
        focusFirstChip()

        pressKey(Key.DirectionDown)
        composeRule.onNodeWithTag(CONTENT_TAG).assertIsFocused()
    }

    @Test
    fun centerSelectsTheFocusedChip() {
        setChips()
        focusFirstChip()

        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)

        chip("A-Z").assertIsSelected()
        chip("Newest").assertIsNotSelected()
    }

    @Test
    fun chipsAnnounceSelectionAndTheDropdownAnnouncesLabelAndValue() {
        setChips()

        chip("Newest")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
        chip("A-Z").assertIsNotSelected()
        // A leading icon and the selected check are decorative.
        chip("All").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        dropdown()
            .assert(hasText(DROPDOWN_VALUE))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.DropdownList))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }

    @Test
    fun rowMirrorsInRtl() {
        setChips(layoutDirection = LayoutDirection.Rtl)
        focusFirstChip()

        assertThat(chip("Newest").getUnclippedBoundsInRoot().left)
            .isGreaterThan(chip("A-Z").getUnclippedBoundsInRoot().right)
        pressKey(Key.DirectionLeft)
        chip("A-Z").assertIsFocused()
    }

    private fun setChips(layoutDirection: LayoutDirection = LayoutDirection.Ltr) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                StreamVaultTheme {
                    var sort by remember { mutableStateOf("Newest") }
                    var show by remember { mutableStateOf("All") }
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            AppChipGroup(label = "Sort") {
                                listOf("Newest", "A-Z").forEachIndexed { index, label ->
                                    AppFilterChip(
                                        label = label,
                                        selected = sort == label,
                                        onClick = { sort = label },
                                        modifier = if (index == 0) Modifier.focusRequester(firstChipRequester) else Modifier
                                    )
                                }
                            }
                            AppChipGroup(label = "Show") {
                                listOf("All", "Unwatched").forEach { label ->
                                    AppFilterChip(
                                        label = label,
                                        selected = show == label,
                                        onClick = { show = label },
                                        leadingIcon = Icons.Default.Star
                                    )
                                }
                            }
                            AppDropdownPill(label = DROPDOWN_LABEL, value = DROPDOWN_VALUE, onClick = {})
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag(CONTENT_TAG)
                                .focusable()
                        )
                    }
                }
            }
        }
    }

    private fun focusFirstChip() {
        composeRule.runOnIdle { firstChipRequester.requestFocus() }
        composeRule.waitForIdle()
        chip("Newest").assertIsFocused()
    }

    private fun chip(label: String) = composeRule.onNode(hasText(label) and hasClickAction())

    private fun dropdown() = composeRule.onNode(hasText(DROPDOWN_LABEL) and hasClickAction())

    private fun pressKey(key: Key) {
        composeRule.onRoot().performKeyInput { pressKey(key) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val CONTENT_TAG = "content"
        const val DROPDOWN_LABEL = "Category"
        const val DROPDOWN_VALUE = "Action"
    }
}
