package com.diskin.alon.coolclock.featuretesting

import android.os.Looper
import android.widget.Button
import android.widget.NumberPicker
import androidx.constraintlayout.motion.widget.MotionLayout
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.R
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.diskin.alon.coolclock.timer.presentation.model.UiTimer
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerState
import com.diskin.alon.coolclock.timer.presentation.util.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.util.TimerService
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.greenrobot.eventbus.EventBus
import org.robolectric.Robolectric
import org.robolectric.Shadows

class ControlTimerSteps : GreenCoffeeSteps() {

    private var scenario = launchFragmentInHiltContainer<TimerFragment>()
    private var service = Robolectric.setupService(TimerService::class.java)

    init {
        EventBus.getDefault().post(UiTimer(0,0,0,0,UiTimerState.DONE))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Given("^user started selected timer$")
    fun user_started_selected_timer() {
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

        assertThat(serviceIntent.getLongExtra(KEY_TIMER_DURATION,0L))
            .isEqualTo(selectedDuration)
        service.onStartCommand(serviceIntent,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user \"([^\"]*)\" timer$")
    fun user_something_timer(action: String) {
        when(action) {
            "pause" -> {
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.fragments[0]

                    // Click on pause timer button
                    fragment.view!!.findViewById<Button>(R.id.buttonPauseResume).performClick()
                }
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
            "none" -> {

            }
            "cancel" -> {
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.fragments[0]

                    // Click on pause timer button
                    fragment.view!!.findViewById<Button>(R.id.buttonStartCancel).performClick()
                }
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$action")
        }
    }

    @Then("^then app should \"([^\"]*)\" timer$")
    fun then_app_should_something_timer(result: String) {
        when(result) {
            "pause" -> {
                assertThat(service.isTimerRunning).isFalse()
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.fragments[0]
                    val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
                    val endState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).endState

                    assertThat(state).isEqualTo(endState)
                }
            }
            "start" -> {
                assertThat(service.isTimerRunning).isTrue()
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.fragments[0]
                    val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
                    val endState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).endState

                    assertThat(state).isEqualTo(endState)
                }
            }
            "stop" -> {
                assertThat(Shadows.shadowOf(service).isStoppedBySelf).isTrue()
                scenario.onActivity {
                    val fragment = it.supportFragmentManager.fragments[0]
                    val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
                    val startState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).startState

                    assertThat(state).isEqualTo(startState)
                }
            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$result")
        }
    }
}