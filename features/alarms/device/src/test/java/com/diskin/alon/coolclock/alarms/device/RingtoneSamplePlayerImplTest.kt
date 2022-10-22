package com.diskin.alon.coolclock.alarms.device

import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.CountDownTimer
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.common.application.AppResult
import com.google.common.truth.Truth.*
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class RingtoneSamplePlayerImplTest {

    // Test subject
    private lateinit var player: RingtoneSamplePlayerImpl

    // Collaborators
    private val audioManager: AudioManager = mockk()
    private val ringtoneManager: RingtoneManager = mockk()

    @Before
    fun setUp() {
        every { ringtoneManager.setType(any()) } returns Unit

        player = RingtoneSamplePlayerImpl(ringtoneManager,audioManager)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun configPlayOnAlarmStream() {
        // Given

        // Then
        verify(exactly = 1) { ringtoneManager.setType(RingtoneManager.TYPE_ALARM) }
    }

    @Test
    fun playRingtoneSample() {
        // Given
        val duration = 1000L
        val path = "path"
        val volume = 10
        val position = 1
        val ringtone = mockk<Ringtone>()

        every { audioManager.setStreamVolume(any(),any(),any()) } returns Unit
        every { ringtoneManager.stopPreviousRingtone() } returns Unit
        every { ringtoneManager.getRingtonePosition(any()) } returns position
        every { ringtoneManager.getRingtone(position) } returns ringtone
        every { ringtone.play() } returns Unit
        every { ringtoneManager.stopPreviousRingtone() } returns Unit


        // When
        val observer = player.play(path, volume, duration).test()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { audioManager.setStreamVolume(AudioManager.STREAM_ALARM,volume,0) }
        verify(exactly = 1) { ringtoneManager.getRingtonePosition(Uri.parse(path)) }
        verify(exactly = 1) { ringtoneManager.getRingtone(position) }
        verify(exactly = 1) { ringtone.play() }
        assertThat(Shadows.shadowOf(player.countDownTimer!!).hasStarted()).isTrue()
        observer.assertValue(AppResult.Success(Unit))

        // When
        Shadows.shadowOf(player.countDownTimer!!).invokeFinish()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { ringtoneManager.stopPreviousRingtone() }
    }

    @Test
    fun stopPlayer() {
        // Given
        player.countDownTimer = object : CountDownTimer(5000,1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {

            }

        }.start()

        every { ringtoneManager.stopPreviousRingtone() } returns Unit

        // When
        val observer = player.stop().test()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { ringtoneManager.stopPreviousRingtone() }
        observer.assertValue(AppResult.Success(Unit))
        assertThat(Shadows.shadowOf(player.countDownTimer!!).hasStarted()).isFalse()
    }
}