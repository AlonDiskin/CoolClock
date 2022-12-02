package com.diskin.alon.coolclock.alarms.domain

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

data class Alarm(val id: Int,
                 var name: String,
                 var hour: Int,
                 var minute: Int,
                 var repeatDays: Set<WeekDay>,
                 var isScheduled: Boolean,
                 var isVibrate: Boolean,
                 var sound: Sound,
                 var duration: Int,
                 var volume: Int,
                 var snooze: Int,
                 var isSnoozed: Boolean) {

    init {
        require(hour in 0..23)
        require(minute in 0..59)
        require(duration > 0)
        require(snooze >= 0)
        require(volume > 0)
    }

    val nextAlarm: Long get() { return nextAlarm() }

    private fun nextAlarm(): Long {
        val currentDateTime = DateTime().withSecondOfMinute(0).withMillisOfSecond(0)
        val alarmDate = DateTime()
            .withHourOfDay(hour)
            .withMinuteOfHour(minute)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)

        return when(repeatDays.isEmpty()) {
            true -> {
                if (alarmDate.millis > currentDateTime.millis) {
                    // Today
                    alarmDate.millis
                } else {
                    // Tomorrow
                    alarmDate.plusDays(1).millis
                }
            }

            false -> {
                val repeatWeekDays = repeatDays.map {
                    when(it) {
                        WeekDay.SUN ->  DateTimeConstants.SUNDAY
                        WeekDay.MON ->  DateTimeConstants.MONDAY
                        WeekDay.TUE ->  DateTimeConstants.TUESDAY
                        WeekDay.WED ->  DateTimeConstants.WEDNESDAY
                        WeekDay.THU ->  DateTimeConstants.THURSDAY
                        WeekDay.FRI ->  DateTimeConstants.FRIDAY
                        WeekDay.SAT ->  DateTimeConstants.SATURDAY
                    }
                }
                val nextAlarms = mutableListOf<Long>()

                for (day: Int in repeatWeekDays) {
                    when {
                        day == currentDateTime.dayOfWeek -> {
                            if (alarmDate.millis > currentDateTime.millis) {
                                // Today
                                nextAlarms.add(alarmDate.millis)
                            } else {
                                // Next week
                                nextAlarms.add(alarmDate.plusDays(7).millis)
                            }
                        }

                        day > currentDateTime.dayOfWeek -> {
                            nextAlarms.add(
                                alarmDate.plusDays(day - currentDateTime.dayOfWeek).millis
                            )
                        }

                        day < currentDateTime.dayOfWeek -> {
                            nextAlarms.add(
                                alarmDate.plusDays(7 - (currentDateTime.dayOfWeek - day)).millis
                            )
                        }
                    }
                }

                nextAlarms.minOf { it }
            }
        }
    }
}