package com.diskin.alon.coolclock.alarms.application.interfaces

import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Single

interface RingtonesDataStore {

    fun getDefault(): Single<AppResult<AlarmSound.Ringtone>>

    fun getAll(): Single<AppResult<List<AlarmSound.Ringtone>>>

    fun getByPath(path: String): Single<AppResult<AlarmSound.Ringtone>>
}