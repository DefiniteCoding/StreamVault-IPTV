package com.streamvault.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.streamvault.app.ui.test.assertAgainstGolden
import com.streamvault.core.ui.components.AppChipGroup
import com.streamvault.core.ui.components.AppDropdownPill
import com.streamvault.core.ui.components.AppFilterChip
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.BrowseVisualStyle
import com.streamvault.core.ui.design.browseBackdrop
import com.streamvault.core.ui.theme.StreamVaultTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * One row shows every chip state at once: a selected chip, resting chips with and without a leading icon, a selected
 * chip whose check replaced its icon, and the dropdown pill. Each golden moves focus to a different kind of element.
 */
@RunWith(AndroidJUnit4::class)
class AppChipsGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val currentThemeId = mutableStateOf(AppPalette.CLASSIC_BLUE_ID)
    private val restingChip = FocusRequester()
    private val selectedChip = FocusRequester()
    private val dropdown = FocusRequester()
    private var contentSet = false

    @Test
    fun chips_matchGoldensInEveryReferenceTheme() {
        REFERENCE_THEMES.forEach { themeId ->
            setChipRow(themeId, LtrLabels)
            focusAndCompare(restingChip, "app_chips_focused_$themeId")
            focusAndCompare(selectedChip, "app_chips_focused_selected_$themeId")
            focusAndCompare(dropdown, "app_chips_dropdown_focused_$themeId")
        }
    }

    @Test
    fun chips_rtl_matchGolden() {
        setChipRow(AppPalette.CLASSIC_BLUE_ID, RtlLabels, LayoutDirection.Rtl)
        focusAndCompare(restingChip, "app_chips_focused_rtl")
    }

    // One composition per test: switching the theme state recomposes it instead of calling setContent again.
    private fun setChipRow(themeId: String, labels: ChipLabels, layoutDirection: LayoutDirection = LayoutDirection.Ltr) {
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
                            AppChipGroup(label = labels.sortGroup) {
                                AppFilterChip(
                                    label = labels.newest,
                                    selected = true,
                                    onClick = {},
                                    modifier = Modifier.focusRequester(selectedChip)
                                )
                                AppFilterChip(
                                    label = labels.title,
                                    selected = false,
                                    onClick = {},
                                    modifier = Modifier.focusRequester(restingChip)
                                )
                            }
                            AppChipGroup(label = labels.showGroup) {
                                AppFilterChip(label = labels.favourites, selected = true, onClick = {}, leadingIcon = Icons.Default.Star)
                                AppFilterChip(label = labels.recent, selected = false, onClick = {}, leadingIcon = Icons.Default.Star)
                            }
                            AppDropdownPill(
                                label = labels.category,
                                value = labels.categoryValue,
                                onClick = {},
                                modifier = Modifier.focusRequester(dropdown)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun focusAndCompare(requester: FocusRequester, goldenName: String) {
        composeRule.runOnIdle { requester.requestFocus() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(GOLDEN_TAG).assertAgainstGolden(goldenName)
    }

    private data class ChipLabels(
        val sortGroup: String,
        val newest: String,
        val title: String,
        val showGroup: String,
        val favourites: String,
        val recent: String,
        val category: String,
        val categoryValue: String,
    )

    private companion object {
        const val GOLDEN_TAG = "golden"
        val REFERENCE_THEMES = listOf(AppPalette.CLASSIC_BLUE_ID, AppPalette.LIGHT_ID, AppPalette.AMOLED_BLACK_ID)
        val LtrLabels = ChipLabels("Sort", "Newest", "A-Z", "Show", "Favourites", "Recent", "Category", "Action")
        val RtlLabels = ChipLabels("ترتيب", "الأحدث", "أ-ي", "عرض", "المفضلة", "الأخيرة", "الفئة", "أكشن")
    }
}
