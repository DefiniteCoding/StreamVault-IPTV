package com.streamvault.app.ui

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

/**
 * Rows of focusable items use FocusSafeLazyRow so the first and last item keep their focus ring and the row fades
 * at a scrollable edge. A raw LazyRow brings back the clipped ring.
 *
 * [pendingMigration] lists files not migrated yet, with their remaining raw call count. Migrating a file means
 * removing its entry; the test fails on a stale entry so the list only shrinks.
 */
class FocusSafeLazyRowGuardTest {
    private val rawLazyRowCall = Regex("(?<![A-Za-z])LazyRow\\(")

    // The wrapper itself is the one place that calls LazyRow.
    private val permanentAllowlist = setOf("FocusHelpers.kt")

    private val pendingMigration = mapOf(
        "PlayerAuxiliaryOverlays.kt" to 1,
        "PlayerChannelInfoOverlay.kt" to 2,
        "PlayerControlsChrome.kt" to 1,
        "StremioCatalogBrowseContent.kt" to 2,
        "StremioDirectoryContent.kt" to 1,
        "StremioGuideContent.kt" to 2,
        "StremioItemDetailContent.kt" to 3,
        "StremioLibraryContent.kt" to 1,
        "StremioViewingContent.kt" to 2
    )

    @Test
    fun rawLazyRowCallsMatchThePendingMigrationList() {
        val sourceRoots = listOf("src", "../core/ui/src", "../feature").map(::File)
        val counts = sourceRoots
            .flatMap { root -> root.walkTopDown().toList() }
            .filter { it.extension == "kt" && "/src/main/" in it.invariantSeparatorsPath }
            .filter { it.name !in permanentAllowlist }
            .associate { it.name to rawLazyRowCall.findAll(it.readText()).count() }
            .filterValues { it > 0 }

        assertThat(counts).isEqualTo(pendingMigration)
    }
}
