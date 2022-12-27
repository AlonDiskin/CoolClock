package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import android.app.Application
import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.alarms.application.model.BrowserAlarm
import com.diskin.alon.coolclock.alarms.application.model.NextAlarm
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class UiAlarmsMapper @Inject constructor(
    app: Application
) {

    private val res = app.resources

    fun map(alarms: PagingData<BrowserAlarm>): PagingData<UiAlarm> {
        return alarms.map {
            UiAlarm(
                it.id,
                mapAlarmTime(it.hour,it.minute),
                it.name,
                it.isActive,
                mapNextAlarm(it),
                it.repeatDays,
                it.isSnoozed
            )
        }
    }

    private fun mapNextAlarm(alarm: BrowserAlarm): String {
        return when {
            alarm.isSnoozed -> "Snoozed"
            alarm.nextAlarm is NextAlarm.None -> res.getString(R.string.label_alarm_not_active)
            isAlarmToday((alarm.nextAlarm as NextAlarm.Next).millis) -> res.getString(R.string.label_alarm_today)
            isAlarmTomorrow((alarm.nextAlarm as NextAlarm.Next).millis) -> res.getString(R.string.label_alarm_tomorrow)
            else -> {
                DateTime((alarm.nextAlarm as NextAlarm.Next).millis)
                    .toString(
                        DateTimeFormat.forPattern(res.getString(R.string.alarm_date_format))
                    )
            }
        }
    }

    private fun isAlarmTomorrow(nextAlarm: Long): Boolean {
        return (DateTime(nextAlarm).dayOfWeek - DateTime().dayOfWeek) == 1
    }

    private fun isAlarmToday(nextAlarm: Long): Boolean {
        return DateTime(nextAlarm).dayOfWeek == DateTime().dayOfWeek
    }

    private fun mapAlarmTime(hour: Int, minute: Int): String {
        val formatHour = if (hour < 10) "0$hour" else hour.toString()
        val formatMinute = if (minute < 10) "0$minute" else minute.toString()
        return "$formatHour:$formatMinute"
    }
}