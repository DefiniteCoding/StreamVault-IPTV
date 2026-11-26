package com.streamvault.core.ui.components.shell

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.google.common.truth.Truth.assertThat
import com.streamvault.core.ui.components.NON_TEXT_CONTRAST
import com.streamvault.core.ui.components.TEXT_CONTRAST
import com.streamvault.core.ui.components.checkEveryBrowsePalette
import org.junit.Test

/**
 * Checks the colors badges draw ([badgeColors]). Badge text is 14sp SemiBold, which is not bold enough for the
 * large-text threshold, so it needs 4.5:1. The badge fill itself carries no meaning (the text does), so only ink
 * against fill is checked.
 */
class BadgePaletteContrastTest {

    @Test
    fun `scrim meets the rule 9 minimum`() {
        assertThat(BadgeScrimAlpha).isAtLeast(MIN_SCRIM_ALPHA)
    }

    // Artwork is unknown, so the scrim is composited over the extremes. White is the worst case for bright ink.
    @Test
    fun `badge over artwork is readable on any artwork in every theme`() = checkEveryBrowsePalette { browse ->
        val colors = badgeColors(BadgeTone.OverArtwork, browse)
        ARTWORK_EXTREMES.forEach { (where, artwork) ->
            val fill = colors.container.compositeOver(artwork)
            expect("text over $where artwork", colors.content, fill, TEXT_CONTRAST)
            expect("star over $where artwork", colors.star, fill, NON_TEXT_CONTRAST)
        }
    }

    @Test
    fun `badge on a surface is readable in every theme`() = checkEveryBrowsePalette { browse ->
        val colors = badgeColors(BadgeTone.OnSurface, browse)
        expect("text on control container", colors.content, colors.container, TEXT_CONTRAST)
        expect("star on control container", colors.star, colors.container, NON_TEXT_CONTRAST)
    }

    private companion object {
        const val MIN_SCRIM_ALPHA = 0.55f
        val ARTWORK_EXTREMES = listOf("white" to Color.White, "black" to Color.Black)
    }
}
