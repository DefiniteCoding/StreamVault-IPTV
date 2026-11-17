package com.streamvault.core.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowsePaletteContrastTest {
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

    @Test
    fun featuredCardContrastAcrossThemes() {
        allThemeIds.forEach { id ->
            val palette = AppPalette.forTheme(id).forBrowsing()
            mapOf("SurfaceElevated" to palette.surfaceElevated, "SurfaceAccent" to palette.surfaceAccent).forEach { (name, fill) ->
                mapOf("TextPrimary" to palette.textPrimary, "TextSecondary" to palette.textSecondary).forEach { (textName, ink) ->
                    val ratio = contrast(ink, fill)
                    println("FEATURED $id $textName/$name=$ratio")
                    assertTrue("$id $textName/$name $ratio", ratio >= 4.5f)
                }
                val ring = contrast(palette.focus, fill)
                println("FEATURED $id Focus/$name=$ring")
                assertTrue("$id focus/$name $ring", ring >= 3f)
            }
            val settings = AppPalette.forTheme(id)
            val selected = androidx.compose.ui.graphics.lerp(settings.surfaceElevated, settings.brand, 0.14f)
            val selectedText = contrast(settings.textPrimary, selected)
            val selectedRing = contrast(settings.focus, selected)
            println("FEATURED $id SettingsTextPrimary/Selected=$selectedText")
            println("FEATURED $id SettingsFocus/Selected=$selectedRing")
            assertTrue("$id selected settings text $selectedText", selectedText >= 4.5f)
            assertTrue("$id selected settings ring $selectedRing", selectedRing >= 3f)
            val cta = contrast(palette.onPrimary, palette.brand)
            println("FEATURED $id OnPrimary/Brand=$cta")
            assertTrue("$id CTA $cta", cta >= 4.5f)
        }
    }

    @Test
    fun allNineThemesResolveToDistinctPalettes() {
        assertEquals(9, allThemeIds.map { AppPalette.forTheme(it) }.toSet().size)
    }

    @Test
    fun focusRingIsVisibleOnEveryBrowseSurfaceInEveryTheme() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            mapOf(
                "canvas" to browse.canvas,
                "canvasElevated" to browse.canvasElevated,
                "surface" to browse.surface,
                "surfaceElevated" to browse.surfaceElevated,
                "surfaceAccent" to browse.surfaceAccent,
            ).forEach { (name, background) ->
                val ratio = contrast(browse.focus, background)
                assertTrue("$id focus ring on $name was $ratio", ratio >= 3f)
            }
        }
    }

    @Test
    fun selectedContainerKeepsLabelReadableAndSeparatesFromEveryBackground() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            val label = contrast(browse.onSelectedContainer, browse.selectedContainer)
            val floor = if (browse.isLight) 4.5f else 9f
            assertTrue("$id label on selectedContainer was $label", label >= floor)
            mapOf(
                "canvas" to browse.canvas,
                "surface" to browse.surface,
                "surfaceElevated" to browse.surfaceElevated,
            ).forEach { (name, background) ->
                val ratio = contrast(browse.selectedContainer, background)
                assertTrue("$id selectedContainer on $name was $ratio", ratio >= 3f)
            }
        }
    }

    // The selected fill is light enough in the dark themes to give its label 9:1, so the ring cannot also hold
    // 3:1 against the fill. The ring is judged against the surfaces around the chip and must still read apart
    // from the fill.
    @Test
    fun focusRingStandsOutAroundTheSelectedContainer() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            mapOf(
                "canvas" to browse.canvas,
                "surface" to browse.surface,
                "surfaceElevated" to browse.surfaceElevated,
                "controlContainer" to browse.controlContainer,
            ).forEach { (name, around) ->
                val ratio = contrast(browse.focus, around)
                assertTrue("$id focus ring on $name was $ratio", ratio >= 3f)
            }
            val onFill = contrast(browse.focus, browse.selectedContainer)
            assertTrue("$id focus ring on selectedContainer was $onFill", onFill >= 1.5f)
        }
    }

    // PremiumDialog paints a brandMuted tint at 18% over surfaceElevated that fades out over the first half of the
    // dialog. Option chips start about 20% down, so the backdrop there is 60% of that tint.
    @Test
    fun selectedChipSeparatesFromUnselectedChipsAndTheDialogBackdrop() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            val dialogTop = browse.brandMuted.copy(alpha = 0.18f).compositeOver(browse.surfaceElevated)
            val backdropAtChips = lerp(dialogTop, browse.surfaceElevated, 0.4f)
            val onBackdrop = contrast(browse.selectedContainer, backdropAtChips)
            assertTrue("$id selectedContainer on dialog backdrop was $onBackdrop", onBackdrop >= 3f)
            val onControl = contrast(browse.selectedContainer, browse.controlContainer)
            assertTrue("$id selectedContainer on controlContainer was $onControl", onControl >= 3f)
        }
    }

    // Setup screens: one filled-brand primary, every other action a controlContainer secondary that fills
    // with surfaceEmphasis on focus. Labels and the focus ring must hold on all three containers.
    @Test
    fun primaryAndSecondaryActionLabelsAndFocusRingAreReadable() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            val primary = contrast(browse.onPrimary, browse.brand)
            assertTrue("$id onPrimary on brand was $primary", primary >= 4.5f)
            val rest = contrast(browse.textPrimary, browse.controlContainer)
            assertTrue("$id textPrimary on controlContainer was $rest", rest >= 4.5f)
            val focused = contrast(browse.textPrimary, browse.surfaceEmphasis)
            assertTrue("$id textPrimary on surfaceEmphasis was $focused", focused >= 4.5f)
            val ring = contrast(browse.focus, browse.surfaceEmphasis)
            assertTrue("$id focus ring on surfaceEmphasis was $ring", ring >= 3f)
        }
    }

    @Test
    fun focusedUnselectedLabelIsReadableOnAccentFill() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            val ratio = contrast(browse.textPrimary, browse.surfaceAccent)
            assertTrue("$id textPrimary on surfaceAccent was $ratio", ratio >= 4.5f)
            val secondary = contrast(browse.textSecondary, browse.surfaceAccent)
            assertTrue("$id textSecondary on surfaceAccent was $secondary", secondary >= 4.5f)
        }
    }

    @Test
    fun controlContainerSeparatesFromItsBackgroundsInEveryTheme() {
        allThemeIds.forEach { id ->
            val raw = AppPalette.forTheme(id)
            val browse = raw.forBrowsing()
            // A light theme's fill would turn heavy grey at 1.5:1, so it is held to a lower bar.
            val minimum = if (raw.isLight) 1.3f else 1.5f
            listOf(raw, browse).forEach { palette ->
                mapOf(
                    "canvas" to (palette.canvas to minimum),
                    "surface" to (palette.surface to minimum),
                ).forEach { (name, backgroundAndFloor) ->
                    val (background, floor) = backgroundAndFloor
                    val ratio = contrast(raw.controlContainer, background)
                    assertTrue("$id controlContainer on $name was $ratio", ratio >= floor)
                }
            }
            // The Search screen hero panel is a browse elevated surface; the field only needs to stay visible on it.
            val onElevated = contrast(raw.controlContainer, browse.surfaceElevated)
            assertTrue("$id controlContainer on browse surfaceElevated was $onElevated", onElevated >= 1.25f)
        }
    }

    @Test
    fun fieldTextAndRingAreReadableOnControlContainer() {
        allThemeIds.forEach { id ->
            val palette = AppPalette.forTheme(id)
            val placeholder = contrast(palette.textSecondary, palette.controlContainer)
            val typed = contrast(palette.textPrimary, palette.controlContainer)
            val ring = contrast(palette.focus, palette.controlContainer)
            assertTrue("$id placeholder on controlContainer was $placeholder", placeholder >= 4.5f)
            assertTrue("$id typed text on controlContainer was $typed", typed >= 4.5f)
            assertTrue("$id focus ring on controlContainer was $ring", ring >= 3f)
        }
    }

    @Test
    fun restingTextIsReadableOnCanvasAndSurfaceInEveryTheme() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            listOf(
                "canvas" to browse.canvas,
                "surface" to browse.surface,
                "surfaceElevated" to browse.surfaceElevated,
            ).forEach { (name, background) ->
                val primary = contrast(browse.textPrimary, background)
                val secondary = contrast(browse.textSecondary, background)
                assertTrue("$id textPrimary on $name was $primary", primary >= 4.5f)
                assertTrue("$id textSecondary on $name was $secondary", secondary >= 4.5f)
            }
        }
    }

    @Test
    fun loadingSpinnerBrandIsVisibleOnCanvasInEveryTheme() {
        allThemeIds.forEach { id ->
            val browse = AppPalette.forTheme(id).forBrowsing()
            val ratio = contrast(browse.brand, browse.canvas)
            assertTrue("$id brand on canvas was $ratio", ratio >= 3f)
        }
    }

    @Test
    fun browseFocusIsTheAppFocusToken() {
        allThemeIds.forEach { id ->
            val palette = AppPalette.forTheme(id)
            assertEquals(id, palette.focus, palette.forBrowsing().focus)
        }
    }

    @Test
    fun metadataRemainsReadableOnFocusedSurfaces() {
        listOf(AppPalette.classicBlue(), AppPalette.m3Purple(), AppPalette.light()).forEach {
            val browse = it.forBrowsing()
            val background = browse.surfaceEmphasis.luminance()
            val foreground = browse.textTertiary.luminance()
            val contrast = (maxOf(background, foreground) + 0.05f) /
                (minOf(background, foreground) + 0.05f)
            assertTrue("Metadata contrast was $contrast", contrast >= 4.5f)
        }
    }

    @Test
    fun primaryActionsPreserveAccentAndReadableInkAcrossPalettes() {
        val palettes = listOf(
            AppPalette.classicBlue(), AppPalette.m3Purple(), AppPalette.light(),
            AppPalette.classicBlue().copy(brand = Color(0xFF8EDADC)),
            AppPalette.classicBlue().copy(brand = Color(0xFF103858)),
        )
        palettes.forEach { original ->
            val browse = original.forBrowsing()
            assertEquals(original.brand, browse.brand)
            val background = browse.brand.luminance()
            val foreground = browse.onPrimary.luminance()
            val contrast = (maxOf(background, foreground) + 0.05f) /
                (minOf(background, foreground) + 0.05f)
            assertTrue("Primary action contrast was $contrast", contrast >= 4.5f)
        }
    }
}
