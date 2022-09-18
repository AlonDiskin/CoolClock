package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.UseCase
import com.diskin.alon.coolclock.common.application.flatMapSingleAppResult
import io.reactivex.Single
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmsScheduler: AlarmsScheduler
) : UseCase<Int,Single<AppResult<Unit>>> {

    override fun execute(param: Int): Single<AppResult<Unit>> {
        return alarmsRepo.get(param)
            .flatMapSingleAppResult { alarmsScheduler.cancel(it) }
            .flatMapSingleAppResult { alarmsRepo.delete(param) }
    }
}