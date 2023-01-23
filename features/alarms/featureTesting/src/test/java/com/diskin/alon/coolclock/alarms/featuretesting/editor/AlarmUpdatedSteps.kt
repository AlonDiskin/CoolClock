package com.diskin.alon.coolclock.alarms.featuretesting.editor

import android.app.AlarmManager
import android.media.RingtoneManager
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.ui.AlarmEditorFragment
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.KEY_ALARM_ID_ARG
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow

class AlarmUpdatedSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val updateHour = 16
    private val updateMinute = 15

    init {
        // Config fake ringtone manager
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title","ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursorValues(ringtoneValues)
    }

    @Given("^app has \"([^\"]*)\" existing alarm$")
    @Throws(Throwable::class)
    fun app_has_something_existing_alarm(scheduledState: String) {
        // Set existing alarm state
        val existingAlarm = when(scheduledState) {
            "scheduled" -> "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                    ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                    "VALUES ('name_1',15,10,'empty',1,'ringtone1_uri/1',0,5,5,0,0,1)"

            "not scheduled" -> "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                    ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                    "VALUES ('name_1',15,10,'empty',0,'ringtone1_uri/1',0,5,5,0,0,1)"

            else -> throw IllegalArgumentException("Unknown scenario arg:$scheduledState")
        }

        db.compileStatement(existingAlarm).executeInsert()

        // Launch editor fragment
        val bundle = bundleOf(KEY_ALARM_ID_ARG to 1)
        scenario = launchFragmentInHiltContainer<AlarmEditorFragment>(fragmentArgs = bundle)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user updates existing alarm time$")
    fun user_updates_existing_alarm_time() {
        // Update alarm time
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("$updateHour o'clock"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("$updateMinute minutes"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Confirm schedule
        onView(withId(R.id.fab))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should reschedule alarm according to update$")
    fun app_should_reschedule_alarm_according_to_update() {
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        val actualHour = db.compileStatement("SELECT hour FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualMinute = db.compileStatement("SELECT minute FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualScheduled = when(db.compileStatement("SELECT isScheduled FROM user_alarms WHERE id = 1").simpleQueryForString()) {
            "1" -> true
            else -> false
        }

        assertThat(actualHour).isEqualTo(updateHour)
        assertThat(actualMinute).isEqualTo(updateMinute)
        assertThat(actualScheduled).isEqualTo(true)
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(1)
    }
}