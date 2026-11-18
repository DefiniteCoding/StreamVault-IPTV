package com.streamvault.core.ui.components

import org.junit.Test

/** Checks the inks [searchInputColors] draws in every theme, at rest and focused (tonal selected fill plus ring, rule 15). */
class SearchInputContrastTest {

    @Test
    fun `text placeholder and icon are readable at rest`() = checkEveryBrowsePalette { browse ->
        val rest = searchInputColors(browse, focused = false)
        expect("value on rest container", rest.text, rest.container, TEXT_CONTRAST)
        expect("placeholder on rest container", rest.placeholder, rest.container, TEXT_CONTRAST)
        expect("icon on rest container", rest.icon, rest.container, NON_TEXT_CONTRAST)
        expect("cursor on rest container", rest.cursor, rest.container, NON_TEXT_CONTRAST)
    }

    @Test
    fun `text placeholder icon and cursor are readable when focused`() = checkEveryBrowsePalette { browse ->
        val focused = searchInputColors(browse, focused = true)
        expect("value on focused container", focused.text, focused.container, TEXT_CONTRAST)
        expect("placeholder on focused container", focused.placeholder, focused.container, TEXT_CONTRAST)
        expect("icon on focused container", focused.icon, focused.container, NON_TEXT_CONTRAST)
        expect("cursor on focused container", focused.cursor, focused.container, NON_TEXT_CONTRAST)
    }

    // Rule 15: nothing at rest may look focused, and the focus fill plus its ring must stand out from the page.
    @Test
    fun `focused field reads apart from rest and from the page`() = checkEveryBrowsePalette { browse ->
        val rest = searchInputColors(browse, focused = false)
        val focused = searchInputColors(browse, focused = true)
        expect("rest container against focused container", rest.container, focused.container, NON_TEXT_CONTRAST)
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("focus ring against $where", browse.focus, backdrop, NON_TEXT_CONTRAST)
            expect("focused container against $where", focused.container, backdrop, NON_TEXT_CONTRAST)
        }
    }
}
