package com.diskin.alon.coolclock.timer.data

import android.content.SharedPreferences
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

const val KEY_SECONDS = "seconds"
const val KEY_MINUTES = "minutes"
const val KEY_HOURS = "hours"

@Singleton
class TimerDurationStore @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun getLast(): Single<TimerDuration> {
        return Single.create<TimerDuration> {
            val duration = TimerDuration(
                sharedPreferences.getInt(KEY_SECONDS,0),
                sharedPreferences.getInt(KEY_MINUTES,0),
                sharedPreferences.getInt(KEY_HOURS,0)
            )
            it.onSuccess(duration)
        }.subscribeOn(Schedulers.io())
    }

    fun save(timerDuration: TimerDuration) {
        sharedPreferences.edit()
            .putInt(KEY_SECONDS,timerDuration.seconds)
            .putInt(KEY_MINUTES,timerDuration.minutes)
            .putInt(KEY_HOURS,timerDuration.hours)
            .apply()
    }
}