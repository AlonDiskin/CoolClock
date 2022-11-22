package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.application.model.ScheduleAlarmRequest
import com.diskin.alon.coolclock.alarms.domain.*
import com.diskin.alon.coolclock.common.application.*
import io.reactivex.Single
import javax.inject.Inject

const val EMPTY_ID = 0

class ScheduleAlarmUseCase @Inject constructor(
    private val repo: AlarmsRepository,
    private val scheduler: AlarmsScheduler,
) : UseCase<ScheduleAlarmRequest,Single<AppResult<Long>>> {

    override fun execute(param: ScheduleAlarmRequest): Single<AppResult<Long>> {
        return when(param) {
            is ScheduleAlarmRequest.NewAlarm -> scheduleNewAlarm(param)
        }
    }

    private fun scheduleNewAlarm(request: ScheduleAlarmRequest.NewAlarm): Single<AppResult<Long>> {
        return repo.getWithNextAlarm(createNewAlarm(request).nextAlarm)
            .flatMapSingleElementAppResult { existingAlarm ->
                deleteAlarmAndCancelScheduled(existingAlarm)
            }
            .toSingle(AppResult.Success(Unit))
            .flatMap { addAlarmToRepoAndSchedule(request) }
    }

    private fun deleteAlarmAndCancelScheduled(existingAlarm: Alarm): Single<AppResult<Unit>> {
        val deleteAlarm = repo.delete(existingAlarm.id)

        return when(existingAlarm.isScheduled) {
            true -> deleteAlarm.flatMap { scheduler.cancel(existingAlarm) }
            false -> deleteAlarm
        }
    }

    private fun addAlarmToRepoAndSchedule(request: ScheduleAlarmRequest.NewAlarm): Single<AppResult<Long>> {
        return repo.add(createNewAlarm(request))
            .flatMapSingleAppResult {
                scheduler.schedule(createAlarm(request,it))
            }
    }

    private fun createNewAlarm(request: ScheduleAlarmRequest.NewAlarm): Alarm {
        return Alarm(
            EMPTY_ID,
            request.name, request.hour,
            request.minute,
            request.repeatDays.map {
                when(it) {
                    RepeatDay.SUN -> WeekDay.SUN
                    RepeatDay.MON -> WeekDay.MON
                    RepeatDay.TUE -> WeekDay.TUE
                    RepeatDay.WED -> WeekDay.WED
                    RepeatDay.THU -> WeekDay.THU
                    RepeatDay.FRI -> WeekDay.FRI
                    RepeatDay.SAT -> WeekDay.SAT
                }
            }.toSet(),
            true,
            request.vibration,
            when(val ringtone = request.ringtone) {
                is AlarmSound.Ringtone -> Sound.AlarmSound(ringtone.path)
                else -> Sound.Silent
            },
            request.duration,
            request.volume,
            request.snooze,
            false
        )
    }

    private fun createAlarm(request: ScheduleAlarmRequest.NewAlarm,id: Int): Alarm {
        return Alarm(
            id,
            request.name, request.hour,
            request.minute,
            request.repeatDays.map {
                when(it) {
                    RepeatDay.SUN -> WeekDay.SUN
                    RepeatDay.MON -> WeekDay.MON
                    RepeatDay.TUE -> WeekDay.TUE
                    RepeatDay.WED -> WeekDay.WED
                    RepeatDay.THU -> WeekDay.THU
                    RepeatDay.FRI -> WeekDay.FRI
                    RepeatDay.SAT -> WeekDay.SAT
                }
            }.toSet(),
            true,
            request.vibration,
            when(val ringtone = request.ringtone) {
                is AlarmSound.Ringtone -> Sound.AlarmSound(ringtone.path)
                else -> Sound.Silent
            },
            request.duration,
            request.volume,
            request.snooze,
            false
        )
    }
}