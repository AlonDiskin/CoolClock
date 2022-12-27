package com.diskin.alon.coolclock.scenario

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.coolclock.di.AppTestDatabase
import com.diskin.alon.coolclock.util.DeviceUtil
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.hamcrest.CoreMatchers.allOf

class ScheduleNewAlarmSteps(
    private val db: AppTestDatabase
) : GreenCoffeeSteps() {

    @Given("^user launch app from home screen$")
    fun user_launch_app_from_home_screen() {
        // Launch app from
        DeviceUtil.launchAppFromHome()
    }

    @And("^open new alarm editor$")
    fun open_new_alarm_editor() {
        // Open new alarm editor screen
        onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.action_add_alarm))
            .perform(click())
    }

    @When("^he confirm new alarm edit$")
    fun he_confirm_new_alarm_edit() {
        onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.fab))
            .perform(click())
        Thread.sleep(2000)
    }

    @Then("^app should schedule alarm$")
    fun app_should_schedule_alarm() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Verify app added alarm to db
        val tableSize = db.compileStatement("SELECT COUNT(*) FROM user_alarms")
            .simpleQueryForLong()

        assertThat(tableSize).isEqualTo(1)

        // Verify alarm was scheduled via alarm pending intent
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(KEY_ALARM_ID,1)
            PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_NO_CREATE)
        }

        assertThat(alarmIntent).isNotNull()
    }

    @And("^display it in alarms browser$")
    fun display_it_in_alarms_browser() {
        onView(allOf(
            withId(com.diskin.alon.coolclock.alarms.presentation.R.id.alarms),
            isAssignableFrom(RecyclerView::class.java)
        ))
            .check(matches(isRecyclerViewItemsCount(1)))
    }
}