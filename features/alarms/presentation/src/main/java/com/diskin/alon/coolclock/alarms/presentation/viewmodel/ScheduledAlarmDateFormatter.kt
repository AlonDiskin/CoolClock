package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import org.joda.time.DateTime
import org.joda.time.Period
import javax.inject.Inject

class ScheduledAlarmDateFormatter @Inject constructor() {

    fun format(date: Long): String {
        val current = DateTime()
        val scheduled = DateTime(date)
        val period = Period(current,scheduled)
        val years = when(val i = period.years) {
            0 -> ""
            1 -> "1 year,"
            else -> i.toString().plus(" years,")
        }
        val months = when(val i = period.months) {
            0 -> ""
            1 -> "1 month,"
            else -> i.toString().plus(" months,")
        }
        val weeks = when(val i = period.weeks) {
            0 -> ""
            1 -> "1 week,"
            else -> i.toString().plus(" weeks,")
        }
        val days = when(val i = period.days) {
            0 -> ""
            1 -> "1 day,"
            else -> i.toString().plus(" days,")
        }
        val hours = when(val i = period.hours) {
            0 -> ""
            1 -> "1 hour,"
            else -> i.toString().plus(" hours,")
        }
        val minutes = when(val i = period.minutes) {
            0 -> ""
            1 -> "1 minute"
            else -> i.toString().plus(" minutes")
        }
        val diff = "$years$months$weeks$days$hours$minutes"
        val res = if (diff.last() == ',') diff.dropLast(1) else diff

        return "Alarm set for $res from now"
    }
}