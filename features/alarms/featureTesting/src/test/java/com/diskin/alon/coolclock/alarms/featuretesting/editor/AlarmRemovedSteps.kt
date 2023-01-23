package com.diskin.alon.coolclock.alarms.featuretesting.editor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
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
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowAlarmManager

class AlarmRemovedSteps(
    private val db: TestDatabase,
    alarmManager: AlarmManager,
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val updateHour = 16
    private val updateMinute = 15
    private val updatedId = 1
    private val shadowAlarmManager = Shadows.shadowOf(alarmManager)
    private val currentTime = DateTime(2022,9,17,19,0)
    private val nextAlarms = listOf(
        DateTime(2022,9,18,23,45),
        DateTime(2022,9,19,16,15)
    )

    init {
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)

        // Config fake ringtone manager
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title","ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursorValues(ringtoneValues)
    }

    @Given("^app has 2 existing scheduled alarms$")
    fun app_has_2_existing_scheduled_alarms() {
        // Insert alarms data to db
        val insertFirstAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('name_1',23,45,'empty',1,'ringtone1_uri/1',0,5,5,0,0,1)"
        val insertSecondAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('name_2',16,15,'empty',1,'ringtone2_uri/2',0,5,5,0,0,2)"

        db.compileStatement(insertFirstAlarm).executeInsert()
        db.compileStatement(insertSecondAlarm).executeInsert()

        // Set scheduled alarms to alarm manager
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstAlarmPendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(KEY_ALARM_ID,1)
            PendingIntent.getBroadcast(context, 1, intent, 0)
        }
        val secondAlarmPendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(KEY_ALARM_ID,2)
            PendingIntent.getBroadcast(context, 2, intent, 0)
        }
        val scheduledFirstAlarm = ShadowAlarmManager.ScheduledAlarm(
            AlarmManager.RTC_WAKEUP,
            nextAlarms[0].millis,
            firstAlarmPendingIntent,
            null
        )
        val scheduledSecondAlarm = ShadowAlarmManager.ScheduledAlarm(
            AlarmManager.RTC_WAKEUP,
            nextAlarms[1].millis,
            secondAlarmPendingIntent,
            null
        )

        shadowAlarmManager.scheduledAlarms.add(scheduledFirstAlarm)
        shadowAlarmManager.scheduledAlarms.add(scheduledSecondAlarm)
    }

    @When("^user updates one of them to have trigger time same as other$")
    fun user_updates_one_of_them_to_have_trigger_time_same_as_other() {
        // Launch editor fragment
        val bundle = bundleOf(KEY_ALARM_ID_ARG to updatedId)
        scenario = launchFragmentInHiltContainer<AlarmEditorFragment>(fragmentArgs = bundle)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

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

    @Then("^app should remove other alarm$")
    fun app_should_remove_other_alarm() {
        val dbAlarms = db.compileStatement("SELECT COUNT(*) FROM user_alarms").simpleQueryForLong().toInt()
        val actualHour = db.compileStatement("SELECT hour FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualMinute = db.compileStatement("SELECT minute FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualScheduled = when(db.compileStatement("SELECT isScheduled FROM user_alarms WHERE id = 1").simpleQueryForString()) {
            "1" -> true
            else -> false
        }

        assertThat(dbAlarms).isEqualTo(1)
        assertThat(actualHour).isEqualTo(updateHour)
        assertThat(actualMinute).isEqualTo(updateMinute)
        assertThat(actualScheduled).isEqualTo(true)
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(1)
    }
}