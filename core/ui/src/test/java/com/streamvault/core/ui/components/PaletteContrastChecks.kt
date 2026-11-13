package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.google.common.truth.Truth.assertWithMessage
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.forBrowsing

/** WCAG thresholds from project rule 7. */
internal const val TEXT_CONTRAST = 4.5f
internal const val NON_TEXT_CONTRAST = 3f

internal val ALL_THEME_IDS = listOf(
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

/** Runs [checks] on the browse palette of every theme and fails once, listing every pair below its threshold. */
internal fun checkEveryBrowsePalette(checks: ContrastChecks.(AppPalette) -> Unit) {
    val failures = ALL_THEME_IDS.flatMap { themeId ->
        ContrastChecks(themeId).apply { checks(AppPalette.forTheme(themeId).forBrowsing()) }.failures
    }
    assertWithMessage(failures.joinToString("\n")).that(failures).isEmpty()
}

internal class ContrastChecks(private val themeId: String) {
    val failures = mutableListOf<String>()

    fun expect(pair: String, foreground: Color, background: Color, minimum: Float) {
        val ratio = contrastRatio(foreground, background)
        if (ratio < minimum) failures += "$themeId $pair: %.2f:1, expected at least %.1f:1".format(ratio, minimum)
    }
}

internal fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    return (maxOf(firstLuminance, secondLuminance) + 0.05f) / (minOf(firstLuminance, secondLuminance) + 0.05f)
}

/**
 * Backdrops a chip row or a section header action can sit on: the browse canvas (both gradient ends, also under the
 * brand ambient light at its strongest) and browse surfaces. SurfaceElevated panels and the Live strip's AccentSoft
 * aurora are excluded: controls there lose too much contrast.
 */
internal fun pageBackdrops(browse: AppPalette) = plainBackdrops(browse) + ambientBackdrops(browse)

internal fun plainBackdrops(browse: AppPalette) = listOf(
    "Canvas" to browse.canvas,
    "CanvasElevated" to browse.canvasElevated,
    "Surface" to browse.surface,
)

internal fun ambientBackdrops(browse: AppPalette): List<Pair<String, Color>> {
    val ambient = browse.brand.copy(alpha = if (browse.isLight) 0.07f else 0.10f)
    return listOf(
        "Canvas under ambient" to ambient.compositeOver(browse.canvas),
        "CanvasElevated under ambient" to ambient.compositeOver(browse.canvasElevated),
    )
}
