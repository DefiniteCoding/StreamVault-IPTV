package com.streamvault.core.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FocusSafePaddingTest {

    private val asymmetric = PaddingValues(start = 10.dp, top = 1.dp, end = 30.dp, bottom = 2.dp)

    @Test
    fun `ltr keeps start on the left and end on the right`() {
        val padded = asymmetric.withFocusOverflow(LayoutDirection.Ltr)

        assertThat(padded.calculateLeftPadding(LayoutDirection.Ltr)).isEqualTo(10.dp)
        assertThat(padded.calculateRightPadding(LayoutDirection.Ltr)).isEqualTo(30.dp)
    }

    @Test
    fun `rtl keeps start on the right and end on the left`() {
        val padded = asymmetric.withFocusOverflow(LayoutDirection.Rtl)

        assertThat(padded.calculateLeftPadding(LayoutDirection.Rtl)).isEqualTo(30.dp)
        assertThat(padded.calculateRightPadding(LayoutDirection.Rtl)).isEqualTo(10.dp)
    }

    @Test
    fun `vertical padding grows by the focus overflow in both directions`() {
        LayoutDirection.entries.forEach { direction ->
            val padded = asymmetric.withFocusOverflow(direction)

            assertThat(padded.calculateTopPadding()).isEqualTo(1.dp + FocusOverflowPadding)
            assertThat(padded.calculateBottomPadding()).isEqualTo(2.dp + FocusOverflowPadding)
        }
    }
}
