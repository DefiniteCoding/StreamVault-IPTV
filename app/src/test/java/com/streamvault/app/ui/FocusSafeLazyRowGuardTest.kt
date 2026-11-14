package com.streamvault.app.ui

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

/**
 * Rows of focusable items use FocusSafeLazyRow so the first and last item keep their focus ring. A raw LazyRow
 * brings back the clipped ring. Rows drawn over video pass `fadeWidth = 0.dp` to skip the edge fade, which is an
 * offscreen layer.
 */
class FocusSafeLazyRowGuardTest {
    private val rawLazyRowCall = Regex("(?<![A-Za-z])LazyRow\\(")

    // The wrapper itself is the one place that calls LazyRow.
    private val permanentAllowlist = setOf("FocusHelpers.kt")

    @Test
    fun noRawLazyRowCallsOutsideTheWrapper() {
        val sourceRoots = listOf("src", "../core/ui/src", "../feature").map(::File)
        val counts = sourceRoots
            .flatMap { root -> root.walkTopDown().toList() }
            .filter { it.extension == "kt" && "/src/main/" in it.invariantSeparatorsPath }
            .filter { it.name !in permanentAllowlist }
            .associate { it.name to rawLazyRowCall.findAll(it.readText()).count() }
            .filterValues { it > 0 }

        assertThat(counts).isEmpty()
    }
}
