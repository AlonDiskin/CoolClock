package com.diskin.alon.coolclock.timer.presentation.device

import android.app.Application
import android.media.RingtoneManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerAlarmManager @Inject constructor(
    app: Application
) {

    private val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    private val ringtone = RingtoneManager.getRingtone(app, uri)

    fun startAlarm() {
        ringtone.play()
    }

    fun stopAlarm() {
        ringtone.stop()
    }
}