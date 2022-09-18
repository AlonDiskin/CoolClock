package com.diskin.alon.coolclock.scenario

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.di.AppTestDatabase
import com.diskin.alon.coolclock.util.DeviceUtil
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class DisableAlarmSteps(
    private val db: AppTestDatabase
) : GreenCoffeeSteps() {

    init {
        // Pre populate test db with active alarm
        val insertAlarm = "INSERT INTO alarms (name,hour,minute,repeatDays,isActive,ringtone" +
                ",isVibrate,isSound,duration,volume,isSnooze,snoozeRepeat,snoozeInterval)" +
                "VALUES ('name_1',15,10,'empty',0,'ringtone_1',0,1,5,5,0,0,0)"

        db.compileStatement(insertAlarm).executeInsert()
    }

    @Given("^user open alarms browser screen$")
    fun user_open_alarms_browser_screen() {
        // Launch app from
        DeviceUtil.launchAppFromHome()

        // Open alarms browser screen
        onView(withContentDescription("Alarm"))
            .perform(click())
        Thread.sleep(2000)
    }

    @And("^activate non active alarm$")
    fun activate_non_active_alarm() {
        onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.active_switcher))
            .perform(click())
        Thread.sleep(2000)
    }

    @Then("^app should schedule it$")
    fun app_should_schedule_it() {
        val alarmId = 1
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmIntent = Intent(context, AlarmReceiver::class.java).also {
            it.action = ACTION_ALARM
        }
        val scheduledAlarmIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE
        )

        assertThat(scheduledAlarmIntent).isNotNull()
    }

    @When("^he delete scheduled active alarm$")
    fun he_delete_scheduled_active_alarm() {
        onView(withId(R.id.options_button))
            .perform(click())
        Thread.sleep(2000)
        onView(withText("Delete"))
            .perform(click())
        Thread.sleep(2000)
    }

    @Then("^app should cancel alarm$")
    fun app_should_cancel_alarm() {
        val alarmId = 1
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmIntent = Intent(context, AlarmReceiver::class.java).also {
            it.action = ACTION_ALARM
        }
        val scheduledAlarmIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE
        )

        assertThat(scheduledAlarmIntent).isNull()
    }

    @And("^delete it from user alarms data$")
    fun delete_it_from_user_alarms_data() {
        val actualSize = db.compileStatement("SELECT COUNT(*) FROM alarms").simpleQueryForLong()

        assertThat(actualSize).isEqualTo(0)
    }
}