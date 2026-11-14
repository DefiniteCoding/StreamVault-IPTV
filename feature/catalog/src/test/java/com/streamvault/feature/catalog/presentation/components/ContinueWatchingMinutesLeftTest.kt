package com.streamvault.feature.catalog.presentation.components

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import org.junit.Test

class ContinueWatchingMinutesLeftTest {

    @Test
    fun partial_minutes_round_up() {
        assertThat(minutesLeft(resumeMs = 0, totalMs = 90_001)).isEqualTo(2)
    }

    @Test
    fun nearly_finished_title_reads_one_minute_not_zero() {
        assertThat(minutesLeft(resumeMs = 5_999_999, totalMs = 6_000_000)).isEqualTo(1)
    }

    @Test
    fun whole_minutes_are_not_rounded_up_further() {
        assertThat(minutesLeft(resumeMs = 600_000, totalMs = 7_800_000)).isEqualTo(120)
    }

    @Test
    fun live_streams_have_no_time_left() {
        assertThat(minutesLeft(resumeMs = 0, totalMs = 3_600_000, type = ContentType.LIVE)).isNull()
    }

    @Test
    fun unknown_duration_has_no_time_left() {
        assertThat(minutesLeft(resumeMs = 120_000, totalMs = 0)).isNull()
    }

    @Test
    fun finished_or_overrun_position_has_no_time_left() {
        assertThat(minutesLeft(resumeMs = 3_600_000, totalMs = 3_600_000)).isNull()
        assertThat(minutesLeft(resumeMs = 4_000_000, totalMs = 3_600_000)).isNull()
    }

    private fun minutesLeft(resumeMs: Long, totalMs: Long, type: ContentType = ContentType.MOVIE) =
        continueWatchingMinutesLeft(
            PlaybackHistory(
                contentId = 1L,
                contentType = type,
                providerId = 1L,
                title = "Title",
                streamUrl = "https://example.com/1",
                resumePositionMs = resumeMs,
                totalDurationMs = totalMs
            )
        )
}
