package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Test

/**
 * Pairs in the player episode picker, composited the way it draws: a 68% black scrim over the frame, the dialog card
 * at 98% canvasElevated, the season and episode panels at 78% and 70% surface, then the row fills on top.
 */
class EpisodePickerContrastTest {

    @Test
    fun `rest and focused rows keep their text readable over any frame`() = checkEveryBrowsePalette { browse ->
        listOf("white frame" to Color.White, "black frame" to Color.Black).forEach { (where, frame) ->
            val card = browse.canvasElevated.copy(alpha = 0.98f)
                .compositeOver(Color.Black.copy(alpha = 0.68f).compositeOver(frame))
            val seasonPanel = browse.surface.copy(alpha = 0.78f).compositeOver(card)
            val episodePanel = browse.surface.copy(alpha = 0.70f).compositeOver(card)
            val rows = mapOf(
                "season rest" to browse.surfaceElevated.copy(alpha = 0.62f).compositeOver(seasonPanel),
                "season focused" to browse.surface.copy(alpha = 0.76f).compositeOver(seasonPanel),
                "episode rest" to browse.surfaceElevated.copy(alpha = 0.44f).compositeOver(episodePanel),
                "episode focused" to browse.surface.copy(alpha = 0.74f).compositeOver(episodePanel),
            )
            rows.forEach { (row, fill) ->
                expect("title on $row over $where", browse.textPrimary, fill, TEXT_CONTRAST)
                expect("meta on $row over $where", browse.textSecondary, fill, TEXT_CONTRAST)
            }
        }
    }

    @Test
    fun `selected rows and the header pill read on the tonal selected fill`() = checkEveryBrowsePalette { browse ->
        expect("ink on selected fill", browse.onSelectedContainer, browse.selectedContainer, TEXT_CONTRAST)
    }
}
