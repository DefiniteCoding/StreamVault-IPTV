package com.streamvault.core.ui.components.shell

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.streamvault.core.ui.theme.StreamVaultTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class TopNavigationFocusTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contentRequester = FocusRequester()
    private var contentInset: Dp = Dp.Unspecified

    @Test
    fun upFromContentLandsOnTheActiveTabAndDownReturns() {
        setScaffold(currentDestinationId = "live")
        composeRule.runOnIdle { contentRequester.requestFocus() }

        pressKey(Key.DirectionUp)
        tab("Live").assertIsFocused()

        pressKey(Key.DirectionRight)
        tab("Guide").assertIsFocused()

        pressKey(Key.DirectionDown)
        composeRule.onNodeWithTag(CONTENT_TAG).assertIsFocused()
    }

    @Test
    fun tabsAnnounceRoleAndSelectionOnce() {
        setScaffold(currentDestinationId = "live")

        tab("Live")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        tab("Home").assertIsNotSelected()
        // The icon is decorative: the visible label already names the tab.
        composeRule.onAllNodesWithContentDescription("Live").assertCountEquals(0)
    }

    @Test
    fun paddedContentStartsBelowTheBar() {
        setScaffold(currentDestinationId = "home")

        val barBottom = tab("Home").getUnclippedBoundsInRoot().bottom
        val contentTop = composeRule.onNodeWithTag(CONTENT_TAG).getUnclippedBoundsInRoot().top
        assertThat(contentTop).isGreaterThan(barBottom)
        assertThat(contentTop).isEqualTo(contentInset)
    }

    @Test
    fun overlaidContentStartsAtTheTopAndReadsTheInset() {
        setScaffold(currentDestinationId = "home", overlay = true)

        val contentTop = composeRule.onNodeWithTag(CONTENT_TAG).getUnclippedBoundsInRoot().top
        assertThat(contentTop).isEqualTo(0.dp)
        // Bar top inset (md 16) + bar height (48) + gap (sm 12).
        assertThat(contentInset).isEqualTo(76.dp)
    }

    private fun setScaffold(currentDestinationId: String, overlay: Boolean = false) {
        composeRule.setContent {
            CompositionLocalProvider(LocalConfiguration provides Configuration().apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
                screenWidthDp = 960
                screenHeightDp = 540
            }) {
                StreamVaultTheme {
                    CoreAppScreenScaffold(
                        currentDestinationId = currentDestinationId,
                        destinations = listOf("home" to "Home", "live" to "Live", "guide" to "Guide")
                            .map { (id, label) -> UiDestination(id, label, Icons.Default.Home) },
                        onDestinationSelected = {},
                        title = "Screen",
                        navigationChrome = NavigationChrome.TopBar,
                        showScreenHeader = false,
                        topBarOverlaysContent = overlay
                    ) {
                        contentInset = LocalTopBarContentInset.current
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag(CONTENT_TAG)
                                .focusRequester(contentRequester)
                                .focusable()
                        )
                    }
                }
            }
        }
    }

    private fun tab(label: String) = composeRule.onNode(hasText(label) and hasClickAction())

    private fun pressKey(key: Key) {
        composeRule.onRoot().performKeyInput { pressKey(key) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val CONTENT_TAG = "content"
    }
}
