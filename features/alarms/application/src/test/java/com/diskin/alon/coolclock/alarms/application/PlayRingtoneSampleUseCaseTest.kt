package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.RingtoneSamplePlayer
import com.diskin.alon.coolclock.alarms.application.model.PlayRingtoneSampleRequest
import com.diskin.alon.coolclock.alarms.application.usecase.PlayRingtoneSampleUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.SAMPLE_DURATION
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class PlayRingtoneSampleUseCaseTest {

    // Test subject
    private lateinit var useCase: PlayRingtoneSampleUseCase

    // Collaborators
    private val ringtonePlayer: RingtoneSamplePlayer = mockk()

    @Before
    fun setUp() {
        useCase = PlayRingtoneSampleUseCase(ringtonePlayer)
    }

    @Test
    fun stopRingtonePlaying_WhenExecutedToStop() {
        // Given
        val result = AppResult.Success(Unit)
        every { ringtonePlayer.stop() } returns Single.just(result)

        // When
        val observer = useCase.execute(PlayRingtoneSampleRequest.Stop).test()

        // Then
        verify(exactly = 1) { ringtonePlayer.stop() }
        observer.assertValue(result)
    }

    @Test
    fun playRingtoneSample_WhenExecutedToPlaySample() {
        // Given
        val request = PlayRingtoneSampleRequest.Ringtone("path",5)
        val result = AppResult.Success(Unit)

        every { ringtonePlayer.play(any(),any(),any()) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 1) { ringtonePlayer.play(request.path, request.volume, SAMPLE_DURATION) }
        observer.assertValue(result)
    }
}