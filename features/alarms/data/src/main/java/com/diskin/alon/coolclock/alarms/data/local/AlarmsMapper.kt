package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.Time
import javax.inject.Inject

class AlarmsMapper @Inject constructor() {

    fun map(alarms: PagingData<AlarmEntity>): PagingData<Alarm> {
        return alarms.map {
            Alarm(
                it.id!!,
                it.name,
                Time(it.hour,it.minute),
                it.repeatDays,
                it.isActive,
                it.ringtone,
                it.isVibrate,
                it.isSound
            )
        }
    }
}