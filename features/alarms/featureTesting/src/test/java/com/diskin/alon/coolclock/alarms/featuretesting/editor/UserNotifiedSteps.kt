package com.diskin.alon.coolclock.alarms.featuretesting.editor

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
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmEditorFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth
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
import org.robolectric.shadows.ShadowToast

class UserNotifiedSteps(
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private val scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val currentTime = DateTime(2022,11,30,11,0,0,0)
    private val selectedAlarmTime = DateTime(2022,11,30,16,15,0,0)
    private val expectedNotificationMessage = "Alarm set for 5 hours,15 minutes from now"

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
            .setCursor(ringtoneValues)

        // Setup test nav controller
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
    }

    @Given("^user edit new alarm$")
    fun user_edit_new_alarm() {
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("${selectedAlarmTime.hourOfDay} o'clock"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("${selectedAlarmTime.minuteOfHour} minutes"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText("OK"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he confirm alarm schedule$")
    fun he_confirm_alarm_schedule() {
        onView(withId(R.id.fab))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should notify user about time left to alarm trigger$")
    fun app_should_notify_user_about_time_left_to_alarm_trigger() {
        Truth.assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedNotificationMessage)
    }
}