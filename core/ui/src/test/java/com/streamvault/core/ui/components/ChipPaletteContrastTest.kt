package com.streamvault.core.ui.components

import com.streamvault.core.ui.components.shell.PillColors
import com.streamvault.core.ui.design.AppPalette
import org.junit.Test

/**
 * Checks the colors chips actually draw ([appChipColors] on the browse palette) against the page backdrops a chip
 * row can sit on. Pressed chips reuse the focused colors, so they add no pairs.
 */
class ChipPaletteContrastTest {

    @Test
    fun `chip labels and icons are readable in every state`() = checkEveryTheme { browse, chip ->
        expect("label on rest container", chip.restContent, chip.restContainer, TEXT)
        expect("dropdown label on rest container", browse.textSecondary, chip.restContainer, TEXT)
        expect("label and check on selected container", chip.selectedContent, chip.selectedContainer, TEXT)
        expect("label and check on focused container", chip.focusedContent, chip.focusedContainer, TEXT)
    }

    @Test
    fun `group label is readable on every page backdrop`() = checkEveryTheme { browse, _ ->
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("group label on $where", browse.textSecondary, backdrop, TEXT)
        }
    }

    @Test
    fun `focused chip and its ring stand out from every page backdrop`()= checkEveryTheme { browse, chip ->
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("focus ring against $where", chip.ring, backdrop, NON_TEXT)
            expect("focused container against $where", chip.focusedContainer, backdrop, NON_TEXT)
        }
    }

    // Rule 15: nothing at rest may look focused.
    @Test
    fun `resting and selected chips never read as focused`() = checkEveryTheme { _, chip ->
        expect("rest container against focus", chip.restContainer, chip.focusedContainer, NON_TEXT)
        expect("selected container against focus", chip.selectedContainer, chip.focusedContainer, NON_TEXT)
    }

    @Test
    fun `selected fill stands out from every page backdrop`() = checkEveryTheme { browse, chip ->
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("selected container against $where", chip.selectedContainer, backdrop, NON_TEXT)
        }
    }

    // The resting fill uses the input floor, not 3:1: the palette keeps unstroked controls quiet and the label
    // identifies the chip. Selection and focus, which carry meaning, are held to 3:1 above. Where the brand ambient
    // light peaks, the fill drops below the input floor, so that tint has its own, lower floor.
    @Test
    fun `resting chip stays visible on every page backdrop`() = checkEveryTheme { browse, chip ->
        val plainFloor = if (browse.isLight) LIGHT_CONTROL_FLOOR else DARK_CONTROL_FLOOR
        val ambientFloor = if (browse.isLight) LIGHT_AMBIENT_FLOOR else DARK_AMBIENT_FLOOR
        plainBackdrops(browse).forEach { (where, backdrop) ->
            expect("rest container against $where", chip.restContainer, backdrop, plainFloor)
        }
        ambientBackdrops(browse).forEach { (where, backdrop) ->
            expect("rest container against $where", chip.restContainer, backdrop, ambientFloor)
        }
    }

    private fun checkEveryTheme(checks: ContrastChecks.(AppPalette, PillColors) -> Unit) =
        checkEveryBrowsePalette { browse -> checks(browse, appChipColors(browse)) }

    private companion object {
        const val TEXT = TEXT_CONTRAST
        const val NON_TEXT = NON_TEXT_CONTRAST
        const val DARK_CONTROL_FLOOR = 1.5f
        const val LIGHT_CONTROL_FLOOR = 1.3f
        const val DARK_AMBIENT_FLOOR = 1.35f
        const val LIGHT_AMBIENT_FLOOR = 1.15f
    }
}
