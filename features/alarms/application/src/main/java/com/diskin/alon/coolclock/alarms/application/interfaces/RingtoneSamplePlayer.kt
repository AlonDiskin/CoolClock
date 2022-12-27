package com.diskin.alon.coolclock.alarms.application.interfaces

import com.diskin.alon.coolclock.common.application.AppResult
import io.reactivex.Single

interface RingtoneSamplePlayer {

    fun playSample(path: String, volume: Int, duration: Long): Single<AppResult<Unit>>

    fun stop(): Single<AppResult<Unit>>
}
