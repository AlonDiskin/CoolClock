package com.diskin.alon.coolclock.alarms.application.usecase

import com.diskin.alon.coolclock.alarms.application.interfaces.RingtoneSamplePlayer
import com.diskin.alon.coolclock.alarms.application.model.PlayRingtoneSampleRequest
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.UseCase
import io.reactivex.Single
import javax.inject.Inject

const val SAMPLE_DURATION = 3000L

class PlayRingtoneSampleUseCase @Inject constructor(
    private val ringtonePlayer: RingtoneSamplePlayer
) : UseCase<PlayRingtoneSampleRequest, Single<AppResult<Unit>>> {

    override fun execute(param: PlayRingtoneSampleRequest): Single<AppResult<Unit>> {
        return when(param) {
            is PlayRingtoneSampleRequest.Ringtone ->
                ringtonePlayer.play(param.path, param.volume, SAMPLE_DURATION)
            is PlayRingtoneSampleRequest.Stop -> ringtonePlayer.stop()
        }
    }
}