package com.streamvault.app.ui.components.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.streamvault.core.ui.design.AppPalette
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.test.assertAgainstGolden
import com.streamvault.core.ui.theme.StreamVaultTheme
import com.streamvault.core.ui.components.shell.UiDestination
import com.streamvault.domain.model.Channel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class ShellGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appScreenScaffold_usesProvidedNavigationDestinations() {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppDestinationItems provides listOf(
                    UiDestination(
                        id = "injected",
                        label = "Injected destination",
                        icon = Icons.Default.Home
                    )
                )
            ) {
                StreamVaultTheme {
                    AppScreenScaffold(
                        currentRoute = "injected",
                        onNavigate = {},
                        title = "Injected shell"
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        composeRule.onNodeWithContentDescription("Injected destination").assertExists()
        composeRule.onNodeWithContentDescription("Live TV").assertDoesNotExist()
    }

    @Test
    fun browseHeroPanel_matchesGolden() {
        composeRule.setContent {
            StreamVaultTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("golden")
                ) {
                    BrowseHeroPanel(
                        title = "Late Night Premiere",
                        subtitle = "Premium hero layout for large-screen browse surfaces.",
                        eyebrow = "Movies",
                        metadata = listOf("2026", "RTG 8.8"),
                        actionLabel = "Play",
                        onClick = {},
                        imageUrl = null
                    )
                }
            }
        }

        composeRule.onNodeWithTag("golden").assertAgainstGolden("browse_hero_panel")
    }

    @Test
    fun liveChannelRowSurface_matchesGolden() {
        composeRule.setContent {
            StreamVaultTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("golden")
                ) {
                    LiveChannelRowSurface(
                        channel = Channel(
                            id = 7L,
                            name = "World Sports HD",
                            streamUrl = "https://example.com/live",
                            logoUrl = null,
                            isFavorite = true,
                            catchUpSupported = true
                        ),
                        nowMs = 0L,
                        onClick = {},
                        onLongClick = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("golden").assertAgainstGolden("live_channel_row_surface")
    }

    @Test
    fun appScreenScaffold_rtl_matchesGolden() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                StreamVaultTheme {
                    AppScreenScaffold(
                        currentRoute = Routes.EPG,
                        onNavigate = {},
                        title = "Guide",
                        subtitle = "RTL shell validation for premium TV surfaces."
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("golden")
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("golden").assertAgainstGolden("app_screen_scaffold_rtl")
    }

    @Test
    fun topNavigationBar_rest_matchesGolden() {
        setTopBarScreen(AppPalette.CLASSIC_BLUE_ID)
        composeRule.onNodeWithTag("golden").assertAgainstGolden("top_navigation_bar_rest")
    }

    @Test
    fun topNavigationBar_focused_matchesGoldenInEveryReferenceTheme() {
        listOf(AppPalette.CLASSIC_BLUE_ID, AppPalette.LIGHT_ID, AppPalette.AMOLED_BLACK_ID).forEach { themeId ->
            setTopBarScreen(themeId)
            focusTabNextToSelected()
            composeRule.onNodeWithTag("golden").assertAgainstGolden("top_navigation_bar_focused_$themeId")
        }
    }

    private val currentThemeId = androidx.compose.runtime.mutableStateOf(AppPalette.CLASSIC_BLUE_ID)
    private val contentRequester = FocusRequester()
    private var contentSet = false

    // One composition per test: switching the theme state recomposes it instead of calling setContent again.
    private fun setTopBarScreen(themeId: String) {
        currentThemeId.value = themeId
        if (contentSet) {
            composeRule.runOnIdle { contentRequester.requestFocus() }
            composeRule.waitForIdle()
            return
        }
        contentSet = true
        composeRule.setContent {
            StreamVaultTheme(themeId = currentThemeId.value) {
                AppScreenScaffold(
                    currentRoute = Routes.LIVE_TV,
                    onNavigate = {},
                    title = "Live TV",
                    navigationChrome = AppNavigationChrome.TopBar,
                    showScreenHeader = false,
                    modifier = Modifier.testTag("golden")
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .focusRequester(contentRequester)
                            .focusable()
                    )
                }
            }
        }
        composeRule.runOnIdle { contentRequester.requestFocus() }
        composeRule.waitForIdle()
    }

    // Up restores the selected Live TV tab, Right focuses its neighbour: one golden shows selected and focused apart.
    private fun focusTabNextToSelected() {
        composeRule.onRoot().performKeyInput { pressKey(Key.DirectionUp) }
        composeRule.waitForIdle()
        composeRule.onRoot().performKeyInput { pressKey(Key.DirectionRight) }
        composeRule.waitForIdle()
    }
}
