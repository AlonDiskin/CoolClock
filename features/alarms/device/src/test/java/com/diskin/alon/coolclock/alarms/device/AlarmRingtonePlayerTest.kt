package com.diskin.alon.coolclock.alarms.device

import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmRingtonePlayerTest {

    // Test subject
    private lateinit var player: AlarmRingtonePlayer

    // Collaborators
    private val ringtoneManager: RingtoneManager = mockk()
    private val audioManager: AudioManager = mockk()

    @Before
    fun setUp() {
        player = AlarmRingtonePlayer(ringtoneManager, audioManager)
    }

    @Test
    fun stopRingtone_WhenStopped() {
        // Given

        every { ringtoneManager.stopPreviousRingtone() } returns Unit

        // When
        player.stop()

        // Then
        verify(exactly = 1) { ringtoneManager.stopPreviousRingtone() }
    }

    @Test
    fun playDeviceRingtone_WhenPlayed() {
        // Given
        val path = "path"
        val volume = 10
        val position = 0
        val ringtone = mockk<Ringtone>()

        every { audioManager.setStreamVolume(AudioManager.STREAM_ALARM,volume,0) } returns Unit
        every { ringtoneManager.getRingtonePosition(Uri.parse(path)) } returns position
        every { ringtoneManager.getRingtone(position) } returns ringtone
        every { ringtone.play() } returns Unit

        // When
        player.play(path, volume)

        // Then
        verify(exactly = 1) { audioManager.setStreamVolume(AudioManager.STREAM_ALARM,volume,0) }
        verify(exactly = 1) { ringtone.play() }
    }
}