package com.streamvault.core.ui.components

import org.junit.Test

/** Checks the inks [inputFieldColors] draws in every theme, at rest and focused (tonal selected fill plus ring, rule 15). */
class SearchInputContrastTest {

    // Rule 7 for a stroke-free input: the fill is the only mark of the field, so it holds 3:1 on its backdrops.
    @Test
    fun `rest fill holds 3 to 1 against the canvas and the surface`() = checkEveryBrowsePalette { browse ->
        val rest = inputFieldColors(browse, focused = false)
        expect("rest container against canvas", rest.container, browse.canvas, NON_TEXT_CONTRAST)
        expect("rest container against surface", rest.container, browse.surface, NON_TEXT_CONTRAST)
    }

    @Test
    fun `text placeholder and icon are readable at rest`() = checkEveryBrowsePalette { browse ->
        val rest = inputFieldColors(browse, focused = false)
        expect("value on rest container", rest.text, rest.container, TEXT_CONTRAST)
        expect("placeholder on rest container", rest.placeholder, rest.container, TEXT_CONTRAST)
        expect("icon on rest container", rest.icon, rest.container, NON_TEXT_CONTRAST)
        expect("cursor on rest container", rest.cursor, rest.container, NON_TEXT_CONTRAST)
    }

    @Test
    fun `text placeholder icon and cursor are readable when focused`() = checkEveryBrowsePalette { browse ->
        val focused = inputFieldColors(browse, focused = true)
        expect("value on focused container", focused.text, focused.container, TEXT_CONTRAST)
        expect("placeholder on focused container", focused.placeholder, focused.container, TEXT_CONTRAST)
        expect("icon on focused container", focused.icon, focused.container, NON_TEXT_CONTRAST)
        expect("cursor on focused container", focused.cursor, focused.container, NON_TEXT_CONTRAST)
    }

    // Rule 15: nothing at rest may look focused, and the focus fill plus its ring must stand out from the page.
    @Test
    fun `focused field reads apart from rest and from the page`() = checkEveryBrowsePalette { browse ->
        val rest = inputFieldColors(browse, focused = false)
        val focused = inputFieldColors(browse, focused = true)
        // Light themes cannot separate two mid-tone fills further; the ring and scale carry the rest.
        expect("rest container against focused container", rest.container, focused.container, FILL_APART)
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("focus ring against $where", browse.focus, backdrop, NON_TEXT_CONTRAST)
            expect("focused container against $where", focused.container, backdrop, NON_TEXT_CONTRAST)
        }
    }

    private companion object {
        const val FILL_APART = 1.4f
    }
}
