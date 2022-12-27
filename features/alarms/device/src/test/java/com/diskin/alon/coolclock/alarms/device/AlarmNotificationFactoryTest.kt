package com.diskin.alon.coolclock.alarms.device

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.*
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(instrumentedPackages = ["androidx.loader.content"],application = HiltTestApplication::class)
class AlarmNotificationFactoryTest {

    // Test subject
    private lateinit var factory: AlarmNotificationFactory

    // Collaborators
    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(appContext)

    @Before
    fun setUp() {
        // Init subject
        factory = AlarmNotificationFactory(appContext, notificationManager)
    }

    @Test
    fun buildAlarmNotification() {
        // Given
        val alarm = DeviceAlarm(
            1,
            false,
            "ringtone_path",
            5,
            5,
            true,
            "alarm_name"
        )

        // When
        val actualNotification = factory.createAlarmNotification(alarm)

        // Then
        assertThat(notificationManager.getNotificationChannel(CHANNEL_ALARM_ID)!!.importance)
            .isEqualTo(NotificationManager.IMPORTANCE_HIGH)
        assertThat(actualNotification.smallIcon.resId).isEqualTo(R.drawable.ic_baseline_alarm_18)
        assertThat(actualNotification.extras.getString("android.text")).isEqualTo(alarm.name)
        assertThat(actualNotification.category).isEqualTo(Notification.CATEGORY_ALARM)
        assertThat(actualNotification.channelId).isEqualTo(CHANNEL_ALARM_ID)
    }
}