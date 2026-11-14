package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Test

/**
 * Pairs drawn by the series detail episode card (CatalogEpisodeRowCard.kt, `EpisodeCard`) on the browse palette.
 * The badge is white over a 55% black scrim, so its worst case is white artwork under the scrim.
 */
class EpisodeCardContrastTest {

    @Test
    fun `card text is readable on the card fill`() = checkEveryBrowsePalette { browse ->
        expect("title (textPrimary) on surfaceElevated", browse.textPrimary, browse.surfaceElevated, TEXT_CONTRAST)
        expect("duration (textSecondary) on surfaceElevated", browse.textSecondary, browse.surfaceElevated, TEXT_CONTRAST)
    }

    @Test
    fun `episode badge is readable over the lightest possible artwork`() = checkEveryBrowsePalette { _ ->
        val scrimOverWhiteArt = Color.Black.copy(alpha = BADGE_SCRIM_ALPHA).compositeOver(Color.White)
        expect("white badge text on 55% black over white art", Color.White, scrimOverWhiteArt, TEXT_CONTRAST)
    }

    @Test
    fun `download icon is readable at rest and focused`() = checkEveryBrowsePalette { browse ->
        expect("icon (textPrimary) on controlContainer", browse.textPrimary, browse.controlContainer, NON_TEXT_CONTRAST)
        expect("icon (textPrimary) on surfaceAccent", browse.textPrimary, browse.surfaceAccent, NON_TEXT_CONTRAST)
    }

    @Test
    fun `focus ring stands out from the card, the icon fill and every page backdrop`() = checkEveryBrowsePalette { browse ->
        expect("focus ring on surfaceElevated", browse.focus, browse.surfaceElevated, NON_TEXT_CONTRAST)
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
    fun `tertiary detail action label is readable over the lightest possible artwork`() = checkEveryBrowsePalette { _ ->
        val scrimOverWhiteArt = Color.Black.copy(alpha = BADGE_SCRIM_ALPHA).compositeOver(Color.White)
        expect("white label on 55% black over white art", Color.White, scrimOverWhiteArt, TEXT_CONTRAST)
    }

    private companion object {
        // Mirrors OVER_ARTWORK_SCRIM_ALPHA in the catalog module, which core:ui cannot depend on.
        const val BADGE_SCRIM_ALPHA = 0.55f
    }
}
