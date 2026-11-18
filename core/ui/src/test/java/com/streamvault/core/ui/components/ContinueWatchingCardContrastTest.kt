package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.streamvault.core.ui.components.shell.BadgeScrimAlpha
import org.junit.Test

/** The Continue Watching card: caption on the card fill, and the chip and progress bar over artwork. */
class ContinueWatchingCardContrastTest {

    @Test
    fun `caption is readable on the card fill at rest and focused`() = checkEveryBrowsePalette { browse ->
        listOf("surface" to browse.surface, "surfaceEmphasis" to browse.surfaceEmphasis).forEach { (fill, background) ->
            expect("title (textPrimary) on $fill", browse.textPrimary, background, TEXT_CONTRAST)
            expect("meta (textSecondary) on $fill", browse.textSecondary, background, TEXT_CONTRAST)
        }
    }

    @Test
    fun `minutes left chip is readable over the lightest possible artwork`() = checkEveryBrowsePalette { _ ->
        val scrimOverWhiteArt = Color.Black.copy(alpha = BadgeScrimAlpha).compositeOver(Color.White)
        expect("white chip text on the badge scrim over white art", Color.White, scrimOverWhiteArt, TEXT_CONTRAST)
    }

    @Test
    fun `progress bar stands out from its track over the lightest possible artwork`() = checkEveryBrowsePalette { _ ->
        val trackOverWhiteArt = Color.Black.copy(alpha = BadgeScrimAlpha).compositeOver(Color.White)
        expect("white progress on the scrim track over white art", Color.White, trackOverWhiteArt, NON_TEXT_CONTRAST)
    }
}
