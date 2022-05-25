package com.diskin.alon.coolclock.featuretesting

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.R
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.diskin.alon.coolclock.timer.presentation.util.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.util.NOTIFICATION_ID_TIMER_ALERT
import com.diskin.alon.coolclock.timer.presentation.util.TimerService
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Robolectric
import org.robolectric.Shadows

class AlertNotificationShownSteps : GreenCoffeeSteps() {

    private var scenario = launchFragmentInHiltContainer<TimerFragment>()
    private var service = Robolectric.setupService(TimerService::class.java)

    init {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Given("^user started timer$")
    fun user_started_timer() {
        val selectedDuration = 30000L

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val hoursPicker = fragment.view!!.findViewById<NumberPicker>(R.id.hours_picker)
            val minutesPicker = fragment.view!!.findViewById<NumberPicker>(R.id.minutes_picker)
            val secondsPicker = fragment.view!!.findViewById<NumberPicker>(R.id.seconds_picker)

            // Set timer duration
            hoursPicker.value = 0
            minutesPicker.value = 0
            secondsPicker.value = selectedDuration.toInt() / 1000

            // Click on start timer button
            fragment.view!!.findViewById<Button>(R.id.buttonStartCancel).performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val serviceIntent = Shadows.shadowOf(service).nextStartedService

        assertThat(serviceIntent.getLongExtra(KEY_TIMER_DURATION, 0L))
            .isEqualTo(selectedDuration)
        service.onStartCommand(serviceIntent,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^timer is finished$")
    fun timer_is_finished() {
        service.countDownTimer.onFinish()
    }

    @Then("^app should show notification$")
    fun app_should_show_notification() {
        val app =ApplicationProvider.getApplicationContext<Context>()
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        assertThat(notificationManager.activeNotifications[0].notification.priority)
            .isEqualTo(NotificationCompat.PRIORITY_HIGH)
    }
}