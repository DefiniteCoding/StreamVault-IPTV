package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.streamvault.core.ui.components.dialogs.PremiumDialogButtonColors
import com.streamvault.core.ui.components.dialogs.premiumDialogButtonColors
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.contentOnColor
import com.streamvault.core.ui.design.forBrowsing
import org.junit.Test

/**
 * Checks the colors PremiumDialog buttons resolve to, in browse mode (browse palette) and in app mode (raw palette),
 * for all nine themes. The dialog panel is `surfaceElevated` of whichever palette the dialog reads.
 */
class PremiumDialogButtonColorsTest {

    private val rest = 0.2f

    private companion object {
        const val RING_AGAINST_FILL = 1.5f
    }

    private fun everyPalette(checks: ContrastChecks.(palette: AppPalette, browseVisuals: Boolean) -> Unit) {
        val failures = ALL_THEME_IDS.flatMap { themeId ->
            listOf(true, false).flatMap { browseVisuals ->
                val raw = AppPalette.forTheme(themeId)
                val palette = if (browseVisuals) raw.forBrowsing() else raw
                val kind = if (browseVisuals) "browse" else "app"
                ContrastChecks("$themeId/$kind").apply { checks(palette, browseVisuals) }.failures
            }
        }
        assertWithMessage(failures.joinToString("\n")).that(failures).isEmpty()
    }

    private fun colors(palette: AppPalette, browseVisuals: Boolean, destructive: Boolean = false, emphasized: Boolean = false) =
        premiumDialogButtonColors(palette, browseVisuals, destructive, emphasized, destructiveRestAlpha = rest)

    @Test
    fun `secondary label is readable on its rest and focused fill`() = everyPalette { palette, browse ->
        val secondary = colors(palette, browse)
        expect("secondary rest label", secondary.content, secondary.container.over(palette.surfaceElevated), TEXT_CONTRAST)
        expect("secondary focused label", secondary.focusedContent, secondary.focusedContainer, TEXT_CONTRAST)
    }

    @Test
    fun `primary label is readable on the brand fill in every state`() = everyPalette { palette, browse ->
        val primary = colors(palette, browse, emphasized = true)
        expect("primary rest label", primary.content, primary.container, TEXT_CONTRAST)
        expect("primary focused label", primary.focusedContent, primary.focusedContainer, TEXT_CONTRAST)
    }

    @Test
    fun `destructive label is readable on its tinted rest fill and its solid focused fill`() = everyPalette { palette, browse ->
        val destructive = colors(palette, browse, destructive = true)
        expect(
            "destructive rest label",
            destructive.content,
            destructive.container.over(palette.surfaceElevated),
            TEXT_CONTRAST
        )
        expect("destructive focused label", destructive.focusedContent, destructive.focusedContainer, TEXT_CONTRAST)
    }

    @Test
    fun `disabled label stays visible and differs from the enabled label`() = everyPalette { palette, browse ->
        val disabled = colors(palette, browse)
        expect("disabled label", disabled.disabledContent, disabled.disabledContainer, NON_TEXT_CONTRAST)
        if (disabled.disabledContent == palette.textPrimary) failures += "disabled label equals the enabled label"
    }

    // The ring's outer edge meets the panel (3:1). Its inner edge meets the fill, which a focused primary keeps, so
    // there the scale change carries focus and the ring only has to read apart from the fill, as for selected chips.
    @Test
    fun `focus ring stands out from the panel and reads apart from every fill`() = everyPalette { palette, browse ->
        expect("ring against panel", palette.focus, palette.surfaceElevated, NON_TEXT_CONTRAST)
        listOf(
            "secondary" to colors(palette, browse),
            "primary" to colors(palette, browse, emphasized = true),
            "destructive" to colors(palette, browse, destructive = true),
        ).forEach { (name, button) ->
            expect("ring against $name rest fill", palette.focus, button.container.over(palette.surfaceElevated), RING_AGAINST_FILL)
        }
    }

    // Rule 15: a focused secondary must not read as a resting primary, and a focused primary not as a resting secondary.
    @Test
    fun `focused fills are distinguishable from the other role's resting fill`() = everyPalette { palette, browse ->
        val primary = colors(palette, browse, emphasized = true)
        val secondary = colors(palette, browse)
        expect("focused primary against resting secondary", primary.focusedContainer, secondary.container, 1.5f)
        expect("focused secondary against resting primary", secondary.focusedContainer, primary.container, 1.5f)
    }

    @Test
    fun `a focused primary keeps the brand fill and its rest ink`() {
        val palette = AppPalette.classicBlue().forBrowsing()
        val primary = colors(palette, browseVisuals = true, emphasized = true)
        assertThat(primary.focusedContainer).isEqualTo(palette.brand)
        assertThat(primary.focusedContent).isEqualTo(primary.content)
    }

    @Test
    fun `primary ink is chosen from the brand fill in both modes`() {
        val palette = AppPalette.light()
        listOf(false to palette, true to palette.forBrowsing()).forEach { (browse, resolved) ->
            assertThat(colors(resolved, browse, emphasized = true).content).isEqualTo(contentOnColor(resolved.brand))
        }
    }

    @Test
    fun `destructive rest tint follows the alpha the caller passes`() {
        val palette = AppPalette.classicBlue()
        val strong: PremiumDialogButtonColors = premiumDialogButtonColors(palette, false, true, false, 0.22f)
        val soft: PremiumDialogButtonColors = premiumDialogButtonColors(palette, false, true, false, 0.18f)
        assertThat(strong.container).isEqualTo(palette.live.copy(alpha = 0.22f))
        assertThat(soft.container).isEqualTo(palette.live.copy(alpha = 0.18f))
    }

    private fun Color.over(background: Color): Color = compositeOver(background)
}
