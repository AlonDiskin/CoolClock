package com.diskin.alon.coolclock.alarms.data.local

import com.diskin.alon.coolclock.alarms.domain.Alarm
import javax.inject.Inject

class AlarmEntityMapper @Inject constructor() {

    fun mapNew(alarm: Alarm): AlarmEntity {
        return AlarmEntity(
            alarm.name,
            alarm.hour,
            alarm.minute,
            alarm.repeatDays,
            true,
            alarm.sound,
            alarm.isVibrate,
            alarm.duration,
            alarm.volume,
            alarm.snooze,
            alarm.isSnoozed
        )
    }
}