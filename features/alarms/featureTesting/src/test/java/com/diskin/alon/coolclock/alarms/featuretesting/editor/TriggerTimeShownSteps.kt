package com.diskin.alon.coolclock.alarms.featuretesting.editor

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmEditorFragment
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.KEY_ALARM_ID_ARG
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
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
import org.robolectric.shadows.ShadowToast

class TriggerTimeShownSteps(
    private val db: TestDatabase,
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val currentTime = DateTime(2022,11,30,11,0,0,0)
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
    private lateinit var expectedNotificationMessage: String
    private lateinit var expectedNextAlarmLabel: String

    init {
        // Set fixed current time
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)

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

    @Given("^use \"([^\"]*)\" scheduled alarm$")
    fun use_something_scheduled_alarm(hasAlarm: String) {
        when(hasAlarm) {
            "yes" -> {
                // Set existing alarm in db
                expectedNotificationMessage = "Alarm set for 5 hours,15 minutes from now"
                selectedHour = 16
                selectedMinute = 15
                expectedNextAlarmLabel = "Today"
                val insertAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                        ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                        "VALUES ('name_1',15,10,'empty',1,'ringtone1_uri/1',0,5,5,0,0,1)"

                db.compileStatement(insertAlarm).executeInsert()
            }

            "no" -> {
                expectedNotificationMessage = "Alarm set for 23 hours,15 minutes from now"
                selectedHour = 10
                selectedMinute = 15
                expectedNextAlarmLabel = "Tomorrow"
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$hasAlarm")
        }
    }

    @When("^user \"([^\"]*)\" an alarm$")
    fun user_something_an_alarm(editAction: String) {
        var flag = false
        // Setup test nav controller
        navController.setGraph(R.navigation.alarms_graph)
        navController.setCurrentDestination(R.id.alarms)

        val bundle = when(editAction) {
            "create" -> null
            "update" -> bundleOf(KEY_ALARM_ID_ARG to 1)
            else -> throw IllegalArgumentException("Unknown scenario arg:$editAction")
        }

        // Launch alarms browser fragment
        scenario = launchFragmentInHiltContainer<AlarmEditorFragment>(fragmentArgs = bundle)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set selected trigger time for edited alarm
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("$selectedHour o'clock"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("$selectedMinute minutes"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(R.id.fab))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should display to user the time left to alarm trigger$")
    fun app_should_display_to_user_the_time_left_to_alarm_trigger() {
        assertThat(ShadowToast.getTextOfLatestToast())
            .isEqualTo(expectedNotificationMessage)
    }

    @And("^redirect user to alarms browser$")
    fun redirect_user_to_alarms_browser() {
        // Verify app navigates to alarms fragment
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.alarmsFragment)
    }
}