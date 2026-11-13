package com.streamvault.core.ui.components

import com.streamvault.core.ui.components.shell.PillColors
import com.streamvault.core.ui.components.shell.sectionActionColors
import com.streamvault.core.ui.design.AppPalette
import org.junit.Test

/**
 * Checks the colors section header actions actually draw ([sectionActionColors] on the browse palette) against the
 * page backdrops a header can sit on. The action has no fill at rest, so its rest ink sits on the page itself.
 * Pressed actions reuse the focused colors, so they add no pairs.
 */
class SectionActionContrastTest {

    @Test
    fun `rest label and icon are readable on every page backdrop`() = checkEveryTheme { browse, action ->
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("rest ink on $where", action.restContent, backdrop, TEXT_CONTRAST)
        }
    }

    @Test
    fun `selected and focused ink is readable on its container`() = checkEveryTheme { _, action ->
        expect("selected icon on selected container", action.selectedContent, action.selectedContainer, TEXT_CONTRAST)
        expect("focused label and chevron on focused container", action.focusedContent, action.focusedContainer, TEXT_CONTRAST)
    }

    @Test
    fun `focused action and its ring stand out from every page backdrop`() = checkEveryTheme { browse, action ->
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("focus ring against $where", action.ring, backdrop, NON_TEXT_CONTRAST)
            expect("focused container against $where", action.focusedContainer, backdrop, NON_TEXT_CONTRAST)
        }
    }

    @Test
    fun `selected fill stands out from every page backdrop`() = checkEveryTheme { browse, action ->
        pageBackdrops(browse).forEach { (where, backdrop) ->
            expect("selected container against $where", action.selectedContainer, backdrop, NON_TEXT_CONTRAST)
        }
    }

    // Rule 15: nothing at rest may look focused. The rest container is transparent, so the rest state is the page
    // backdrop itself, covered by the focused container pair above; selected is the only fill that can be confused.
    @Test
    fun `selected action never reads as focused`() = checkEveryTheme { _, action ->
        expect("selected container against focus", action.selectedContainer, action.focusedContainer, NON_TEXT_CONTRAST)
    }

    private fun checkEveryTheme(checks: ContrastChecks.(AppPalette, PillColors) -> Unit) =
        checkEveryBrowsePalette { browse -> checks(browse, sectionActionColors(browse)) }
}
