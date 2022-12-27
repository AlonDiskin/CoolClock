package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.UseCase
import com.diskin.alon.coolclock.common.application.flatMapSingleAppResult
import io.reactivex.Single
import javax.inject.Inject

class StopAlarmUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmExecutor: AlarmExecutor
) : UseCase<Int, Single<AppResult<Unit>>> {

    override fun execute(param: Int): Single<AppResult<Unit>> {
        return alarmExecutor.stopAlarm()
            .flatMapSingleAppResult { alarmsRepo.get(param) }
            .flatMapSingleAppResult {
                when(it.isRepeated) {
                    true -> Single.just(AppResult.Success(Unit))
                    false -> alarmsRepo.setActive(param,false)
                }
            }
            .flatMapSingleAppResult { alarmsRepo.setSnoozed(param,false) }
    }
}