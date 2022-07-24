package com.diskin.alon.coolclock.alarms.application.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.alarms.application.model.CreatedAlarm
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import javax.inject.Inject

class AlarmsMapper @Inject constructor() {

    fun map(entityAlarms: PagingData<Alarm>): PagingData<CreatedAlarm> {
        return entityAlarms.map {
            CreatedAlarm(
                it.id,
                it.name,
                it.time.hour,
                it.time.minute,
                it.repeatDays.map { day ->
                    when (day) {
                        WeekDay.SUN -> RepeatDay.SUN
                        WeekDay.MON -> RepeatDay.MON
                        WeekDay.TUE -> RepeatDay.TUE
                        WeekDay.WED -> RepeatDay.WED
                        WeekDay.THU -> RepeatDay.THU
                        WeekDay.FRI -> RepeatDay.FRI
                        WeekDay.SUT -> RepeatDay.SAT
                    }
                }.toSet(),
                it.isActive,
                it.nextAlarm()
            )
        }
    }
}