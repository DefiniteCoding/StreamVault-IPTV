package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.streamvault.core.ui.components.shell.BadgeScrimAlpha
import org.junit.Test

/**
 * Pairs drawn by the series detail episode card (CatalogEpisodeRowCard.kt, `EpisodeCard`) on the browse palette.
 * The badge is white over a the badge scrim scrim, so its worst case is white artwork under the scrim.
 */
class EpisodeCardContrastTest {

    @Test
    fun `card text is readable on the card fill`() = checkEveryBrowsePalette { browse ->
        expect("title (textPrimary) on surfaceEmphasis", browse.textPrimary, browse.surfaceEmphasis, TEXT_CONTRAST)
        expect("duration (textSecondary) on surfaceEmphasis", browse.textSecondary, browse.surfaceEmphasis, TEXT_CONTRAST)
    }

    @Test
    fun `episode badge is readable over the lightest possible artwork`() = checkEveryBrowsePalette { _ ->
        val scrimOverWhiteArt = Color.Black.copy(alpha = BadgeScrimAlpha).compositeOver(Color.White)
        expect("white badge text on the badge scrim over white art", Color.White, scrimOverWhiteArt, TEXT_CONTRAST)
    }

    @Test
    fun `download icon is readable at rest and focused`() = checkEveryBrowsePalette { browse ->
        expect("icon (textPrimary) on controlContainer", browse.textPrimary, browse.controlContainer, NON_TEXT_CONTRAST)
        expect("icon (textPrimary) on surfaceAccent", browse.textPrimary, browse.surfaceAccent, NON_TEXT_CONTRAST)
    }

    @Test
    fun `focus ring stands out from the card, the icon fill and every page backdrop`() = checkEveryBrowsePalette { browse ->
        expect("focus ring on surfaceEmphasis (card fill)", browse.focus, browse.surfaceEmphasis, NON_TEXT_CONTRAST)
        expect("focus ring on surfaceAccent", browse.focus, browse.surfaceAccent, NON_TEXT_CONTRAST)
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("focus ring on $where", browse.focus, backdrop, NON_TEXT_CONTRAST)
        }
    }

    @Test
    fun `resume progress stands out from its track`() = checkEveryBrowsePalette { browse ->
        expect("brand progress on surfaceEmphasis track", browse.brand, browse.surfaceEmphasis, NON_TEXT_CONTRAST)
    }

    @Test
    fun `card fill stands apart from the page it sits on`() = checkEveryBrowsePalette { browse ->
        plainBackdrops(browse).forEach { (where, backdrop) ->
            expect("card fill (surfaceEmphasis) on $where", browse.surfaceEmphasis, backdrop, CARD_SEPARATION)
        }
    }

    @Test
    fun `tertiary detail action label is readable over the lightest possible artwork`() = checkEveryBrowsePalette { _ ->
        val scrimOverWhiteArt = Color.Black.copy(alpha = BadgeScrimAlpha).compositeOver(Color.White)
        expect("white label on the badge scrim over white art", Color.White, scrimOverWhiteArt, TEXT_CONTRAST)
    }

    private companion object {
        // A card body cannot reach the 3:1 of meaningful UI without turning into a slab; this floor only keeps it
        // from melting into the page (surfaceElevated measured 1.07 to 1.41:1, 1.12 on amoled_black).
        const val CARD_SEPARATION = 1.15f
    }
}
