package com.diskin.alon.coolclock.alarms.application.interfaces

import com.diskin.alon.coolclock.alarms.application.model.AlarmVolumeRange
import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Single

interface AlarmVolumeRangeProvider {

    fun get(): Single<AppResult<AlarmVolumeRange>>
}