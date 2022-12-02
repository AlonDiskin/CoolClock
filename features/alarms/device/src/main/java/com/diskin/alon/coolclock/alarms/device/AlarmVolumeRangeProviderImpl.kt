package com.diskin.alon.coolclock.alarms.device

import android.content.Context
import android.media.AudioManager
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmVolumeRangeProvider
import com.diskin.alon.coolclock.alarms.application.model.AlarmVolumeRange
import com.diskin.alon.coolclock.common.application.AppError
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class AlarmVolumeRangeProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmVolumeRangeProvider {

    override fun get(): Single<AppResult<AlarmVolumeRange>> {
        return Single.create<AlarmVolumeRange> {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            it.onSuccess(
                AlarmVolumeRange(
                    1,
                    am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                )
            )
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .toSingleAppResult{ AppError.INTERNAL_ERROR }
    }
}