package com.streamvault.core.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class GlassLayersTest {

    @Test
    fun `dark theme bar keeps its translucent fill and sheen`() {
        val palette = AppPalette.classicBlue()
        val layers = glassLayersFor(palette, GlassTier.Bar, reducedEffects = false)

        assertColor(layers.fill, palette.glassBar)
        assertThat(layers.sheen).isEqualTo(palette.glassSheen)
        assertThat(layers.stroke).isEqualTo(palette.glassStroke)
        assertThat(layers.highlight).isEqualTo(palette.glassHighlight)
    }

    @Test
    fun `light theme glass folds its opaque base into one fill`() {
        listOf(AppPalette.light(), AppPalette.vintage()).forEach { palette ->
            val bar = glassLayersFor(palette, GlassTier.Bar, reducedEffects = false)
            val panel = glassLayersFor(palette, GlassTier.Panel, reducedEffects = false)

            assertColor(bar.fill, palette.glassBar.compositeOver(palette.glassBase))
            assertColor(panel.fill, palette.glassPanel.compositeOver(palette.glassBase))
            assertThat(bar.fill.alpha).isEqualTo(1f)
        }
    }

    @Test
    fun `panel tier uses the panel fill`() {
        val palette = AppPalette.forest()

        assertColor(glassLayersFor(palette, GlassTier.Panel, reducedEffects = false).fill, palette.glassPanel)
    }

    @Test
    fun `dark tier is identical in every theme and has no sheen`() {
        val layers = ALL_PALETTES.map { glassLayersFor(it, GlassTier.OverVideo, reducedEffects = false) }

        assertThat(layers.toSet()).hasSize(1)
        assertThat(layers.first().fill).isEqualTo(AppPalette.light().glassOverVideo)
        assertThat(layers.first().sheen).isNull()
    }

    @Test
    fun `reduced effects draw every tier opaque without a sheen`() {
        ALL_PALETTES.forEach { palette ->
            GlassTier.entries.forEach { tier ->
                val layers = glassLayersFor(palette, tier, reducedEffects = true)

                assertWithMessage("$tier fill alpha").that(layers.fill.alpha).isEqualTo(1f)
                assertWithMessage("$tier sheen").that(layers.sheen).isNull()
            }
        }
    }

    private fun assertColor(actual: Color, expected: Color) {
        listOf(
            actual.red to expected.red,
            actual.green to expected.green,
            actual.blue to expected.blue,
            actual.alpha to expected.alpha,
        ).forEach { (a, e) -> assertThat(a).isWithin(0.002f).of(e) }
    }

    private companion object {
        val ALL_PALETTES = listOf(
            AppPalette.classicBlue(),
            AppPalette.m3Purple(),
            AppPalette.light(),
            AppPalette.forest(),
            AppPalette.sunset(),
            AppPalette.ocean(),
            AppPalette.rose(),
            AppPalette.amoledBlack(),
            AppPalette.vintage(),
        )
    }
}
