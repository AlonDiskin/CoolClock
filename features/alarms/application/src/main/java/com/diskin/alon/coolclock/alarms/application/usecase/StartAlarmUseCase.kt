package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.NO_CURRENT_ALARM
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.UseCase
import com.diskin.alon.coolclock.common.application.flatMapSingleAppResult
import io.reactivex.Single
import javax.inject.Inject

class StartAlarmUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmExecutor: AlarmExecutor,
    private val stopAlarmUseCase: StopAlarmUseCase
) : UseCase<Int,Single<AppResult<Unit>>> {

    override fun execute(param: Int): Single<AppResult<Unit>> {
        return alarmExecutor.currentAlarm()
            .flatMapSingleAppResult { currentAlarmId ->
                if (currentAlarmId != NO_CURRENT_ALARM) {
                    stopAlarmUseCase.execute(currentAlarmId)
                } else {
                    Single.just(AppResult.Success(Unit))
                }
            }
            .flatMapSingleAppResult { alarmsRepo.get(param) }
            .flatMapSingleAppResult(alarmExecutor::startAlarm)
    }
}