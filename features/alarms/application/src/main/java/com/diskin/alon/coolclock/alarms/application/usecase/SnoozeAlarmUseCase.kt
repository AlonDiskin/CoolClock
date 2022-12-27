package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.UseCase
import com.diskin.alon.coolclock.common.application.flatMapSingleAppResult
import io.reactivex.Single
import javax.inject.Inject

class SnoozeAlarmUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmScheduler: AlarmsScheduler,
    private val alarmExecutor: AlarmExecutor,
) : UseCase<Int, Single<AppResult<Unit>>> {

    override fun execute(param: Int): Single<AppResult<Unit>> {
        return alarmsRepo.get(param)
            .flatMapSingleAppResult(alarmScheduler::scheduleSnooze)
            .flatMapSingleAppResult { alarmExecutor.stopAlarm() }
            .flatMapSingleAppResult { alarmsRepo.setSnoozed(param,true) }
    }
}