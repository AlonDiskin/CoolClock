package com.diskin.alon.coolclock.alarms.application.interfaces

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Observable
import io.reactivex.Single

interface AlarmsRepository {

    fun getAll(): Observable<PagingData<Alarm>>

    fun get(id: Int): Single<AppResult<Alarm>>

    fun setActive(id: Int,isActive: Boolean): Single<AppResult<Unit>>

    fun delete(id: Int): Single<AppResult<Unit>>

    fun add(alarm: Alarm): Single<AppResult<Int>>
}