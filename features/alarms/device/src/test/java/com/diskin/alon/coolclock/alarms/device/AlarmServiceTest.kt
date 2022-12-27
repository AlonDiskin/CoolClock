package com.diskin.alon.coolclock.alarms.device

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(instrumentedPackages = ["androidx.loader.content"])
class AlarmServiceTest {

    // Test subject
    private lateinit var service: AlarmService

    // Collaborators
    private val ringtonePlayer: AlarmRingtonePlayer = mockk()
    private val vibrationManager: AlarmVibrationManager = mockk()
    private val notificationFactory: AlarmNotificationFactory = mockk()
    private val notificationManager: NotificationManagerCompat = mockk()

    @Before
    fun setUp() {
        // Init subject
        service = Robolectric.setupService(AlarmService::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set subjects collaborators
        service.ringtonePlayer = ringtonePlayer
        service.vibrationManager = vibrationManager
        service.notificationFactory = notificationFactory
        service.notificationManager = notificationManager
    }

    @Test
    fun startAlarm_WhenStarted() {
        // Given
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val deviceAlarm = DeviceAlarm(
            1,
            false,
            "sound_1",
            2,
            5,
            false,
            "alarm_name"
        )
        val serviceIntent = Intent(appContext,AlarmService::class.java).also { intent ->
            intent.putExtra(ALARM_REQUEST,deviceAlarm)
        }

        every { vibrationManager.stopDeviceVibration() } returns Unit
        every { vibrationManager.startDeviceVibration() } returns Unit
        every { ringtonePlayer.stop() } returns Unit
        every { ringtonePlayer.play(any(),any()) } returns Unit
        every { notificationManager.cancel(any()) } returns Unit
        every { notificationFactory.createAlarmNotification(any()) } returns mockk()

        // When
        service.onStartCommand(serviceIntent,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { ringtonePlayer.play(deviceAlarm.ringtone,deviceAlarm.volume) }
        assertThat(Shadows.shadowOf(service).isLastForegroundNotificationAttached).isTrue()
        assertThat(Shadows.shadowOf(service).isForegroundStopped).isFalse()
    }

    @Test
    fun stopAlarm_WhenDestroyed() {
        // Given
        every { ringtonePlayer.stop() } returns Unit
        every { vibrationManager.stopDeviceVibration() } returns Unit
        every { notificationManager.cancel(any()) } returns Unit

        // When
        service.onDestroy()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { vibrationManager.stopDeviceVibration() }
        verify(exactly = 1) { ringtonePlayer.stop() }
        verify(exactly = 1) { notificationManager.cancel(NOTIFICATION_ID_ALARM) }
    }
}