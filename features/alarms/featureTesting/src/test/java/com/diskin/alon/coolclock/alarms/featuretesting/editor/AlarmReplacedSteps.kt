package com.diskin.alon.coolclock.alarms.featuretesting.editor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Looper
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmEditorFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowAlarmManager

class AlarmReplacedSteps(
    private val db: TestDatabase,
    alarmManager: AlarmManager,
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private val shadowAlarmManager = Shadows.shadowOf(alarmManager)
    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val currentDateTime = DateTime(2022,11,27,19,0,0,0)
    private val existingAlarmDateTime = DateTime(2022,11,28,18,20,0,0)
    private val expectedScheduledAlarms = 1
    private val expectedScheduledAlarmId = 2
    private val expectedScheduledAlarmTriggerTime = existingAlarmDateTime.millis


    init {
        // Set current time
        DateTimeUtils.setCurrentMillisFixed(currentDateTime.millis)

        // Mock static ringtone manager
        mockkStatic(RingtoneManager::class)
        val defaultRingtone = mockk<Ringtone>()
        val defaultRingtonePath = "ringtone1_uri/1"
        val defaultRingtoneTitle = "ringtone1_title"

        every { RingtoneManager.getActualDefaultRingtoneUri(any(),any()) } returns Uri.parse(defaultRingtonePath)
        every { RingtoneManager.getRingtone(any(),any()) } returns defaultRingtone
        every { defaultRingtone.getTitle(any()) } returns defaultRingtoneTitle

        // Config fake ringtone manager
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title","ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursorValues(ringtoneValues)
    }

    @Given("^app has existing scheduled alarm$")
    fun app_has_existing_scheduled_alarm() {
        // Add existing alarm to db
        val existingAlarm = AlarmEntity(
            "name_1",
            existingAlarmDateTime.hourOfDay,
            existingAlarmDateTime.minuteOfHour,
            emptySet(),
            true,
            Sound.AlarmSound("sound_1"),
            false,
            5,
            1,
            5,
            false
        )

        db.alarmDao().insert(existingAlarm).blockingGet()

        // Add existing alarm to alarm manager
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmPendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(KEY_ALARM_ID,1)
            PendingIntent.getBroadcast(context, 1, intent, 0)
        }
        val scheduledAlarm = ShadowAlarmManager.ScheduledAlarm(
            AlarmManager.RTC_WAKEUP,
            existingAlarmDateTime.millis,
            alarmPendingIntent,
            null
        )

        shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
    }

    @When("^user schedule new alarm with trigger time equal to existing one$")
    fun user_schedule_new_alarm_with_trigger_time_equal_to_existing_one() {
        // Setup test nav controller
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.alarms_graph)
        navController.setCurrentDestination(R.id.alarmEditorFragment)

        // Launch editor fragment
        scenario = launchFragmentInHiltContainer<AlarmEditorFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Schedule new alarm with same trigger time as existing alarm
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withContentDescription("${existingAlarmDateTime.hourOfDay} o'clock"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withContentDescription("${existingAlarmDateTime.minuteOfHour} minutes"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText("OK"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withId(R.id.fab))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should remove existing alarm,and add new one$")
    fun app_should_remove_existing_alarm_and_add_new_one() {
        // Verify db removed initial existing alarm
        val dbAlarms = db.alarmDao().getAll().blockingGet()

        assertThat(dbAlarms.size).isEqualTo(expectedScheduledAlarms)
        assertThat(dbAlarms[0].id).isEqualTo(expectedScheduledAlarmId)

        // Verify initial existing alam was canceled by alarm manager,and new alarm scheduled
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(expectedScheduledAlarms)

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm

        assertThat(Shadows.shadowOf(scheduledAlarm.operation)
            .savedIntent.getIntExtra(KEY_ALARM_ID,-1)).isEqualTo(expectedScheduledAlarmId)
        assertThat(scheduledAlarm.triggerAtTime).isEqualTo(expectedScheduledAlarmTriggerTime)
    }
}