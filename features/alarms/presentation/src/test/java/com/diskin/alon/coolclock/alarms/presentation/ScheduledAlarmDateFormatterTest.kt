package com.diskin.alon.coolclock.alarms.presentation

import com.diskin.alon.coolclock.alarms.presentation.viewmodel.ScheduledAlarmDateFormatter
import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class ScheduledAlarmDateFormatterTest {

    // Test subject
    private val formatter = ScheduledAlarmDateFormatter()

    @Test
    @Parameters(method = "formatParams")
    fun formatDate(current: DateTime,scheduled: DateTime,format: String) {
        // Given
        DateTimeUtils.setCurrentMillisFixed(current.millis)

        // When
        val actual = formatter.format(scheduled.millis)

        // Then
        assertThat(actual).isEqualTo(format)
    }

    fun formatParams() = arrayOf(
        arrayOf(
            DateTime(2022,9,11,12,45),
            DateTime(2022,9,11,13,45),
            "Alarm set for 1 hour from now"
        ),
        arrayOf(
            DateTime(2022,9,11,12,45),
            DateTime(2022,9,14,13,45),
            "Alarm set for 3 days,1 hour from now"
        ),
        arrayOf(
            DateTime(2022,9,11,12,45),
            DateTime(2022,9,18,12,44),
            "Alarm set for 6 days,23 hours,59 minutes from now"
        ),
        arrayOf(
            DateTime(2022,9,11,12,45),
            DateTime(2022,9,18,12,46),
            "Alarm set for 1 week,1 minute from now"
        )
    )
}