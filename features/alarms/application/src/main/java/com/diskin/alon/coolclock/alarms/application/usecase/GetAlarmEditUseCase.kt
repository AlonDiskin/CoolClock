package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmVolumeRangeProvider
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.RingtonesDataStore
import com.diskin.alon.coolclock.alarms.application.model.AlarmEdit
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.application.model.GetEditRequest
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.common.application.*
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class GetAlarmEditUseCase @Inject constructor(
    private val ringtonesDataStore: RingtonesDataStore,
    private val alarmVolumeProvider: AlarmVolumeRangeProvider,
    private val alarmsRepository: AlarmsRepository
) : UseCase<GetEditRequest,Single<AppResult<AlarmEdit>>> {

    override fun execute(param: GetEditRequest): Single<AppResult<AlarmEdit>> {
        return when(param) {
            is GetEditRequest.New -> getDefaultEdit()
            is GetEditRequest.Existing -> getAlarmEdit(param.id)
        }
    }

    private fun getDefaultEdit(): Single<AppResult<AlarmEdit>> {
        return combineLatestAppResults(
            ringtonesDataStore.getDefault().toObservable(),
            ringtonesDataStore.getAll().toObservable(),
            alarmVolumeProvider.get().toObservable(),
        ) { defaultRingtone, allRingtones, volume ->
            val edit: AlarmEdit = AlarmEdit.DefaultEdit(defaultRingtone,allRingtones,volume)
            edit
        }.firstOrError()
    }

    private fun getAlarmEdit(id: Int): Single<AppResult<AlarmEdit>> {
        return alarmsRepository.get(id)
            .flatMapSingleAppResult { alarm ->
                combineLatestAppResults(
                    when (val sound = alarm.sound) {
                        is Sound.AlarmSound -> ringtonesDataStore.getByPath(sound.path)
                            .toObservable()
                            .mapAppResult { it }
                        is Sound.Silent -> Observable.just(AppResult.Success(AlarmSound.Silent))
                    },
                    ringtonesDataStore.getAll().toObservable(),
                    alarmVolumeProvider.get().toObservable(),
                ) { ringtone,allRingtones, volume ->
                    val edit: AlarmEdit = AlarmEdit.ExistingEdit(
                        alarm.name,
                        alarm.hour,
                        alarm.minute,
                        alarm.repeatDays.map { day ->
                            when (day) {
                                WeekDay.SUN -> RepeatDay.SUN
                                WeekDay.MON -> RepeatDay.MON
                                WeekDay.TUE -> RepeatDay.TUE
                                WeekDay.WED -> RepeatDay.WED
                                WeekDay.THU -> RepeatDay.THU
                                WeekDay.FRI -> RepeatDay.FRI
                                WeekDay.SAT -> RepeatDay.SAT
                            }
                        }.toSet(),
                        ringtone,
                        alarm.duration,
                        alarm.volume,
                        alarm.snooze,
                        alarm.isVibrate,
                        allRingtones,
                        volume)
                    edit
                }.firstOrError()
            }
    }
}