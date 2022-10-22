package com.diskin.alon.coolclock.alarms.data.local

import com.diskin.alon.coolclock.alarms.domain.*
import javax.inject.Inject

class AlarmMapper @Inject constructor() {

    fun map(entity: AlarmEntity): Alarm {
        return Alarm(
            entity.id!!,
            entity.name,
            entity.hour,
            entity.minute,
            entity.repeatDays,
            entity.isScheduled,
            entity.isVibrate,
            entity.sound,
            entity.duration,
            entity.volume,
            entity.snooze,
            entity.isSnoozed
        )
    }
}