package com.streamvault.core.ui.components

import org.junit.Test

/** Pairs on the player error overlay card: the opaque theme surface with theme ink, so the frame behind it never matters. */
class ErrorOverlayContrastTest {

    @Test
    fun `error overlay text and title are readable on the card`() = checkEveryBrowsePalette { browse ->
        expect("message on card", browse.textPrimary, browse.surface, TEXT_CONTRAST)
        expect("hint on card", browse.textSecondary, browse.surface, TEXT_CONTRAST)
        expect("error title on card", browse.live, browse.surface, TEXT_CONTRAST)
    }

    @Test
    fun `focus ring and buttons stand out from the card`() = checkEveryBrowsePalette { browse ->
        expect("focus ring against card", browse.focus, browse.surface, NON_TEXT_CONTRAST)
        expect("primary fill against card", browse.brand, browse.surface, NON_TEXT_CONTRAST)
    }
}
