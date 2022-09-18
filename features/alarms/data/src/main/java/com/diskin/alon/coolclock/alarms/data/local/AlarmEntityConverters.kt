package com.diskin.alon.coolclock.alarms.data.local

import androidx.room.TypeConverter
import com.diskin.alon.coolclock.alarms.domain.WeekDay

private const val NO_REPEAT_DAYS = "empty"

class AlarmEntityConverters {

    @TypeConverter
    fun weekDaysToString(repeatDays: Set<WeekDay>): String {
        return when(repeatDays.isEmpty()) {
            true -> NO_REPEAT_DAYS
            else -> repeatDays.joinToString(",") { it.name }
        }
    }

    @TypeConverter
    fun stringToWeekDays(str: String): Set<WeekDay> {
        val daysValues = str.split(',')
        return when(daysValues.first() == NO_REPEAT_DAYS) {
            true -> emptySet()
            false -> daysValues.map { WeekDay.valueOf(it) }.toSet()
        }
    }
}