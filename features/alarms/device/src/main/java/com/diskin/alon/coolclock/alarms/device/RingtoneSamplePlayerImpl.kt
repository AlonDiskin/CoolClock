package com.diskin.alon.coolclock.alarms.device

import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.CountDownTimer
import androidx.annotation.VisibleForTesting
import com.diskin.alon.coolclock.alarms.application.interfaces.RingtoneSamplePlayer
import com.diskin.alon.coolclock.common.application.AppError
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class RingtoneSamplePlayerImpl @Inject constructor(
    private val ringtoneManager: RingtoneManager,
    private val audioManager: AudioManager
) : RingtoneSamplePlayer {

    @VisibleForTesting
    var countDownTimer: CountDownTimer? = null

    init {
        // Set audio stream
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
    }

    override fun play(path: String, volume: Int, duration: Long): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            countDownTimer?.cancel()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,volume,0)
            ringtoneManager.getRingtone(ringtoneManager
                .getRingtonePosition(Uri.parse(path)))
                .play()
            countDownTimer = object : CountDownTimer(duration, 1000) {

                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    ringtoneManager.stopPreviousRingtone()
                }

            }.start()

            it.onSuccess(Unit)
        }.subscribeOn(AndroidSchedulers.mainThread())
            .toSingleAppResult { AppError.INTERNAL_ERROR }
    }

    override fun stop(): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            countDownTimer?.cancel()
            ringtoneManager.stopPreviousRingtone()
            it.onSuccess(Unit)
        }.subscribeOn(AndroidSchedulers.mainThread())
            .toSingleAppResult { AppError.INTERNAL_ERROR }
    }
}