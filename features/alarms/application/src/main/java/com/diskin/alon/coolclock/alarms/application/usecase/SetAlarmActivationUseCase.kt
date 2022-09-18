package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.model.AlarmActivation
import com.diskin.alon.coolclock.alarms.application.model.NextAlarm
import com.diskin.alon.coolclock.common.application.*
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class SetAlarmActivationUseCase @Inject constructor(
    private val alarmsRepo: AlarmsRepository,
    private val alarmsScheduler: AlarmsScheduler
) : UseCase<AlarmActivation,Single<AppResult<NextAlarm>>> {

    override fun execute(param: AlarmActivation): Single<AppResult<NextAlarm>> {
        return when(param.activation) {
            true -> scheduleAlarmAndUpdateRepo(param.alarmId)
            false -> cancelAlarmAndUpdateRepo(param.alarmId)
        }
    }

    private fun scheduleAlarmAndUpdateRepo(id: Int): Single<AppResult<NextAlarm>> {
        val res: Observable<AppResult<NextAlarm>> = combineLatestAppResults(
            alarmsRepo.get(id).flatMapSingleAppResult { alarm ->
                alarmsScheduler.schedule(alarm).mapAppResult { alarm.nextAlarm() } }.toObservable(),
            alarmsRepo.setActive(id,true).toObservable()
        ) { r1, _ -> NextAlarm.Next(r1) }

        return res.firstOrError()
    }

    private fun cancelAlarmAndUpdateRepo(id: Int): Single<AppResult<NextAlarm>> {
        val alarm = alarmsRepo.get(id)
        val res: Observable<AppResult<NextAlarm>> = combineLatestAppResults(
            alarm.flatMapSingleAppResult { alarmsScheduler.cancel(it) }.toObservable(),
            alarmsRepo.setActive(id,false).toObservable()
        ) { _, _ -> NextAlarm.None}

        return res.firstOrError()
    }
}