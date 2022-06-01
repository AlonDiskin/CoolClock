package com.diskin.alon.coolclock.featuretesting.timer

import android.os.Looper
import android.widget.NumberPicker
import androidx.test.core.app.ActivityScenario
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.R
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

class TimerRestoredSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val selectedSeconds = 15

    @Given("^user open timer screen for first time$")
    fun user_open_timer_screen_for_first_time() {
        scenario = launchFragmentInHiltContainer<TimerFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^timer should show timer set to zero$")
    fun timer_should_show_timer_set_to_zero() {
        scenario.onActivity {
            assertThat(it.findViewById<NumberPicker>(R.id.seconds_picker).value).isEqualTo(0)
            assertThat(it.findViewById<NumberPicker>(R.id.minutes_picker).value).isEqualTo(0)
            assertThat(it.findViewById<NumberPicker>(R.id.hours_picker).value).isEqualTo(0)
        }
    }

    @When("^user pick timer duration$")
    fun user_pick_timer_duration() {
        scenario.onActivity {
            it.findViewById<NumberPicker>(R.id.seconds_picker).value = selectedSeconds
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^exit timer screen$")
    fun exit_timer_screen() {
        scenario.recreate()
    }

    @When("^he return to timer screen$")
    fun he_return_to_timer_screen() {
    }

    @Then("^app should show last timer pick as current timer duration$")
    fun app_should_show_last_timer_pick_as_current_timer_duration() {
        scenario.onActivity {
            assertThat(it.findViewById<NumberPicker>(R.id.seconds_picker).value).isEqualTo(selectedSeconds)
            assertThat(it.findViewById<NumberPicker>(R.id.minutes_picker).value).isEqualTo(0)
            assertThat(it.findViewById<NumberPicker>(R.id.hours_picker).value).isEqualTo(0)
        }
    }
}