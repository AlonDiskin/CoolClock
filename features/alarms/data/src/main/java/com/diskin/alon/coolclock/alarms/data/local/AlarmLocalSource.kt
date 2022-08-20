package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.observable
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 20

@Singleton
class AlarmLocalSource @Inject constructor(
    private val dao: AlarmDao,
    private val pagingMapper: AlarmsPagingMapper,
    private val alarmMapper: AlarmMapper,
    private val errorHandler: AlarmsDaoErrorHandler
) {

    fun getAll(): Observable<PagingData<Alarm>> {
        return Pager(PagingConfig(PAGE_SIZE)) { dao.getAll() }
            .observable
            .subscribeOn(Schedulers.io())
            .map(pagingMapper::map)
    }

    fun get(id: Int): Single<AppResult<Alarm>> {
        return dao.get(id)
            .subscribeOn(Schedulers.io())
            .map(alarmMapper::map)
            .toSingleAppResult(errorHandler::handle)
    }

    fun setActive(id: Int, isActive: Boolean): Single<AppResult<Unit>> {
        return dao.updateIsActive(id, isActive)
            .subscribeOn(Schedulers.io())
            .toSingleDefault(Unit)
            .toSingleAppResult(errorHandler::handle)
    }
}