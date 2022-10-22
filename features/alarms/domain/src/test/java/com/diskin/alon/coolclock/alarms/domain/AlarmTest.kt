package com.diskin.alon.coolclock.alarms.domain

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class AlarmTest {

    @Parameters(method = "unrepeatedNextAlarmParams")
    @Test
    fun getNextAlarm_WhenAlarmUnrepeated(current: DateTime,alarm: Alarm,next: Long) {
        // Given
        DateTimeUtils.setCurrentMillisFixed(current.millis)

        // When
        val actual = alarm.nextAlarm()

        println(actual)
        // Then
        assertThat(actual).isEqualTo(next)
    }

    @Parameters(method = "repeatedNextAlarmParams")
    @Test
    fun getNextAlarm_WhenAlarmRepeated(current: DateTime,alarm: Alarm,next: Long) {
        // Given
        DateTimeUtils.setCurrentMillisFixed(current.millis)

        // When
        val actual = alarm.nextAlarm()

        // Then
        assertThat(actual).isEqualTo(next)
    }

    private fun unrepeatedNextAlarmParams() =
        arrayOf(
            arrayOf(
                DateTime(2022,8,10,12,10),
                Alarm(1,
                    "name",
                    2,
                    45,
                    emptySet(),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,11,2,45).millis
            ),
            arrayOf(
                DateTime(2022,8,10,10,10),
                Alarm(1,
                    "name",
                    12,
                    45,
                    emptySet(),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,10,12,45).millis
            ),
            arrayOf(
                DateTime(2022,8,10,12,10),
                Alarm(1,
                    "name",
                    12,
                        10,
                    emptySet(),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,11,12,10).millis
            ),
            arrayOf(
                DateTime(2022,8,10,12,10),
                Alarm(1,
                    "name",
                    12,
                    11,
                    emptySet(),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,10,12,11).millis
            ),
            arrayOf(
                DateTime(2022,8,10,12,10),
                Alarm(1,
                    "name",
                    12,
                    9,
                    emptySet(),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,11,12,9).millis
            )
        )

    private fun repeatedNextAlarmParams() =
        arrayOf(
            arrayOf(
                DateTime(2022,8,9,12,10),
                Alarm(1,
                    "name",
                    2,
                    45,
                    setOf(WeekDay.SAT,WeekDay.MON),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,13,2,45).millis
            ),
            arrayOf(
                DateTime(2022,8,10,10,10),
                Alarm(1,
                    "name",
                    12,
                    45,
                    setOf(WeekDay.SAT,WeekDay.FRI),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,12,12,45).millis
            ),
            arrayOf(
                DateTime(2022,8,10,12,10),
                Alarm(1,
                    "name",
                    12,
                    10,
                    setOf(WeekDay.SUN,WeekDay.MON),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,14,12,10).millis
            ),
            arrayOf(
                DateTime(2022,8,10,12,10),
                Alarm(1,
                    "name",
                    12,
                    9,
                    setOf(WeekDay.TUE,WeekDay.THU),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,11,12,9).millis
            ),
            arrayOf(
                DateTime(2022,8,9,12,10),
                Alarm(1,
                    "name",
                    12,
                    11,
                    setOf(WeekDay.TUE),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,9,12,11).millis
            ),
            arrayOf(
                DateTime(2022,8,9,12,10),
                Alarm(1,
                    "name",
                    12,
                    9,
                    setOf(WeekDay.TUE,WeekDay.FRI),
                    true,
                    false,
                    Sound.AlarmSound("ringtone"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,12,12,9).millis
            )
        )
}