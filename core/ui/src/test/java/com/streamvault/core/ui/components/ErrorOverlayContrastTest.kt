package com.streamvault.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.streamvault.core.ui.theme.ErrorColor
import org.junit.Test

/**
 * Pairs behind the player error overlay card: the dark over-video tier, worst case over a white frame.
 */
class ErrorOverlayContrastTest {

    @Test
    fun `error overlay text is readable on the dark tier over any frame`() = checkEveryBrowsePalette { browse ->
        listOf("white frame" to Color.White, "black frame" to Color.Black).forEach { (where, frame) ->
            val card = browse.glassOverVideo.compositeOver(frame)
            expect("message ink on card over $where", browse.onGlassOverVideo, card, TEXT_CONTRAST)
            expect("error title on card over $where", ErrorColor, card, TEXT_CONTRAST)
        }
    }
}
