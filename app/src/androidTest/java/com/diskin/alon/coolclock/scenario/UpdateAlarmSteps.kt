package com.diskin.alon.coolclock.scenario

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.ALARM_ID
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.*
import com.diskin.alon.coolclock.di.AppTestDatabase
import com.diskin.alon.coolclock.util.DeviceUtil
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class UpdateAlarmSteps(
    private val db: AppTestDatabase
) : GreenCoffeeSteps() {

    @Given("^user has an existing alarm$")
    fun user_has_an_existing_alarm() {
        val insertAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed)" +
                "VALUES ('name_1',15,10,'empty',0,'none',0,5,5,0,0)"

        db.compileStatement(insertAlarm).executeInsert()
    }

    @And("^he launched app from home screen$")
    fun he_launched_app_from_home_screen() {
        // Launch app from
        DeviceUtil.launchAppFromHome()
        Thread.sleep(2000)
    }

    @When("^update trigger time for existing alarm$")
    fun update_trigger_time_for_existing_alarm() {
        // Open alarm editor for existing alarm
        onView(withRecyclerView(R.id.alarms).atPosition(0))
            .perform(click())
        Thread.sleep(2000)

        // Update trigger time
        onView(withId(R.id.alarmTime))
            .perform(click())
        onView(withContentDescription("16 o'clock"))
            .inRoot(isDialog())
            .perform(click())
        onView(withContentDescription("15 minutes"))
            .inRoot(isDialog())
            .perform(click())
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
        onView(withId(R.id.fab))
            .perform(click())
        Thread.sleep(2000)
    }

    @Then("^app should reschedule alarm$")
    fun app_should_reschedule_alarm() {
        // Verify alarm set as scheduled in db
        val actualScheduled = when(db.compileStatement("SELECT isScheduled FROM user_alarms WHERE id = 1").simpleQueryForString()) {
            "1" -> true
            else -> false
        }

        assertThat(actualScheduled).isEqualTo(true)

        // Verify alarm was scheduled via alarm pending intent
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(ALARM_ID,1)
            PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_NO_CREATE)
        }

        assertThat(alarmIntent).isNotNull()
    }

    @And("^display it in alarms browser$")
    fun display_it_in_alarms_browser() {
        onView(withRecyclerView(R.id.alarms).atPositionOnView(0, R.id.alarmTime))
            .check(ViewAssertions.matches(withText("16:15")))
    }
}