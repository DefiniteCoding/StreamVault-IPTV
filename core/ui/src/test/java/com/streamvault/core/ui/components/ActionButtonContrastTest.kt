package com.streamvault.core.ui.components

import org.junit.Test

/**
 * Checks the colors [AppDialogButton] actually draws against what it sits on: the dialog panel (browse `surface`).
 * Pressed buttons reuse the focused colors, so they add no pairs.
 */
class ActionButtonContrastTest {

    @Test
    fun `dialog text is readable on the panel`() = checkEveryBrowsePalette { browse ->
        expect("title on panel", browse.textPrimary, browse.surface, TEXT_CONTRAST)
        expect("body on panel", browse.textSecondary, browse.surface, TEXT_CONTRAST)
    }

    @Test
    fun `primary label is readable on the brand fill in every state`() = checkEveryBrowsePalette { browse ->
        val primary = dialogButtonColors(browse, primary = true)
        expect("primary rest label", primary.restContent, primary.restContainer, TEXT_CONTRAST)
        expect("primary focused label", primary.focusedContent, primary.focusedContainer, TEXT_CONTRAST)
    }

    @Test
    fun `secondary label is readable on its fill in every state`() = checkEveryBrowsePalette { browse ->
        val secondary = dialogButtonColors(browse, primary = false)
        expect("secondary rest label", secondary.restContent, secondary.restContainer, TEXT_CONTRAST)
        expect("secondary focused label", secondary.focusedContent, secondary.focusedContainer, TEXT_CONTRAST)
    }

    @Test
    fun `focus ring and the primary fill stand out from the panel`() = checkEveryBrowsePalette { browse ->
        val primary = dialogButtonColors(browse, primary = true)
        expect("focus ring against panel", primary.ring, browse.surface, NON_TEXT_CONTRAST)
        expect("primary fill against panel", primary.restContainer, browse.surface, NON_TEXT_CONTRAST)
    }

    // Rule 15: the focused primary must not look like a resting secondary, and the other way round.
    @Test
    fun `primary and secondary fills are distinguishable`() = checkEveryBrowsePalette { browse ->
        val primary = dialogButtonColors(browse, primary = true)
        val secondary = dialogButtonColors(browse, primary = false)
        expect("primary fill against secondary fill", primary.restContainer, secondary.restContainer, 1.5f)
    }
}
