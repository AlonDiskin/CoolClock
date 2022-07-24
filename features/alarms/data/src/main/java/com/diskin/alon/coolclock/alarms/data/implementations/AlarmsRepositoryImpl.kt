package com.diskin.alon.coolclock.alarms.data.implementations

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.data.local.AlarmLocalSource
import com.diskin.alon.coolclock.alarms.domain.Alarm
import io.reactivex.Observable
import javax.inject.Inject

class AlarmsRepositoryImpl @Inject constructor(
    private val localSource: AlarmLocalSource
) : AlarmsRepository {

    override fun getAll(): Observable<PagingData<Alarm>> {
        return localSource.getAll()
    }
}