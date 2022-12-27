package com.diskin.alon.coolclock.featuretesting.timer_notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import android.widget.Button
import android.widget.NumberPicker
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.R
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.diskin.alon.coolclock.timer.presentation.device.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.device.NOTIFICATION_ID_TIMER
import com.diskin.alon.coolclock.timer.presentation.device.TimerNotificationReceiver
import com.diskin.alon.coolclock.timer.presentation.device.TimerService
import com.diskin.alon.coolclock.timer.presentation.model.UiTimer
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerState
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.greenrobot.eventbus.EventBus
import org.robolectric.Robolectric
import org.robolectric.Shadows

class TimerNotificationShownSteps : GreenCoffeeSteps() {

    private var scenario = launchFragmentInHiltContainer<TimerFragment>()
    private var service = Robolectric.setupService(TimerService::class.java)

    init {
        EventBus.getDefault().post(UiTimer(0,0,0,0, UiTimerState.DONE))
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

    @When("^he close timer screen$")
    fun he_close_timer_screen() {
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Then("^app should show timer notification in status bar$")
    fun app_should_show_timer_notification_in_status_bar() {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        assertThat(notificationManager.activeNotifications.size).isEqualTo(1)
        assertThat(notificationManager.activeNotifications[0].id)
            .isEqualTo(NOTIFICATION_ID_TIMER)
        assertThat(
            notificationManager.activeNotifications[0].notification.extras.getString(
                Notification.EXTRA_TITLE
            )
        )
            .isEqualTo(app.getString(R.string.title_timer_notification))
        assertThat(notificationManager.activeNotifications[0].notification.actions[0].title)
            .isEqualTo(app.getString(R.string.title_notification_action_cancel))
        assertThat(notificationManager.activeNotifications[0].notification.actions[1].title)
            .isEqualTo(app.getString(R.string.title_notification_action_pause))
//        assertThat(
//            notificationManager.activeNotifications[0].notification.extras.getString(
//                Notification.EXTRA_TEXT
//            )
//        )
//            .isEqualTo(app.getString(R.string.timer_alert_notification_content))
    }

    @When("^user \"([^\"]*)\" timer via notification$")
    fun user_something_timer_via_notification(action: String) {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        when(action) {
            "pause" -> {
                val intent = notificationManager.activeNotifications[0].notification.actions[1].actionIntent
                if (Shadows.shadowOf(intent).isBroadcastIntent) {
                    TimerNotificationReceiver().onReceive(app,Shadows.shadowOf(intent).savedIntent)
                }

            }
            "cancel" -> {
                val intent = notificationManager.activeNotifications[0].notification.actions[0].actionIntent
                if (Shadows.shadowOf(intent).isBroadcastIntent) {
                    TimerNotificationReceiver().onReceive(app,Shadows.shadowOf(intent).savedIntent)
                }
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$action")
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should \"([^\"]*)\" timer$")
    fun app_should_something_timer(result: String) {
        when(result) {
            "pause timer" -> assertThat(service.isTimerRunning).isFalse()
            "cancel timer" -> assertThat(Shadows.shadowOf(service).isStoppedBySelf).isTrue()
            else -> throw IllegalArgumentException("Unknown scenario arg:$result")
        }
    }
}