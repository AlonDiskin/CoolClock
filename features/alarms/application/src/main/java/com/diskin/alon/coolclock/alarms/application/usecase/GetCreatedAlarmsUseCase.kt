package com.diskin.alon.coolclock.alarms.application.usecase

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.model.CreatedAlarm
import com.diskin.alon.coolclock.common.application.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetCreatedAlarmsUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmsMapper: AlarmsMapper
) : UseCase<Unit, Observable<PagingData<CreatedAlarm>>> {

    override fun execute(param: Unit): Observable<PagingData<CreatedAlarm>> {
        return alarmsRepo.getAll().map(alarmsMapper::map)
    }
}