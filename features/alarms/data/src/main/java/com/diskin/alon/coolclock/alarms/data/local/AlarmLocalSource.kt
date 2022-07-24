package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.observable
import com.diskin.alon.coolclock.alarms.domain.Alarm
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 20

@Singleton
class AlarmLocalSource @Inject constructor(
    private val dao: AlarmDao,
    private val mapper: AlarmsMapper
) {

    fun getAll(): Observable<PagingData<Alarm>> {
        return Pager(PagingConfig(PAGE_SIZE)) { dao.getAll() }
            .observable
            .subscribeOn(Schedulers.io())
            .map(mapper::map)
    }
}