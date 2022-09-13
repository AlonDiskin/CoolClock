package com.diskin.alon.coolclock.alarms.featuretesting.browser

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.ALARM_ID
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmsFragment
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

class AlarmDeletedSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val intentSlot = slot<Intent>()
    private val requestCodeSlot = slot<Int>()
    private val pendingIntent = mockk<PendingIntent>()
    private val currentTime = DateTime(2022,9,12,19,0)

    init {
        // Set test time and stub mocks
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)
        mockkStatic(PendingIntent::class)
        every { alarmManager.cancel(any<PendingIntent>()) } returns Unit
        every { PendingIntent.getBroadcast(any(),capture(requestCodeSlot),capture(intentSlot),any()) } returns pendingIntent
        every { pendingIntent.cancel() } returns Unit

        // Pre populate test db with active alarm
        db.alarmDao()
            .insert(
                AlarmEntity(
                    "name_1",
                    12,
                    10,
                    setOf(WeekDay.SUN, WeekDay.MON),
                    true,
                    "sound_1",
                    false,
                    true,
                    1,
                    5,
                    false,
                    5,
                    1
                )
            ).blockingAwait()
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
        val actualSize = db.compileStatement("SELECT COUNT(*) FROM alarms").simpleQueryForLong()

        assertThat(actualSize).isEqualTo(0)
    }

    @And("^cancel its scheduled alarm$")
    fun cancel_its_scheduled_alarm() {
        // Verify alarm manager canceled active alarm
        verify(exactly = 1) { alarmManager.cancel(pendingIntent) }
        assertThat(requestCodeSlot.captured).isEqualTo(1)
        assertThat(intentSlot.captured.action).isEqualTo(ACTION_ALARM)
        assertThat(intentSlot.captured.getIntExtra(ALARM_ID,-1)).isEqualTo(1)
    }
}