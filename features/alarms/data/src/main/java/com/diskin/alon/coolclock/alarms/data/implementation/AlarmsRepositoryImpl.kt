package com.diskin.alon.coolclock.alarms.data.implementation

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.data.local.AlarmLocalSource
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class AlarmsRepositoryImpl @Inject constructor(
    private val localSource: AlarmLocalSource
) : AlarmsRepository {

    override fun getAll(): Observable<PagingData<Alarm>> {
        return localSource.getAll()
    }

    override fun get(id: Int): Single<AppResult<Alarm>> {
        return localSource.get(id)
    }

    override fun setActive(id: Int, isActive: Boolean): Single<AppResult<Unit>> {
        return localSource.setActive(id, isActive)
    }

    override fun delete(id: Int): Single<AppResult<Unit>> {
        return localSource.delete(id)
    }
}