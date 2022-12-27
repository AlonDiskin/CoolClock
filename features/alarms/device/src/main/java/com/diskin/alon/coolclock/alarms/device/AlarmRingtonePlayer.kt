package com.diskin.alon.coolclock.alarms.device

import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import androidx.annotation.MainThread
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class AlarmRingtonePlayer @Inject constructor(
    private val ringtoneManager: RingtoneManager,
    private val audioManager: AudioManager
){

    @MainThread
    fun play(path: String, volume: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,volume,0)
        ringtoneManager.getRingtone(ringtoneManager.getRingtonePosition(Uri.parse(path))).play()
    }

    @MainThread
    fun stop() {
        ringtoneManager.stopPreviousRingtone()
    }
}