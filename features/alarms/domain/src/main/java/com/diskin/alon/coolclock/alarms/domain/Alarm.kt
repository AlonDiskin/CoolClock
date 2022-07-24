package com.diskin.alon.coolclock.alarms.domain

import org.joda.time.DateTime

data class Alarm(val id: Int,
                 var name: String,
                 var time: Time,
                 var repeatDays: Set<WeekDay>,
                 var isActive: Boolean,
                 var ringtone: String,
                 var isVibrate: Boolean,
                 var isSound: Boolean) {

    init {
        require(if (isSound) ringtone.isNotEmpty() else ringtone.isEmpty())
    }

    fun nextAlarm(): Long {
        return when(isActive) {
            true -> {
                if (repeatDays.isEmpty()) {
                    calcUnrepeatedNextAlarm()
                }  else {
                    calcRepeatedNextAlarm()
                }
            }

            false -> 0
        }
    }

    private fun calcUnrepeatedNextAlarm(): Long {
        val currentDateTime = DateTime()

        return if ( time.hour >= currentDateTime.hourOfDay &&
             time.minute > currentDateTime.minuteOfHour) {
            // Today
            currentDateTime.plusHours(time.hour - currentDateTime.hourOfDay)
                .plusMinutes(time.minute - currentDateTime.minuteOfHour).millis
        } else {
            // Tomorrow
            currentDateTime.plusDays(1)
                .withTime(time.hour,time.minute,0,0).millis
        }
    }

    private fun calcRepeatedNextAlarm(): Long {
        val currentDateTime = DateTime()
        val currentWeekDay = when(currentDateTime.dayOfWeek) {
            1 -> WeekDay.MON
            2 -> WeekDay.TUE
            3 -> WeekDay.WED
            4 -> WeekDay.THU
            5 -> WeekDay.FRI
            6 -> WeekDay.SUT
            7 -> WeekDay.SUN
            else -> throw IllegalArgumentException("Wrong week day value was read from joda!")
        }.ordinal
        val sortedRepeatDays = repeatDays.sortedBy { it.ordinal }.map { it.ordinal }
        var minDest = 7

        for (day: Int in sortedRepeatDays) {
            var dest = day - currentWeekDay

            if (dest < 0) {
               dest = 7 - (dest*(-1))
            }

            if (dest < minDest) minDest = dest
        }

        return if (minDest == 0) {
            if ( time.hour >= currentDateTime.hourOfDay &&
                time.minute > currentDateTime.minuteOfHour) {
                // Today
                currentDateTime.plusHours(time.hour - currentDateTime.hourOfDay)
                    .plusMinutes(time.minute - currentDateTime.minuteOfHour).millis
            } else {
                // Next week today
                currentDateTime.plusDays(7)
                    .withTime(time.hour,time.minute,0,0).millis
            }
        } else {
            currentDateTime.plusDays(minDest)
                .withTime(time.hour,time.minute,0,0).millis
        }
    }
}