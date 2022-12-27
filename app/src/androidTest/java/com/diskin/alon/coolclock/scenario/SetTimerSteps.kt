package com.diskin.alon.coolclock.scenario

import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import com.diskin.alon.coolclock.common.uitesting.isWithProgress
import com.diskin.alon.coolclock.timer.presentation.device.CHANNEL_ID_TIMER_ALERT
import com.diskin.alon.coolclock.timer.presentation.device.TimerAlarmManager
import com.diskin.alon.coolclock.util.DeviceUtil
import com.diskin.alon.coolclock.util.WhiteBox
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import java.util.regex.Pattern

class SetTimerSteps(
    private val alarmManager: TimerAlarmManager
) : GreenCoffeeSteps() {

    init {
        //DeviceUtil.clearSharedPrefs()
    }

    @Given("^user started a timer$")
    fun user_started_a_timer() {
        // Launch app from home
        DeviceUtil.launchAppFromHome()

        // Open timer screen
        onView(withId(com.diskin.alon.coolclock.home.presentation.R.id.timer))
            .perform(click())

        // Select "20" seconds as timer duration
        onView(withId(com.diskin.alon.coolclock.timer.presentation.R.id.seconds_picker))
            .perform(click())
        waitUntilSoftKeyboardShow(500)

        InstrumentationRegistry.getInstrumentation()
            .sendKeyDownUpSync(KeyEvent.KEYCODE_0)
        InstrumentationRegistry.getInstrumentation()
            .sendKeyDownUpSync(KeyEvent.KEYCODE_3)
        InstrumentationRegistry.getInstrumentation()
            .sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER)

        // Exist keyboard
        DeviceUtil.pressBack()

        // Start timer
        onView(withId(com.diskin.alon.coolclock.timer.presentation.R.id.buttonStartCancel))
            .perform(click())
    }

    @When("^timer finish$")
    fun timer_finish() {
        // Verify timer has finished
        onView(withId(com.diskin.alon.coolclock.timer.presentation.R.id.buttonStartCancel))
            .check(matches(withText("START")))
        onView(withId(com.diskin.alon.coolclock.timer.presentation.R.id.buttonPauseResume))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
        onView(withId(com.diskin.alon.coolclock.timer.presentation.R.id.progressBar))
            .check(matches(isWithProgress(0)))
    }

    @Then("^app should show urgent status bar notification with alarm sound$")
    fun app_should_show_urgent_status_bar_notification_with_alarm_sound() {
        val app =ApplicationProvider.getApplicationContext<Context>()
        val channel = NotificationManagerCompat.from(app).getNotificationChannel(
            CHANNEL_ID_TIMER_ALERT
        )

        // Verify urgent notification is showing
        assertThat(channel?.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)

        // Verify alarm sound is playing
        val ringtone = WhiteBox.getInternalState(alarmManager,"ringtone") as Ringtone
        assertThat(ringtone.isPlaying).isTrue()
    }

    @When("^user dismiss notification$")
    fun user_dismiss_notification() {
        DeviceUtil.openNotifications()
        Thread.sleep(2000)

        val dismissButton = DeviceUtil.getDevice()
            .findObject(By.text(Pattern.compile("dismiss", Pattern.CASE_INSENSITIVE)))

        dismissButton.click()
    }

    @Then("^app should remove notification with alarm sound sound$")
    fun app_should_remove_notification_with_alarm_sound_sound() {
        // Verify notification removed
        val notification = DeviceUtil.getDevice().findObject(
            UiSelector().text("time is up!")
        )

        assertThat(notification.exists()).isFalse()

        // Verify alarm sound is off
        val ringtone = WhiteBox.getInternalState(alarmManager,"ringtone") as Ringtone
        assertThat(ringtone.isPlaying).isFalse()
    }

    private fun waitUntilSoftKeyboardShow(millis: Long) {
        Thread.sleep(millis)
        val imm: InputMethodManager = ApplicationProvider.getApplicationContext<Context>().
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (!imm.isAcceptingText) {
            throw IllegalStateException("Ui test fail: soft keyboard is not showing past timeout")
        }
    }
}