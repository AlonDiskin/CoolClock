package com.diskin.alon.coolclock.alarms.data.local

import com.diskin.alon.coolclock.alarms.domain.*
import javax.inject.Inject

class AlarmMapper @Inject constructor() {

    fun map(entity: AlarmEntity): Alarm {
        return Alarm(
            entity.id!!,
            entity.name,
            Time(entity.hour,entity.minute),
            entity.repeatDays,
            entity.isActive,
            entity.isVibrate,
            if (entity.isSound) Sound.Ringtone(entity.ringtone) else Sound.None,
            Duration(entity.duration),
            Volume(entity.volume),
            if (entity.isSnooze) Snooze.Active(entity.snoozeRepeat,entity.snoozeInterval) else Snooze.None
        )
    }
}