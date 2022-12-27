package com.diskin.alon.coolclock.alarms.device

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmVibrationManager @Inject constructor(
    private val vibrator: Vibrator
) {

    fun startDeviceVibration() {
        //vibrator.vibrate(longArrayOf(0, 200, 500),0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 500),0))
        } else {
            vibrator.vibrate(longArrayOf(0, 200, 500),0)
        }
    }

    fun stopDeviceVibration() {
        vibrator.cancel()
    }
}