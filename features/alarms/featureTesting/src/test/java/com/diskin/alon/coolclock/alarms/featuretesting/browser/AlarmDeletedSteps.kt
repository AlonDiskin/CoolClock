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
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.ui.AlarmsFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.*
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager

class AlarmDeletedSteps(
    private val db: TestDatabase,
    alarmManager: AlarmManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val shadowAlarmManager = Shadows.shadowOf(alarmManager)
    private val currentTime = DateTime(2022,9,12,19,0)
    private val nextAlarm = DateTime(2022,9,13,12,10)

    init {
        // Set test time
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)

        // Stub fake alarm manager
        val context = ApplicationProvider.getApplicationContext<Context>()
        val weekMillisInterval = 1000L * 60 * 60 * 24 * 7
        val alarmPendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(KEY_ALARM_ID,1)
            PendingIntent.getBroadcast(context, 1, intent, 0)
        }
        val scheduledAlarm = ShadowAlarmManager.ScheduledAlarm(
            AlarmManager.RTC_WAKEUP,
            nextAlarm.millis,
            weekMillisInterval,
            alarmPendingIntent,
            null
        )

        shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)

        // Pre populate test db with active alarm
        db.alarmDao()
            .insert(
                AlarmEntity(
                    "name_1",
                    12,
                    10,
                    emptySet(),
                    true,
                    Sound.AlarmSound("sound_1"),
                    false,
                    10,
                    1,
                    5,
                    false
                )
            ).blockingGet()
    }

    @Given("^user opened alarms browser$")
    fun user_opened_alarms_browser() {
        // Launch alarms browser fragment
        scenario = launchFragmentInHiltContainer<AlarmsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
    }

    @When("^he select to delete first listed active alarm$")
    fun he_select_to_delete_first_listed_active_alarm() {
        // Select to delete listed alarm
        onView(withRecyclerView(R.id.alarms).atPositionOnView(0, R.id.options_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.title_action_delete))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should delete its record$")
    fun app_should_delete_its_record(){
        // Verify db is empty
        val actualSize = db.compileStatement("SELECT COUNT(*) FROM user_alarms").simpleQueryForLong()

        assertThat(actualSize).isEqualTo(0)
    }

    @And("^cancel its scheduled alarm$")
    fun cancel_its_scheduled_alarm() {
        // Verify alarm manager canceled active alarm
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(0)
    }
}