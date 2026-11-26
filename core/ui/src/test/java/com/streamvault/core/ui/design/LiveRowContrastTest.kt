package com.streamvault.core.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** Text pairs on the Live TV category and channel rows, at rest, focused and selected, in every theme. */
class LiveRowContrastTest {
    private val allThemeIds = listOf(
        AppPalette.CLASSIC_BLUE_ID, AppPalette.M3_PURPLE_ID, AppPalette.LIGHT_ID,
        AppPalette.FOREST_ID, AppPalette.SUNSET_ID, AppPalette.OCEAN_ID,
        AppPalette.ROSE_ID, AppPalette.AMOLED_BLACK_ID, AppPalette.VINTAGE_ID,
    )

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
    }

    private val failures = mutableListOf<String>()

    private fun assertAtLeast(minimum: Float, id: String, label: String, foreground: Color, background: Color) {
        val ratio = contrast(foreground, background)
        if (ratio < minimum) failures += "$id $label was $ratio"
    }

    private fun assertNoFailures() = assertTrue(failures.joinToString("\n"), failures.isEmpty())

    @Test
    fun channelRowTextIsReadableAtRestAndFocused() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            listOf("surfaceElevated" to browse.surfaceElevated, "surfaceEmphasis" to browse.surfaceEmphasis).forEach { (name, row) ->
                assertAtLeast(4.5f, id, "textPrimary (name) on $name", browse.textPrimary, row)
                // Number, programme title and "No schedule": tertiary fails 4.5 on surfaceEmphasis in four themes, so it is not used here.
                assertAtLeast(4.5f, id, "textSecondary on $name", browse.textSecondary, row)
            }
        }
        assertNoFailures()
    }

    @Test
    fun categoryRowTextIsReadableFocusedAndSelected() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            assertAtLeast(4.5f, id, "textPrimary on surfaceAccent (focused)", browse.textPrimary, browse.surfaceAccent)
            assertAtLeast(4.5f, id, "onSelectedContainer on selectedContainer", browse.onSelectedContainer, browse.selectedContainer)
        }
        assertNoFailures()
    }
}
