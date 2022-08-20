package com.diskin.alon.coolclock.alarms.featuretesting.browser

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.ALARM_ID
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmsFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.common.uitesting.withSwitchChecked
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.*
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows

class AlarmActivationSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val intentSlot = slot<Intent>()
    private val requestCodeSlot = slot<Int>()
    private val pendingIntent = mockk<PendingIntent>()
    private val currentTime = DateTime(2022,9,12,19,0)
    private lateinit var nextAlarms: List<DateTime>

    init {
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)
    }

    @Given("^user browsed to alarm that is in \"([^\"]*)\" state$")
    fun user_browsed_to_alarm_that_is_in_something_state(current: String) {
        // Set existing alarm in db, and stub mocked alarm manager according to scenario
        mockkStatic(PendingIntent::class)

        val existingAlarm = when(current) {
            "active" -> {
                every { alarmManager.cancel(any<PendingIntent>()) } returns Unit
                every { PendingIntent.getBroadcast(any(),capture(requestCodeSlot),capture(intentSlot),any()) } returns pendingIntent

                AlarmEntity(
                    "name_1",
                    12,
                    10,
                    setOf(WeekDay.SUN, WeekDay.MON),
                    true,
                    "sound_1",
                    false,
                    true,
                    1,
                    5,
                    false,
                    5,
                    1
                )
            }

            "not active" -> {
                every { alarmManager.setRepeating(any(),any(),any(),any()) } returns Unit
                every { PendingIntent.getBroadcast(any(),capture(requestCodeSlot),capture(intentSlot),any()) } returns pendingIntent

                nextAlarms = listOf(
                    DateTime(2022,9,18,12,10),
                    DateTime(2022,9,19,12,10)
                )

                AlarmEntity(
                    "name_1",
                    12,
                    10,
                    setOf(WeekDay.SUN, WeekDay.MON),
                    false,
                    "sound_1",
                    false,
                    true,
                    1,
                    5,
                    false,
                    5,
                    1
                )
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$current")
        }

        db.alarmDao().insert(existingAlarm).blockingAwait()

        // Launch alarms fragment
        scenario = launchFragmentInHiltContainer<AlarmsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
    }

    @When("^he switch activation to \"([^\"]*)\"$")
    fun he_switch_activation_to_something(change: String) {
        // Verify alarm activation shown accordingly,prior to changing it
        val expectedUiActivationState = when(change) {
            "active" -> false
            "not active" -> true
            else -> throw IllegalArgumentException("Unknown scenario arg:$change")
        }

        onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
            .check(matches(withSwitchChecked(expectedUiActivationState)))

        // Click the alarm activation switcher
        onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should change alarm activation to \"([^\"]*)\"$")
    fun app_should_change_alarm_activation_to_something(change: String) {
        when(change) {
            "active" -> {
                // Check db has changed alarm activation state to 'active'
                val weekMillis = 1000L * 60 * 60 * 24 * 7
                val actualActive = db.alarmDao().get(1).blockingGet().isActive

                assertThat(actualActive).isTrue()

                // Check alarm manager has scheduled existing alarm
                nextAlarms.forEach {
                    verify(exactly = 1) {
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            it.millis,
                            weekMillis,
                            pendingIntent
                        )
                    }
                }
                assertThat(requestCodeSlot.captured).isEqualTo(1)
                assertThat(intentSlot.captured.action).isEqualTo(ACTION_ALARM)
                assertThat(intentSlot.captured.getIntExtra(ALARM_ID,-1)).isEqualTo(1)

                // Check alarm ui active state updated accordingly
                onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
                    .check(matches(withSwitchChecked(true)))
            }
            "not active" -> {
                // Check db has changed alarm activation state to 'not active'
                val actualActive = db.alarmDao().get(1).blockingGet().isActive

                assertThat(actualActive).isFalse()

                // Check alarm manager has cancel existing alarm
                verify(exactly = 1) { alarmManager.cancel(pendingIntent) }
                assertThat(requestCodeSlot.captured).isEqualTo(1)
                assertThat(intentSlot.captured.action).isEqualTo(ACTION_ALARM)
                assertThat(intentSlot.captured.getIntExtra(ALARM_ID,-1)).isEqualTo(1)

                // Check alarm ui active state updated accordingly
                onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
                    .check(matches(withSwitchChecked(false)))
            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$change")
        }
    }
}