package com.diskin.alon.coolclock.timer.presentation

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.timer.presentation.infrastructure.CHANNEL_ID_TIMER
import com.diskin.alon.coolclock.timer.presentation.infrastructure.CHANNEL_ID_TIMER_ALERT
import com.diskin.alon.coolclock.timer.presentation.infrastructure.TimerNotificationFactory
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerNotificationFactoryTest {

    // Test subject
    private lateinit var factory: TimerNotificationFactory

    // Collaborators
    private val app: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        factory = TimerNotificationFactory(app)
    }

    @Test
    fun createTimerAlertNotificationChannel() {
        // Given

        // When
        factory.createTimerAlertNotificationChannel()

        // Then
        val channel = NotificationManagerCompat.from(app).getNotificationChannel(CHANNEL_ID_TIMER_ALERT)

        assertThat(channel).isNotNull()
        assertThat(channel!!.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)
    }

    @Test
    fun createTimerNotificationChannel() {
        // Given

        // When
        factory.createTimerNotificationChannel()

        // Then
        assertThat(NotificationManagerCompat.from(app).getNotificationChannel(CHANNEL_ID_TIMER))
            .isNotNull()
    }

    @Test
    fun createUrgentStatusBarAlertNotification() {
        // Given

        // When
        val notification = factory.createTimerAlertNotification()

        // Then
        assertThat(notification.extras.getString(Notification.EXTRA_TITLE))
            .isEqualTo(app.getString(R.string.timer_alert_notification_title))
        assertThat(notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo(app.getString(R.string.timer_alert_notification_content))
        assertThat(notification.priority).isEqualTo(NotificationCompat.PRIORITY_HIGH)
    }

    @Test
    fun createRunningTimerNotification() {
        // Given
        val seconds = 10
        val minutes = 0
        val hours = 0
        val timerText = "00:00:10"

        // When
        val notification = factory.createRunningTimerNotification(seconds, minutes, hours)

        // Then
        assertThat(notification.extras.getString(Notification.EXTRA_TITLE))
            .isEqualTo(app.getString(R.string.title_timer_notification))
        assertThat(notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo(timerText)
        assertThat(notification.actions[0].title)
            .isEqualTo(app.getString(R.string.title_notification_action_cancel))
        assertThat(notification.actions[1].title)
            .isEqualTo(app.getString(R.string.title_notification_action_pause))
    }

    @Test
    fun createPausedTimerNotification() {
        // Given
        val seconds = 10
        val minutes = 0
        val hours = 0
        val timerText = "00:00:10"

        // When
        val notification = factory.createPausedTimerNotification(seconds, minutes, hours)

        // Then
        assertThat(notification.extras.getString(Notification.EXTRA_TITLE))
            .isEqualTo(app.getString(R.string.title_timer_notification))
        assertThat(notification.extras.getString(Notification.EXTRA_TEXT))
            .isEqualTo(timerText)
        assertThat(notification.actions[0].title)
            .isEqualTo(app.getString(R.string.title_notification_action_cancel))
        assertThat(notification.actions[1].title)
            .isEqualTo(app.getString(R.string.title_notification_action_resume))
    }
}