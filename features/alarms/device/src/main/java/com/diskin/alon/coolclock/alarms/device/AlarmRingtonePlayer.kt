package com.diskin.alon.coolclock.alarms.device

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PLAYER_TAG = "AlarmRingtonePlayer"

class RingtonePlayer @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val mediaPlayer: MediaPlayer
) : MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    init {
        // Set audio attributes
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        // Set listeners
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnErrorListener(this)
    }

    fun play(uri: Uri) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(appContext,uri)
        mediaPlayer.prepareAsync()
    }

    fun stop() {
        try {
            mediaPlayer.stop()
        } catch (error: Throwable) {
            Log.d(PLAYER_TAG,"player error:$error")
        }
    }

    fun release() {
        mediaPlayer.release()
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.d(PLAYER_TAG,"player error:${what},${extra}")
        return true
    }
}