package com.streamvault.core.ui.components.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TopBarSearchTest {
    private val destinations = listOf("home", "live", "search", "settings")
        .map { id -> UiDestination(id, id, Icons.Default.Home) }
    private val search = TopBarSearch(destinationId = "search", contentDescription = "Search")

    @Test
    fun `search leaves the tab row and the other tabs keep their order`() {
        assertThat(topBarTabs(destinations, search).map { it.id })
            .containsExactly("home", "live", "settings").inOrder()
    }

    @Test
    fun `without a search pill every destination stays a tab`() {
        assertThat(topBarTabs(destinations, search = null)).isEqualTo(destinations)
    }

    @Test
    fun `a search pill without a configured search destination leaves the tabs unchanged`() {
        val withoutSearch = destinations.filterNot { it.id == "search" }
        assertThat(topBarTabs(withoutSearch, search)).isEqualTo(withoutSearch)
    }

    @Test
    fun `focus enters on the search pill while Search is showing`() {
        val tabs = topBarTabs(destinations, search)
        assertThat(topBarEntryTargetId("search?query=news", tabs, search)).isEqualTo("search")
    }

    @Test
    fun `focus enters on the active tab elsewhere`() {
        val tabs = topBarTabs(destinations, search)
        assertThat(topBarEntryTargetId("live", tabs, search)).isEqualTo("live")
        assertThat(topBarEntryTargetId("settings/general", tabs, search)).isEqualTo("settings")
    }

    @Test
    fun `focus has no target when no tab matches and Search is not showing`() {
        val tabs = topBarTabs(destinations, search)
        assertThat(topBarEntryTargetId("player", tabs, search)).isNull()
    }
}
