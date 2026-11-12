package com.streamvault.core.ui.design

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.streamvault.core.ui.theme.standardMaterialColorScheme
import org.junit.Test

class AppPaletteTest {

    @Test
    fun `classic blue preserves the existing palette`() {
        val palette = AppPalette.forTheme("classic_blue")

        assertThat(palette.brand).isEqualTo(Color(0xFF69A8FF))
        assertThat(palette.canvas).isEqualTo(Color(0xFF07111B))
        assertThat(palette.surface).isEqualTo(Color(0xFF0F1B29))
        assertThat(palette.onPrimary).isEqualTo(Color.White)
    }

    @Test
    fun `m3 purple uses a distinct primary and neutral surface palette`() {
        val palette = AppPalette.forTheme("m3_purple")

        assertThat(palette.brand).isEqualTo(Color(0xFFD0BCFF))
        assertThat(palette.canvas).isEqualTo(Color(0xFF0A0A0D))
        assertThat(palette.surface).isEqualTo(Color(0xFF141419))
        assertThat(palette.onPrimary).isEqualTo(Color(0xFF381E72))
    }

    @Test
    fun `light theme uses a light surface palette and readable dark text`() {
        val palette = AppPalette.forTheme("light")

        assertThat(palette.canvas).isEqualTo(Color(0xFFF4F7FB))
        assertThat(palette.surface).isEqualTo(Color.White)
        assertThat(palette.brand).isEqualTo(Color(0xFF185FB4))
        assertThat(palette.textPrimary).isEqualTo(Color(0xFF132033))
        assertThat(palette.isLight).isTrue()
    }

    @Test
    fun `unknown theme IDs resolve to classic blue`() {
        assertThat(AppPalette.forTheme("unknown")).isEqualTo(AppPalette.forTheme("classic_blue"))
        assertThat(AppPalette.forTheme(null)).isEqualTo(AppPalette.forTheme("classic_blue"))
    }

    @Test
    fun `dark glass tier is a black scrim of at least 55 percent in every theme`() {
        ALL_THEME_IDS.forEach { themeId ->
            val overVideo = AppPalette.forTheme(themeId).glassOverVideo

            assertThat(overVideo.copy(alpha = 1f)).isEqualTo(Color.Black)
            assertThat(overVideo.alpha).isAtLeast(0.55f)
        }
    }

    @Test
    fun `only light themes draw an opaque base under glass`() {
        ALL_THEME_IDS.forEach { themeId ->
            val palette = AppPalette.forTheme(themeId)

            assertThat(palette.glassBase.alpha).isEqualTo(if (palette.isLight) 1f else 0f)
        }
    }

    @Test
    fun `nav selected and dark tier focus fills are opaque`() {
        ALL_THEME_IDS.forEach { themeId ->
            val palette = AppPalette.forTheme(themeId)

            assertThat(palette.navSelected.alpha).isEqualTo(1f)
            assertThat(palette.focusOverVideo.alpha).isEqualTo(1f)
        }
    }

    @Test
    fun `standard material controls receive the same semantic palette`() {
        listOf("classic_blue", "m3_purple", "light").forEach { themeId ->
            val palette = AppPalette.forTheme(themeId)
            val scheme = standardMaterialColorScheme(palette)

            assertThat(scheme.primary).isEqualTo(palette.brand)
            assertThat(scheme.surface).isEqualTo(palette.surface)
            assertThat(scheme.onSurface).isEqualTo(palette.textPrimary)
            assertThat(scheme.surfaceVariant).isEqualTo(palette.surfaceElevated)
            assertThat(scheme.onSurfaceVariant).isEqualTo(palette.textSecondary)
        }
    }

    private companion object {
        val ALL_THEME_IDS = listOf(
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
    }
}
