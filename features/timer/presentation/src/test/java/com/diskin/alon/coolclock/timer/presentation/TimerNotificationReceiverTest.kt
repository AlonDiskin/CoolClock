package com.diskin.alon.coolclock.timer.presentation

import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.timer.presentation.device.NOTIFICATION_ID_TIMER_ALERT
import com.diskin.alon.coolclock.timer.presentation.device.TimerAlarmManager
import com.diskin.alon.coolclock.timer.presentation.model.TimerControl
import com.diskin.alon.coolclock.timer.presentation.device.TimerNotificationReceiver
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"],application = HiltTestApplication::class)
class TimerNotificationReceiverTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var receiver: TimerNotificationReceiver

    // Collaborators
    @BindValue
    @JvmField
    val eventBus: EventBus = mockk()

    @BindValue
    @JvmField
    val notificationManager: NotificationManagerCompat = mockk()

    @BindValue
    @JvmField
    val alarmManager: TimerAlarmManager = mockk()

    @BindValue
    @JvmField
    val sh: SharedPreferences = mockk()

    @Before
    fun setUp() {
        receiver = TimerNotificationReceiver()
    }

    @Test
    fun cancelAlertNotification_WhenCancelTimerAlertBroadcastReceived() {
        // Given
        val broadcastIntent = Intent().apply { action = "ACTION_TIMER_ALERT_CANCEL" }

        every { notificationManager.cancel(any()) } returns Unit
        every { alarmManager.stopAlarm() } returns Unit

        // When
        receiver.onReceive(ApplicationProvider.getApplicationContext(),broadcastIntent)

        // Then
        verify { notificationManager.cancel(NOTIFICATION_ID_TIMER_ALERT) }
    }

    @Test
    fun cancelAlertAlarm_WhenCancelTimerAlertBroadcastReceived() {
        // Given
        val broadcastIntent = Intent().apply { action = "ACTION_TIMER_ALERT_CANCEL" }

        every { alarmManager.stopAlarm() } returns Unit
        every { notificationManager.cancel(any()) } returns Unit

        // When
        receiver.onReceive(ApplicationProvider.getApplicationContext(),broadcastIntent)

        // Then
        verify { alarmManager.stopAlarm() }
    }

    @Test
    fun pauseTimer_WhenPauseTimerBroadcastReceived() {
        // Given
        val broadcastIntent = Intent().apply { action = "ACTION_TIMER_PAUSE" }

        every { eventBus.post(any()) } returns Unit

        // When
        receiver.onReceive(ApplicationProvider.getApplicationContext(),broadcastIntent)

        // Then
        verify { eventBus.post(TimerControl.PAUSE) }
    }

    @Test
    fun resumeTimer_WhenResumeTimerBroadcastReceived() {
        // Given
        val broadcastIntent = Intent().apply { action = "ACTION_TIMER_RESUME" }

        every { eventBus.post(any()) } returns Unit

        // When
        receiver.onReceive(ApplicationProvider.getApplicationContext(),broadcastIntent)

        // Then
        verify { eventBus.post(TimerControl.RESUME) }
    }

    @Test
    fun cancelTimer_WhenCancelTimerBroadcastReceived() {
        // Given
        val broadcastIntent = Intent().apply { action = "ACTION_TIMER_CANCEL" }

        every { eventBus.post(any()) } returns Unit

        // When
        receiver.onReceive(ApplicationProvider.getApplicationContext(),broadcastIntent)

        // Then
        verify { eventBus.post(TimerControl.CANCEL) }
    }
}