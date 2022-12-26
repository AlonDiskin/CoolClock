package com.diskin.alon.coolclock.alarms.application.usecase

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.model.BrowserAlarm
import com.diskin.alon.coolclock.common.application.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetAlarmsBrowserUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmsMapper: BrowserAlarmsMapper
) : UseCase<Unit, Observable<PagingData<BrowserAlarm>>> {

    override fun execute(param: Unit): Observable<PagingData<BrowserAlarm>> {
        return alarmsRepo.getAll().map(alarmsMapper::map)
    }
}