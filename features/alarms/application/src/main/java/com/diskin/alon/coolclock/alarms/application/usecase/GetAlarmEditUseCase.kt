package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmVolumeRangeProvider
import com.diskin.alon.coolclock.alarms.application.interfaces.RingtonesDataStore
import com.diskin.alon.coolclock.alarms.application.model.AlarmEdit
import com.diskin.alon.coolclock.alarms.application.model.GetEditRequest
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.UseCase
import com.diskin.alon.coolclock.common.application.combineLatestAppResults
import io.reactivex.Single
import javax.inject.Inject

class GetAlarmEditUseCase @Inject constructor(
    private val ringtonesDataStore: RingtonesDataStore,
    private val alarmVolumeProvider: AlarmVolumeRangeProvider
) : UseCase<GetEditRequest,Single<AppResult<AlarmEdit>>> {

    override fun execute(param: GetEditRequest): Single<AppResult<AlarmEdit>> {
        return when(param) {
            is GetEditRequest.New -> getDefaultEdit()
            else -> TODO("Not yet implemented")
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
}