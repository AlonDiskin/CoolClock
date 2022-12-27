package com.diskin.alon.coolclock.alarms.application.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.alarms.application.model.BrowserAlarm
import com.diskin.alon.coolclock.alarms.application.model.NextAlarm
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import javax.inject.Inject

class BrowserAlarmsMapper @Inject constructor() {

    fun map(entityAlarms: PagingData<Alarm>): PagingData<BrowserAlarm> {
        return entityAlarms.map {
            BrowserAlarm(
                it.id,
                it.name,
                it.hour,
                it.minute,
                it.repeatDays.map { day ->
                    when (day) {
                        WeekDay.SUN -> RepeatDay.SUN
                        WeekDay.MON -> RepeatDay.MON
                        WeekDay.TUE -> RepeatDay.TUE
                        WeekDay.WED -> RepeatDay.WED
                        WeekDay.THU -> RepeatDay.THU
                        WeekDay.FRI -> RepeatDay.FRI
                        WeekDay.SAT -> RepeatDay.SAT
                    }
                }.toSet(),
                it.isScheduled,
                if (it.isScheduled) NextAlarm.Next(it.nextAlarm) else NextAlarm.None,
                it.isSnoozed
            )
        }
    }
}