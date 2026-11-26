package com.streamvault.core.ui.design

import androidx.compose.foundation.gestures.BringIntoViewSpec
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@Suppress("OPT_IN_USAGE")
class BringIntoViewSpecTest {

    private fun BringIntoViewSpec.distance(offset: Float, size: Float, container: Float = 1000f) =
        calculateScrollDistance(offset, size, container)

    // Container 1000 gives a comfort zone of 200..800.
    private val comfort = ComfortZoneBringIntoViewSpec

    @Test
    fun `comfort zone leaves an item that sits inside the zone`() {
        assertThat(comfort.distance(offset = 300f, size = 100f)).isEqualTo(0f)
    }

    @Test
    fun `comfort zone scrolls back when the item is above the zone`() {
        assertThat(comfort.distance(offset = 100f, size = 100f)).isEqualTo(-100f)
    }

    @Test
    fun `comfort zone scrolls forward when the item ends below the zone`() {
        assertThat(comfort.distance(offset = 750f, size = 100f)).isEqualTo(50f)
    }

    @Test
    fun `comfort zone shows the leading edge of an item taller than the zone`() {
        assertThat(comfort.distance(offset = 50f, size = 700f)).isEqualTo(-150f)
    }

    @Test
    fun `comfort zone does not move an item that already fills the viewport`() {
        assertThat(comfort.distance(offset = -100f, size = 1200f)).isEqualTo(0f)
    }

    // Fade of 100 on a container of 1000 gives a clear zone of 100..900.
    private val clearOfFade = ClearOfFadeBringIntoViewSpec(startInsetPx = 100f)

    @Test
    fun `clear of fade leaves an item between the fades`() {
        assertThat(clearOfFade.distance(offset = 500f, size = 100f)).isEqualTo(0f)
    }

    @Test
    fun `clear of fade scrolls an item out of the top fade`() {
        assertThat(clearOfFade.distance(offset = 50f, size = 100f)).isEqualTo(-50f)
    }

    @Test
    fun `clear of fade scrolls an item out of the bottom fade`() {
        assertThat(clearOfFade.distance(offset = 850f, size = 100f)).isEqualTo(50f)
    }

    @Test
    fun `clear of fade aligns an item taller than the zone to the top edge of the zone`() {
        assertThat(clearOfFade.distance(offset = 300f, size = 900f)).isEqualTo(200f)
    }

    // Row resting room of 60 on the left and 20 on the right gives a zone of 60..980.
    private val rowRoom = ClearOfFadeBringIntoViewSpec(startInsetPx = 60f, endInsetPx = 20f)

    @Test
    fun `row room scrolls an item back to its resting position, not to the viewport edge`() {
        assertThat(rowRoom.distance(offset = -40f, size = 100f)).isEqualTo(-100f)
    }

    @Test
    fun `row room leaves an item at its resting position`() {
        assertThat(rowRoom.distance(offset = 60f, size = 100f)).isEqualTo(0f)
    }

    @Test
    fun `row room scrolls forward using the smaller end room`() {
        assertThat(rowRoom.distance(offset = 900f, size = 100f)).isEqualTo(20f)
    }
}
