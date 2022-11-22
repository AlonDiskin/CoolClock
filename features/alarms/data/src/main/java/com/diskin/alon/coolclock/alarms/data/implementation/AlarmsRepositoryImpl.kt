package com.diskin.alon.coolclock.alarms.data.implementation

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.observable
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.data.local.*
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toMaybeAppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

private const val PAGE_SIZE = 20

class AlarmsRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
    private val pagingMapper: AlarmsPagingMapper,
    private val alarmMapper: AlarmMapper,
    private val entityMapper: AlarmEntityMapper,
    private val errorHandler: AlarmsDaoErrorHandler
) : AlarmsRepository {

    override fun getAll(): Observable<PagingData<Alarm>> {
        return Pager(PagingConfig(PAGE_SIZE)) { dao.getAllPaging() }
            .observable
            .subscribeOn(Schedulers.io())
            .map(pagingMapper::map)
    }

    override fun get(id: Int): Single<AppResult<Alarm>> {
        return dao.get(id)
            .subscribeOn(Schedulers.io())
            .map(alarmMapper::map)
            .toSingleAppResult(errorHandler::handle)
    }

    override fun setActive(id: Int, isActive: Boolean): Single<AppResult<Unit>> {
        return dao.updateScheduled(id, isActive)
            .subscribeOn(Schedulers.io())
            .toSingleDefault(Unit)
            .toSingleAppResult(errorHandler::handle)
    }

    override fun delete(id: Int): Single<AppResult<Unit>> {
        return dao.delete(id)
            .subscribeOn(Schedulers.io())
            .toSingleDefault(Unit)
            .toSingleAppResult(errorHandler::handle)
    }

    override fun add(alarm: Alarm): Single<AppResult<Int>> {
        return dao.insert(entityMapper.mapNew(alarm))
            .subscribeOn(Schedulers.io())
            .map { it.toInt() }
            .toSingleAppResult(errorHandler::handle)
    }

    override fun getWithNextAlarm(next: Long): Maybe<AppResult<Alarm>> {
        return dao.getAll()
            .subscribeOn(Schedulers.io())
            .map {
                it.map(alarmMapper::map)
            }
            .flatMapMaybe { alarms ->
                Maybe.create<Alarm> { emitter ->
                    alarms.find { it.nextAlarm == next }?.let {
                        emitter.onSuccess(it)
                    } ?: run {
                        emitter.onComplete()
                    }
                }
            }
            .toMaybeAppResult()
    }
}