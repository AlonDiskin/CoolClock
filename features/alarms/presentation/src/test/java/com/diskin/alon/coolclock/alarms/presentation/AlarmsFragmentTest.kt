package com.diskin.alon.coolclock.alarms.presentation

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.paging.PagingData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmsAdapter.AlarmViewHolder
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmsFragment
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsViewModel
import com.diskin.alon.coolclock.common.uitesting.*
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class AlarmsFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: AlarmsViewModel = mockk()

    // Stub data
    private val alarms = MutableLiveData<PagingData<UiAlarm>>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        every { viewModel.alarms } returns alarms

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<AlarmsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showAllAlarms_WhenResumed() {
        // Given
        val alarmsData = createUiAlarms()

        // When
        alarms.value = PagingData.from(alarmsData)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.alarms))
            .check(matches(isRecyclerViewItemsCount(alarmsData.size)))

        alarmsData.forEachIndexed { index, uiAlarm ->
            onView(withId(R.id.alarms))
                .perform(scrollToPosition<AlarmViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.alarm_name))
                .check(matches(withText(uiAlarm.name)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.next_alarm))
                .check(matches(withText(uiAlarm.nextAlarm)))

            val switchStateMatcher = when(uiAlarm.isActive) {
                true -> isChecked()
                false -> not(isChecked())
            }

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.active_switcher))
                .check(matches(switchStateMatcher))

            val repeatedDayColorId = com.google.android.material.R.color.design_default_color_primary
            val unRepeatedDatColorId = com.google.android.material.R.color.material_on_background_disabled
            val expectedSundayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.SUN)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }
            val expectedMondayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.MON)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }
            val expectedTuesdayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.TUE)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }
            val expectedWednesdayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.WED)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }
            val expectedThursdayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.THU)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }
            val expectedFridayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.FRI)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }
            val expectedSaturdayLabelTextColorId = if (uiAlarm.repeatDays.contains(RepeatDay.SAT)) {
                repeatedDayColorId
            } else {
                unRepeatedDatColorId
            }

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.sunday_label))
                .check(matches(withTextViewTextColor(expectedSundayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.monday_label))
                .check(matches(withTextViewTextColor(expectedMondayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.tuesday_label))
                .check(matches(withTextViewTextColor(expectedTuesdayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.wednesday_label))
                .check(matches(withTextViewTextColor(expectedWednesdayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.thursday_label))
                .check(matches(withTextViewTextColor(expectedThursdayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.friday_label))
                .check(matches(withTextViewTextColor(expectedFridayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.saturday_label))
                .check(matches(withTextViewTextColor(expectedSaturdayLabelTextColorId)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.active_switcher))
                .check(matches(withSwitchText(uiAlarm.time)))
        }
    }
}