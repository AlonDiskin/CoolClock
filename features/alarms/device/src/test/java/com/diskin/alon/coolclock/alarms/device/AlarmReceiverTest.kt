package com.diskin.alon.coolclock.alarms.device

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.application.usecase.SnoozeAlarmUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.StartAlarmUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.StopAlarmUseCase
import com.diskin.alon.coolclock.common.application.AppResult
import com.google.common.truth.Truth.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(instrumentedPackages = ["androidx.loader.content"])
class AlarmReceiverTest {

    // Test subject
    private lateinit var receiver: AlarmReceiver

    // Collaborators
    private val startAlarm: StartAlarmUseCase = mockk()
    private val stopAlarm: StopAlarmUseCase = mockk()
    private val snoozeAlarm: SnoozeAlarmUseCase = mockk()

    @Before
    fun setUp() {
        // Init subject
        val app = ApplicationProvider.getApplicationContext<Context>()
        receiver = Shadows.shadowOf(app as Application)
            .registeredReceivers[0]
            .broadcastReceiver as AlarmReceiver

        // Set subjects dependencies
        receiver.startAlarm = startAlarm
        receiver.stopAlarm = stopAlarm
        receiver.snoozeAlarm = snoozeAlarm
    }

    @Test
    fun startAlarm_WhenStartAlarmBroadcastReceived() {
        // Given
        val id = 1
        val broadcastIntent = Intent().apply {
            action = ACTION_ALARM
            putExtra(KEY_ALARM_ID,id)
        }

        every { startAlarm.execute(id) } returns Single.just(AppResult.Success(Unit))

        // When
        ApplicationProvider.getApplicationContext<Context>()
            .sendBroadcast(broadcastIntent)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { startAlarm.execute(id) }
        assertThat(Shadows.shadowOf(receiver).wentAsync()).isTrue()
    }

    @Test
    fun stopAlarm_WhenStopAlarmBroadcastReceived() {
        // Given
        val id = 1
        val broadcastIntent = Intent().apply {
            action = ACTION_STOP_ALARM
            putExtra(KEY_ALARM_ID,id)
        }

        every { stopAlarm.execute(id) } returns Single.just(AppResult.Success(Unit))

        // When
        ApplicationProvider.getApplicationContext<Context>()
            .sendBroadcast(broadcastIntent)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { stopAlarm.execute(id) }
        assertThat(Shadows.shadowOf(receiver).wentAsync()).isTrue()
    }

    @Test
    fun snoozeAlarm_WhenSnoozeAlarmBroadcastReceived() {
        // Given
        val id = 1
        val broadcastIntent = Intent().apply {
            action = ACTION_SNOOZE_ALARM
            putExtra(KEY_ALARM_ID,id)
        }

        every { snoozeAlarm.execute(id) } returns Single.just(AppResult.Success(Unit))

        // When
        ApplicationProvider.getApplicationContext<Context>()
            .sendBroadcast(broadcastIntent)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { snoozeAlarm.execute(id) }
        assertThat(Shadows.shadowOf(receiver).wentAsync()).isTrue()
    }
}