package com.streamvault.core.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Glass is translucent, so every check composites the real draw stack (backdrop, glass base, tier fill, then the
 * sheen at its peak) over the backdrops content can put behind it. Pressed pills only scale, so they add no colors.
 */
class GlassPaletteContrastTest {

    @Test
    fun `resting labels meet 4,5 to 1 on both glass tiers`() = checkEveryTheme { palette ->
        barStack(palette).forEach { (where, bar) ->
            expect("TextSecondary on GlassBar over $where", palette.textSecondary, bar, TEXT)
        }
        darkTierStack(palette).forEach { (where, bar) ->
            expect("OnGlassOverVideo on GlassOverVideo over $where", palette.onGlassOverVideo, bar, TEXT)
        }
    }

    @Test
    fun `selected pill meets 3 to 1 against both glass tiers and carries readable ink`() = checkEveryTheme { palette ->
        (barStack(palette) + darkTierStack(palette)).forEach { (where, bar) ->
            expect("NavSelected against glass over $where", palette.navSelected, bar, NON_TEXT)
        }
        expect("OnNavSelected on NavSelected", palette.onNavSelected, palette.navSelected, TEXT)
    }

    @Test
    fun `focused pill and its ring meet 3 to 1 against both glass tiers`() = checkEveryTheme { palette ->
        barStack(palette).forEach { (where, bar) ->
            expect("Focus against GlassBar over $where", palette.focus, bar, NON_TEXT)
        }
        darkTierStack(palette).forEach { (where, bar) ->
            expect("FocusOverVideo against GlassOverVideo over $where", palette.focusOverVideo, bar, NON_TEXT)
        }
        expect("OnFocus on Focus", palette.onFocus, palette.focus, TEXT)
        expect("OnFocusOverVideo on FocusOverVideo", palette.onFocusOverVideo, palette.focusOverVideo, TEXT)
    }

    @Test
    fun `selected pill never reads as focused`() = checkEveryTheme { palette ->
        expect("NavSelected against Focus", palette.navSelected, palette.focus, NON_TEXT)
        expect("NavSelected against FocusOverVideo", palette.navSelected, palette.focusOverVideo, NON_TEXT)
    }

    @Test
    fun `glass panels keep text and focus readable`() = checkEveryTheme { palette ->
        panelStack(palette).forEach { (where, panel) ->
            expect("TextPrimary on GlassPanel over $where", palette.textPrimary, panel, TEXT)
            expect("TextSecondary on GlassPanel over $where", palette.textSecondary, panel, TEXT)
            expect("Focus against GlassPanel over $where", palette.focus, panel, NON_TEXT)
        }
    }

    @Test
    fun `text stays readable on the soft accent wash`() = checkEveryTheme { palette ->
        listOf(
            "Canvas" to palette.canvas,
            "Surface" to palette.surface,
            "browse Canvas" to palette.forBrowsing().canvas,
        ).forEach { (where, surface) ->
            expect("TextPrimary on AccentSoft over $where", palette.textPrimary, palette.accentSoft.compositeOver(surface), TEXT)
        }
    }

    private fun backdrops(palette: AppPalette) = listOf(
        "white" to Color.White,
        "black" to Color.Black,
        "Canvas" to palette.canvas,
        "browse Canvas" to palette.forBrowsing().canvas,
        "saturated mid-tone" to SaturatedMidTone,
    )

    private fun barStack(palette: AppPalette) = glassStack(palette, palette.glassBar)

    private fun panelStack(palette: AppPalette) = glassStack(palette, palette.glassPanel)

    private fun glassStack(palette: AppPalette, tier: Color) = backdrops(palette).flatMap { (where, backdrop) ->
        val fill = tier.compositeOver(palette.glassBase.compositeOver(backdrop))
        listOf(where to fill, "$where under the sheen" to palette.glassSheen.compositeOver(fill))
    }

    // The dark tier is drawn without a sheen: any white lift over it breaks the selected pill's 3:1.
    private fun darkTierStack(palette: AppPalette) = backdrops(palette).map { (where, backdrop) ->
        where to palette.glassOverVideo.compositeOver(backdrop)
    }

    private fun checkEveryTheme(checks: ContrastChecks.(AppPalette) -> Unit) {
        val failures = THEME_IDS.flatMap { themeId ->
            ContrastChecks(themeId).apply { checks(AppPalette.forTheme(themeId)) }.failures
        }
        assertWithMessage(failures.joinToString("\n")).that(failures).isEmpty()
    }

    private class ContrastChecks(private val themeId: String) {
        val failures = mutableListOf<String>()

        fun expect(pair: String, foreground: Color, background: Color, minimum: Float) {
            val ratio = contrastRatio(foreground, background)
            if (ratio < minimum) failures += "$themeId $pair: %.2f:1, expected at least %.1f:1".format(ratio, minimum)
        }
    }

    private companion object {
        const val TEXT = 4.5f
        const val NON_TEXT = 3f

        // Warm artwork tone from the approved mock; bright enough to lift translucent glass the most after white.
        val SaturatedMidTone = Color(0xFFC98A4B)

        val THEME_IDS = listOf(
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

        fun contrastRatio(first: Color, second: Color): Float {
            val firstLuminance = first.luminance()
            val secondLuminance = second.luminance()
            return (maxOf(firstLuminance, secondLuminance) + 0.05f) /
                (minOf(firstLuminance, secondLuminance) + 0.05f)
        }
    }
}
