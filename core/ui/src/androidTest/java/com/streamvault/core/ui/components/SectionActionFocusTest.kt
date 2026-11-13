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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
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
import com.streamvault.core.ui.components.shell.AppSectionHeader
import com.streamvault.core.ui.components.shell.AppSectionToggleAction
import com.streamvault.core.ui.theme.StreamVaultTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class SectionActionFocusTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contentRequester = FocusRequester()
    private var seeAllClicks = 0

    @Test
    fun rightMovesFromTheActionToTheToggleAndStopsAtTheEnd() {
        setHeader()
        focusContent()

        pressKey(Key.DirectionUp)
        action().assertIsFocused()
        pressKey(Key.DirectionRight)
        toggle().assertIsFocused()
        pressKey(Key.DirectionRight)
        toggle().assertIsFocused()
        pressKey(Key.DirectionLeft)
        action().assertIsFocused()
    }

    @Test
    fun downLeavesTheHeaderForTheRowBelow() {
        setHeader()
        focusContent()
        pressKey(Key.DirectionUp)

        pressKey(Key.DirectionDown)
        composeRule.onNodeWithTag(CONTENT_TAG).assertIsFocused()
    }

    @Test
    fun centerRunsTheActionAndFlipsTheToggle() {
        setHeader()
        focusContent()
        pressKey(Key.DirectionUp)

        pressKey(Key.DirectionCenter)
        assertThat(seeAllClicks).isEqualTo(1)

        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)
        toggle().assertIsSelected()
    }

    @Test
    fun actionAndToggleAnnounceTheirRoleAndSelection() {
        setHeader()

        action().assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        // The chevron is decorative: the label names the action.
        action().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        action().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
        toggle().assertIsNotSelected()
    }

    @Test
    fun headerMirrorsInRtl() {
        setHeader(layoutDirection = LayoutDirection.Rtl)
        focusContent()
        pressKey(Key.DirectionUp)

        action().assertIsFocused()
        pressKey(Key.DirectionLeft)
        toggle().assertIsFocused()
    }

    private fun setHeader(layoutDirection: LayoutDirection = LayoutDirection.Ltr) {
        seeAllClicks = 0
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                StreamVaultTheme {
                    var pinned by remember { mutableStateOf(false) }
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppSectionHeader(
                                title = "Action",
                                modifier = Modifier.weight(1f),
                                actionLabel = ACTION_LABEL,
                                onActionClick = { seeAllClicks++ }
                            )
                            AppSectionToggleAction(
                                icon = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = TOGGLE_DESCRIPTION,
                                selected = pinned,
                                onClick = { pinned = !pinned }
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .focusRequester(contentRequester)
                                .testTag(CONTENT_TAG)
                                .focusable()
                        )
                    }
                }
            }
        }
    }

    private fun focusContent() {
        composeRule.runOnIdle { contentRequester.requestFocus() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(CONTENT_TAG).assertIsFocused()
    }

    private fun action() = composeRule.onNode(hasText(ACTION_LABEL) and hasClickAction())

    private fun toggle() = composeRule.onNode(hasContentDescription(TOGGLE_DESCRIPTION) and hasClickAction())

    private fun pressKey(key: Key) {
        composeRule.onRoot().performKeyInput { pressKey(key) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val CONTENT_TAG = "content"
        const val ACTION_LABEL = "See all"
        const val TOGGLE_DESCRIPTION = "Pin category"
    }
}
