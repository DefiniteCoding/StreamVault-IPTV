package com.streamvault.core.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** The resume prompt card sits on the player palette; only the primary label ink is derived (BrowseColors.OnPrimary). */
class ResumePromptContrastTest {
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

    private fun check(id: String, label: String, ink: Color, fill: Color, floor: Float) {
        val ratio = contrast(ink, fill)
        println("RESUME $id $label=$ratio")
        assertTrue("$id $label was $ratio, needs $floor", ratio >= floor)
    }

    @Test
    fun resumePromptTextAndRingMeetThresholdsInEveryTheme() {
        allThemeIds.forEach { id ->
            val p = AppPalette.forTheme(id)
            check(id, "TextPrimary/CanvasElevated", p.textPrimary, p.canvasElevated, 4.5f)
            check(id, "TextSecondary/CanvasElevated", p.textSecondary, p.canvasElevated, 4.5f)
            check(id, "OnPrimary/Brand", p.forBrowsing().onPrimary, p.brand, 4.5f)
            check(id, "TextPrimary/ControlContainer", p.textPrimary, p.controlContainer, 4.5f)
            check(id, "Focus/CanvasElevated", p.focus, p.canvasElevated, 3f)
            // The ring's outer edge meets the card (asserted above); the inner edge meets the fill. Reported only.
            println("RESUME $id Focus/Brand=${contrast(p.focus, p.brand)}")
            println("RESUME $id Focus/ControlContainer=${contrast(p.focus, p.controlContainer)}")
            println("RESUME $id ControlContainer/CanvasElevated=${contrast(p.controlContainer, p.canvasElevated)}")
        }
    }
}
