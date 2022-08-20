package com.diskin.alon.coolclock.alarms.device

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton

class VibrationManager @Inject constructor(
    private val vibrator: Vibrator
) {

    @VisibleForTesting
    val pattern = longArrayOf(0, 200, 500)

    fun startDeviceVibration() {
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