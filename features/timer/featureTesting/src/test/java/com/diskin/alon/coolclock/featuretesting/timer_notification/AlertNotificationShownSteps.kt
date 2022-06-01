package com.diskin.alon.coolclock.featuretesting.timer_notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.R
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.diskin.alon.coolclock.timer.presentation.infrastructure.*
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.verify
import org.robolectric.Robolectric
import org.robolectric.Shadows

class AlertNotificationShownSteps(
    private val alarmManager: TimerAlarmManager
) : GreenCoffeeSteps() {

    private var scenario = launchFragmentInHiltContainer<TimerFragment>()
    private var service = Robolectric.setupService(TimerService::class.java)

    init {
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        every { alarmManager.startAlarm() } returns Unit
    }

    @Given("^user started timer$")
    fun user_started_timer() {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(app, TimerService::class.java).apply { putExtra(KEY_TIMER_DURATION,10L) }

        service.onStartCommand(intent,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^timer is finished$")
    fun timer_is_finished() {
        service.countDownTimer.onFinish()
    }

    @Then("^app should show urgent notification with ongoing alarm sound$")
    fun app_should_show_urgent_notification_with_ongoing_alarm_sound() {
        val app =ApplicationProvider.getApplicationContext<Context>()
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationManagerCompat.from(app).getNotificationChannel(
            CHANNEL_ID_TIMER_ALERT
        )

        assertThat(channel?.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)
        assertThat(notificationManager.activeNotifications.size).isEqualTo(1)
        assertThat(notificationManager.activeNotifications[0].id)
            .isEqualTo(NOTIFICATION_ID_TIMER_ALERT)
        assertThat(
            notificationManager.activeNotifications[0].notification.extras.getString(
                Notification.EXTRA_TITLE
            )
        )
            .isEqualTo(app.getString(R.string.timer_alert_notification_title))
        assertThat(
            notificationManager.activeNotifications[0].notification.extras.getString(
                Notification.EXTRA_TEXT
            )
        )
            .isEqualTo(app.getString(R.string.timer_alert_notification_content))

        verify { alarmManager.startAlarm() }
    }
}