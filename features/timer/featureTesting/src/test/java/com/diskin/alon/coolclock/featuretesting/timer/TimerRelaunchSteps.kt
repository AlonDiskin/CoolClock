package com.diskin.alon.coolclock.featuretesting.timer

import android.os.Looper
import android.widget.Button
import android.widget.NumberPicker
import androidx.constraintlayout.motion.widget.MotionLayout
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.R
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.diskin.alon.coolclock.timer.presentation.device.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.device.TimerService
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Robolectric
import org.robolectric.Shadows

class TimerRelaunchSteps : GreenCoffeeSteps() {

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

    @When("^user leave timer screen$")
    fun user_leave_timer_screen() {
        scenario.onActivity { it.finish() }
    }

    @And("^return to timer screen$")
    fun return_to_timer_screen() {
        scenario = launchFragmentInHiltContainer<TimerFragment>()
    }

    @Then("^app should display current timer$")
    fun app_should_display_current_timer() {
        assertThat(service.isTimerRunning).isTrue()
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
            val endState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).endState

            assertThat(state).isEqualTo(endState)
        }
    }
}