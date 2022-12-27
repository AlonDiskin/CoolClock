package com.diskin.alon.coolclock.alarms.featuretesting.browser

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.domain.Sound
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
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager

class AlarmActivationSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val currentTime = DateTime(2022,9,12,19,0)
    private val nextAlarms = listOf(
        DateTime(2022,9,18,12,10),
        DateTime(2022,9,19,12,10)
    )

    init {
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)
    }

    @Given("^user browsed to alarm that is in \"([^\"]*)\" state$")
    fun user_browsed_to_alarm_that_is_in_something_state(current: String) {
        // Set existing alarm in db
        val existingAlarm = when(current) {
            "active" -> {
                val context = ApplicationProvider.getApplicationContext<Context>()
                val shadowAlarmManager = Shadows.shadowOf(alarmManager)
                val weekMillisInterval = 1000L * 60 * 60 * 24 * 7

                nextAlarms.forEachIndexed { index, dateTime ->
                    val alarmPendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                        intent.action = ACTION_ALARM
                        intent.addCategory(getWeekDayFromScheduledAlarm(dateTime).name)

                        intent.putExtra(KEY_ALARM_ID,1)
                        PendingIntent.getBroadcast(context, 1, intent, 0)
                    }
                    val scheduledAlarm = ShadowAlarmManager.ScheduledAlarm(
                        AlarmManager.RTC_WAKEUP,
                        dateTime.millis,
                        weekMillisInterval,
                        alarmPendingIntent,
                        null
                    )

                    shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
                }

                AlarmEntity(
                    "name_1",
                    12,
                    10,
                    setOf(WeekDay.SUN, WeekDay.MON),
                    true,
                    Sound.AlarmSound("sound_1"),
                    false,
                    5,
                    1,
                    5,
                    false
                )
            }

            "not active" -> {
                AlarmEntity(
                    "name_1",
                    12,
                    10,
                    setOf(WeekDay.SUN, WeekDay.MON),
                    false,
                    Sound.AlarmSound("sound_1"),
                    false,
                    5,
                    1,
                    5,
                    false
                )
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$current")
        }

        db.alarmDao().insert(existingAlarm).blockingGet()

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
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        when(change) {
            "active" -> {
                // Check db has changed alarm activation state to 'active'
                val weekMillis = 1000L * 60 * 60 * 24 * 7
                val alarm = db.alarmDao().get(1).blockingGet()

                assertThat(alarm.isScheduled).isTrue()

                // Check alarm manager has scheduled existing alarm
                assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(nextAlarms.size)
                shadowAlarmManager.scheduledAlarms.forEachIndexed { index, scheduledAlarm ->
                    val alarmPendingIntent = scheduledAlarm.operation!!
                    val alarmIntent = Shadows.shadowOf(alarmPendingIntent).savedIntent

                    assertThat(scheduledAlarm.triggerAtTime).isEqualTo(nextAlarms[index].millis)
                    assertThat(scheduledAlarm.interval).isEqualTo(weekMillis)
                    assertThat(scheduledAlarm.type).isEqualTo(AlarmManager.RTC_WAKEUP)
                    assertThat(alarmPendingIntent.isBroadcast).isTrue()

                    assertThat(alarmIntent.action).isEqualTo(ACTION_ALARM)
                    assertThat(alarmIntent.hasCategory(getWeekDayFromScheduledAlarm(nextAlarms[index])
                        .name)).isTrue()

                    assertThat(alarmIntent.getIntExtra(KEY_ALARM_ID,-1)).isEqualTo(alarm.id)
                    assertThat(alarmIntent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
                }

                // Check alarm ui active state updated accordingly
                onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
                    .check(matches(withSwitchChecked(true)))
            }
            "not active" -> {
                // Check db has changed alarm activation state to 'not active'
                val alarm = db.alarmDao().get(1).blockingGet()

                assertThat(alarm.isScheduled).isFalse()

                // Check alarm manager has cancel existing alarm
                assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(0)

                // Check alarm ui active state updated accordingly
                onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
                    .check(matches(withSwitchChecked(false)))
            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$change")
        }
    }

    private fun getWeekDayFromScheduledAlarm(date: DateTime): WeekDay {
        return when(date.dayOfWeek) {
            DateTimeConstants.SUNDAY -> WeekDay.SUN
            DateTimeConstants.MONDAY -> WeekDay.MON
            DateTimeConstants.TUESDAY -> WeekDay.TUE
            DateTimeConstants.WEDNESDAY -> WeekDay.WED
            DateTimeConstants.THURSDAY -> WeekDay.THU
            DateTimeConstants.FRIDAY -> WeekDay.FRI
            DateTimeConstants.SATURDAY -> WeekDay.SAT
            else -> throw IllegalArgumentException("Unknown week day arg")
        }
    }
}