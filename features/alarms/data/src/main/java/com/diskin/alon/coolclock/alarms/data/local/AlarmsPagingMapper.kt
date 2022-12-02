package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.alarms.domain.Alarm
import javax.inject.Inject

class AlarmsPagingMapper @Inject constructor(
    private val alarmMapper: AlarmMapper
) {

    fun map(alarms: PagingData<AlarmEntity>): PagingData<Alarm> {
        return alarms.map(alarmMapper::map)
    }
}