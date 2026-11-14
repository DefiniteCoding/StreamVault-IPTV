package com.streamvault.feature.catalog.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.streamvault.core.ui.design.AppPalette
import org.junit.Assert.assertTrue
import org.junit.Test

class VodFocusHeroContrastTest {
    private val themeIds = listOf(
        AppPalette.CLASSIC_BLUE_ID,
        AppPalette.M3_PURPLE_ID,
        AppPalette.LIGHT_ID,
        AppPalette.FOREST_ID,
        AppPalette.SUNSET_ID,
        AppPalette.OCEAN_ID,
        AppPalette.ROSE_ID,
        AppPalette.AMOLED_BLACK_ID,
        AppPalette.VINTAGE_ID,
    )

    // The browse screens draw on AppPalette.forBrowsing(), which is internal to core:ui. It blends the elevated surface
    // 48% of the way from surface to surfaceElevated and leaves text, focus and brand untouched.
    private fun heroSurface(palette: AppPalette) = lerp(palette.surface, palette.surfaceElevated, 0.48f)

    @Test
    fun heroTextHoldsFourPointFiveOnTheHeroSurfaceInEveryTheme() {
        themeIds.forEach { id ->
            val palette = AppPalette.forTheme(id)
            mapOf("textPrimary" to palette.textPrimary, "textSecondary" to palette.textSecondary).forEach { (token, ink) ->
                val ratio = contrastRatio(ink, heroSurface(palette))
                assertTrue("$id $token on surfaceElevated was $ratio, expected at least 4.5", ratio >= 4.5f)
            }
        }
    }

    @Test
    fun heroButtonRingAndFillHoldThreeOnTheHeroSurfaceInEveryTheme() {
        themeIds.forEach { id ->
            val palette = AppPalette.forTheme(id)
            val ring = contrastRatio(palette.focus, heroSurface(palette))
            assertTrue("$id focus ring on surfaceElevated was $ring, expected at least 3", ring >= 3f)
            val fill = contrastRatio(palette.brand, heroSurface(palette))
            assertTrue("$id brand fill on surfaceElevated was $fill, expected at least 3", fill >= 3f)
        }
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val firstLuminance = first.luminance()
        val secondLuminance = second.luminance()
        return (maxOf(firstLuminance, secondLuminance) + 0.05f) /
            (minOf(firstLuminance, secondLuminance) + 0.05f)
    }
}
