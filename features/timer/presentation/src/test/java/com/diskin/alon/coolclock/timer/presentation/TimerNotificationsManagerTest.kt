package com.diskin.alon.coolclock.timer.presentation

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.timer.presentation.util.CHANNEL_ID_TIMER_ALERT
import com.diskin.alon.coolclock.timer.presentation.util.NOTIFICATION_ID_TIMER_ALERT
import com.diskin.alon.coolclock.timer.presentation.util.TimerNotificationsManager
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerNotificationsManagerTest {

    // Test subject
    private lateinit var manager: TimerNotificationsManager

    // Collaborators
    private val app: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        manager = TimerNotificationsManager(app)
    }

    @Test
    fun showAndRemoveUrgentStatusBarAlertNotification() {
        // Given
        val notificationManager: NotificationManager =
            app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // When
        manager.showTimerAlertNotification()

        // Then
        val channel = NotificationManagerCompat.from(app).getNotificationChannel(CHANNEL_ID_TIMER_ALERT)!!

        assertThat(channel).isNotNull()
        assertThat(channel.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)
        assertThat(notificationManager.activeNotifications.size).isEqualTo(1)
        assertThat(notificationManager.activeNotifications[0].id).isEqualTo(NOTIFICATION_ID_TIMER_ALERT)
        assertThat(notificationManager.activeNotifications[0].notification.extras.getString(Notification.EXTRA_TITLE))
            .isEqualTo(app.getString(R.string.timer_alert_notification_title))
        assertThat(notificationManager.activeNotifications[0].notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo(app.getString(R.string.timer_alert_notification_content))
        assertThat(notificationManager.activeNotifications[0].notification.priority).isEqualTo(NotificationCompat.PRIORITY_HIGH)

        // When
        manager.dismissTimerAlertNotification()

        // Then
        assertThat(notificationManager.activeNotifications.size).isEqualTo(0)
    }
}