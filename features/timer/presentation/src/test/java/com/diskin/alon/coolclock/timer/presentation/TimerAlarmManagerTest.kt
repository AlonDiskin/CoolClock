package com.diskin.alon.coolclock.timer.presentation

import android.app.Application
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import com.diskin.alon.coolclock.timer.presentation.device.TimerAlarmManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class TimerAlarmManagerTest {

    // Test subject
    private lateinit var manager: TimerAlarmManager

    // Collaborators
    private val app: Application = mockk()

    // Stub data
    private val alarmRingtone: Ringtone = mockk()
    private val alarmRingtoneUri: Uri = mockk()

    @Before
    fun setUp() {
        mockkStatic(RingtoneManager::class)

        every { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) } returns alarmRingtoneUri
        every { RingtoneManager.getRingtone(app,alarmRingtoneUri) } returns alarmRingtone

        manager = TimerAlarmManager(app)
    }

    @Test
    fun playOngoingAlarmSoundOnDevice_WhenAlarmStarted() {
        // Given
        every { alarmRingtone.play() } returns Unit

        // When
        manager.startAlarm()

        // Then
        verify { alarmRingtone.play() }
    }

    @Test
    fun stopOngoingAlarmSoundOnDevice_WhenAlarmStopped() {
        // Given
        every { alarmRingtone.stop() } returns Unit

        // When
        manager.stopAlarm()

        // Then
        verify { alarmRingtone.stop() }
    }
}