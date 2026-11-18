package com.streamvault.core.ui.components.shell

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.streamvault.core.ui.components.NON_TEXT_CONTRAST
import com.streamvault.core.ui.components.TEXT_CONTRAST
import com.streamvault.core.ui.components.checkEveryBrowsePalette
import com.streamvault.core.ui.design.contentOnColor
import org.junit.Test

/**
 * Pairs drawn by the Home refresh: Continue Watching card captions and chips, the channel card pills, and the update
 * and warning panels. Artwork is unknown, so ink over artwork is checked on white and black, the worst cases.
 * Alphas and the pill ink rule come from the same constants the screens use.
 */
class HomeSurfacesContrastTest {

    // The card sits on `surface` at rest and on `surfaceEmphasis` when focused (the CatalogCards FocusableCard colors).
    @Test
    fun `continue watching caption is readable on the card in both states`() = checkEveryBrowsePalette { browse ->
        listOf("rest" to browse.surface, "focused" to browse.surfaceEmphasis).forEach { (state, card) ->
            expect("title (13sp) on $state card", browse.textPrimary, card, TEXT_CONTRAST)
            expect("meta (12sp) on $state card", browse.textSecondary, card, TEXT_CONTRAST)
        }
    }

    // 18sp regular title: the large-text threshold applies.
    @Test
    fun `continue watching art fallback label is readable`() = checkEveryBrowsePalette { browse ->
        expect("fallback label on art placeholder", browse.textSecondary, browse.surfaceElevated, NON_TEXT_CONTRAST)
    }

    @Test
    fun `time left chip and progress bar hold on any artwork`() = checkEveryBrowsePalette {
        ARTWORK_EXTREMES.forEach { (where, artwork) ->
            val scrim = Color.Black.copy(alpha = BadgeScrimAlpha).compositeOver(artwork)
            expect("time left text over $where artwork", Color.White, scrim, TEXT_CONTRAST)
            expect("progress fill against track over $where artwork", Color.White, scrim, NON_TEXT_CONTRAST)
        }
    }

    @Test
    fun `channel card pills are readable on any artwork`() = checkEveryBrowsePalette { browse ->
        ARTWORK_EXTREMES.forEach { (where, artwork) ->
            val catchUp = browse.brand.copy(alpha = ArtworkPillAlpha).compositeOver(artwork)
            expect("CATCH UP text over $where artwork", browse.onPrimary, catchUp, TEXT_CONTRAST)
            val live = browse.live.copy(alpha = ArtworkPillAlpha).compositeOver(artwork)
            expect("LIVE text over $where artwork", contentOnColor(browse.live), live, TEXT_CONTRAST)
        }
    }

    @Test
    fun `update notice and warning card text is readable on the panel`() = checkEveryBrowsePalette { browse ->
        expect("notice and warning title on panel", browse.textPrimary, browse.surfaceElevated, TEXT_CONTRAST)
        expect("body (textTertiary) on panel", browse.textTertiary, browse.surfaceElevated, TEXT_CONTRAST)
    }

    private companion object {
        val ARTWORK_EXTREMES = listOf("white" to Color.White, "black" to Color.Black)
    }
}
