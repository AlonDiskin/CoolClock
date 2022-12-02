package com.diskin.alon.coolclock.alarms.presentation

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.paging.PagingData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import com.diskin.alon.coolclock.common.uitesting.*
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class AlarmsFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: AlarmsViewModel = mockk()
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val alarms = MutableLiveData<PagingData<UiAlarm>>()
    private val latestScheduledAlarm = SingleLiveEvent<String>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        every { viewModel.alarms } returns alarms
        every { viewModel.latestScheduledAlarm } returns latestScheduledAlarm

        // Setup test nav controller
        navController.setGraph(R.navigation.alarms_graph)
        navController.setCurrentDestination(R.id.alarmsFragment)

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<AlarmsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
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

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.alarmTime))
                .check(matches(withText(uiAlarm.time)))

            onView(withRecyclerView(R.id.alarms).atPositionOnView(index, R.id.active_switcher))
                .check(matches(withSwitchChecked(uiAlarm.isActive)))
        }
    }

    @Test
    fun activateAlarm_WhenUserSwitchOnUnActivatedAlarm() {
        // Given
        val alarm = createUnActiveAlarm()

        every { viewModel.changeAlarmActivation(any(),any()) } returns Unit

        alarms.value = PagingData.from(listOf(alarm))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.active_switcher))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.changeAlarmActivation(alarm.id,true) }
    }

    @Test
    fun deActivateAlarm_WhenUserSwitchOfActivatedAlarm() {
        // Given
        val alarm = createActiveAlarm()

        every { viewModel.changeAlarmActivation(any(),any()) } returns Unit

        alarms.value = PagingData.from(listOf(alarm))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.active_switcher))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.changeAlarmActivation(alarm.id,false) }
    }

    @Test
    fun notifyOfLatestScheduledAlarm_WhenActivatedAlarmScheduled() {
        // Given
        val date = "date"

        // When
        latestScheduledAlarm.value = date
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val toastMessage = ShadowToast.getTextOfLatestToast()

        assertThat(toastMessage).isEqualTo(date)
    }

    @Test
    fun deleteAlarm_WhenUserSelectToDeleteIt() {
        // Given
        val alarmsData = createUiAlarms()
        alarms.value = PagingData.from(alarmsData)

        every { viewModel.deleteAlarm(any()) } returns Unit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withRecyclerView(R.id.alarms).atPositionOnView(0, R.id.options_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.title_action_delete))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.deleteAlarm(alarmsData.first().id) }
    }

    @Test
    fun openAlarmEditor_WhenAddNewAlarmClicked() {
        // Given

        // When
        onView(withId(R.id.action_add_alarm))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.alarmEditorFragment)
    }
}