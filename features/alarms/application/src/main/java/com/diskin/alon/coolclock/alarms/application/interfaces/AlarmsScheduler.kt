package com.diskin.alon.coolclock.alarms.application.interfaces

import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Single

interface AlarmsScheduler {

    fun schedule(alarm: Alarm): Single<AppResult<Long>>

    fun cancel(alarm: Alarm): Single<AppResult<Unit>>

    fun scheduleSnooze(alarm: Alarm): Single<AppResult<Unit>>
}