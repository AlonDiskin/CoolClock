package com.diskin.alon.coolclock.alarms.application.interfaces

import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Single

const val NO_CURRENT_ALARM = -1

interface AlarmExecutor {

    fun startAlarm(alarm: Alarm): Single<AppResult<Unit>>

    fun stopAlarm(): Single<AppResult<Unit>>

    fun currentAlarm(): Single<AppResult<Int>>
}