package com.diskin.alon.coolclock.alarms.featuretesting.browser

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmsAdapter.AlarmViewHolder
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmsFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.common.uitesting.withSwitchText
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import org.hamcrest.CoreMatchers
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows

class AlarmsListedSteps(
    db: TestDatabase
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val expectedUiAlarms = listOf(
        TestUiAlarm(
            "alarm_1",
            "Today",
            true,
            "12:15"
        ),
        TestUiAlarm(
            "alarm_2",
            "Tomorrow",
            true,
            "10:15"
        ),
        TestUiAlarm(
            "alarm_3",
            "Not scheduled",
            false,
            "02:30"
        ),
        TestUiAlarm(
            "alarm_4",
            "Sun, 18 Sep",
            true,
            "15:35"
        )
    )

    private data class TestUiAlarm(val name: String,
                                   val nextAlarm: String,
                                   val isActive: Boolean,
                                   val time: String)

    init {
        // Set current system time
        DateTimeUtils.setCurrentMillisFixed(
            DateTime(2022,9,13,12,0)
                .millis
        )

        // Populate test db
        val alarms = listOf(
            AlarmEntity(
                "alarm_1",
                12,
                15,
                emptySet(),
                true,
                "sound_1",
                false,
                true,
                1,
                5,
                false,
                5,
                1
            ),
            AlarmEntity(
                "alarm_2",
                10,
                15,
                emptySet(),
                true,
                "sound_1",
                false,
                true,
                1,
                5,
                false,
                5,
                1
            ),
            AlarmEntity(
                "alarm_3",
                2,
                30,
                emptySet(),
                false,
                "sound_1",
                false,
                true,
                1,
                5,
                false,
                5,
                1
            ),
            AlarmEntity(
                "alarm_4",
                15,
                35,
                setOf(WeekDay.SUN,WeekDay.MON),
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
        )

        alarms.forEach { db.alarmDao().insert(it).blockingAwait() }
    }

    @Given("^user opened alarms screen$")
    fun user_opened_alarms_screen() {
        scenario = launchFragmentInHiltContainer<AlarmsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
    }

    @Then("^app should show all alarms listing in descending adding order$")
    fun app_should_show_all_alarms_listing_in_descending_adding_order() {
        onView(withId(R.id.alarms))
            .check(matches(isRecyclerViewItemsCount(expectedUiAlarms.size)))

        expectedUiAlarms.reversed().forEachIndexed { index, testUiAlarm ->
            onView(withId(R.id.alarms))
                .perform(scrollToPosition<AlarmViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.alarm_name))
                .check(matches(withText(testUiAlarm.name)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.next_alarm))
                .check(matches(withText(testUiAlarm.nextAlarm)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.active_switcher))
                .check(matches(withSwitchText(testUiAlarm.time)))

            val switchStateMatcher = when(testUiAlarm.isActive) {
                true -> isChecked()
                false -> CoreMatchers.not(isChecked())
            }

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.active_switcher))
                .check(matches(switchStateMatcher))
        }
    }
}