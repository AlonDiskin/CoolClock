package com.diskin.alon.coolclock.alarms.application.interfaces

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.domain.Alarm
import io.reactivex.Observable

interface AlarmsRepository {

    fun getAll(): Observable<PagingData<Alarm>>
}