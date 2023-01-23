package com.diskin.alon.coolclock.alarms.presentation

import android.content.Context
import android.os.Looper
import android.widget.EditText
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.presentation.ui.AlarmEditorFragment
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarmEdit
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmEditorViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import com.diskin.alon.coolclock.common.presentation.VolumeButtonPressEvent
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.hamcrest.CoreMatchers.allOf
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
@Config(instrumentedPackages = ["androidx.loader.content"],qualifiers = "w411dp-h891dp")
class AlarmEditorFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: AlarmEditorViewModel = mockk()
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val alarmEdit = MutableLiveData<UiAlarmEdit>()
    private val scheduleAlarmDate = SingleLiveEvent<String>()
    private val volumeButtonPress = SingleLiveEvent<VolumeButtonPressEvent>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub collaborators
        every { viewModel.alarmEdit } returns alarmEdit
        every { viewModel.stopRingtonePlayback() } returns Unit
        every { viewModel.scheduledAlarmDate } returns scheduleAlarmDate
        every { viewModel.volumeButtonPress } returns volumeButtonPress

        // Setup test nav controller
        navController.setGraph(R.navigation.alarms_graph)
        navController.setCurrentDestination(R.id.alarmEditorFragment)

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<AlarmEditorFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun updateVolumeEditAndPlayRingtone_WhenDeviceVolumePressedWhileResumed() {
        // Given
        val edit = createAlarmEdit()
        val pressUp = VolumeButtonPressEvent.VOLUME_UP
        val expectedVolume = edit.volume + 1

        every { viewModel.playRingtoneSample(any()) } returns Unit

        // When
        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        volumeButtonPress.value = pressUp
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val bar = it.findViewById<SeekBar>(R.id.volumeSeekBar)

            assertThat(bar.progress).isEqualTo(expectedVolume)
        }
        assertThat(edit.volume).isEqualTo(expectedVolume)
        verify(exactly = 1) { viewModel.playRingtoneSample(edit.ringtone) }
    }

    @Test
    fun showTitleInAppBar_WhenResumed() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedTitle = context.getString(R.string.title_fragment_alarm_editor)

        // Then
        assertThat(navController.currentDestination?.label).isEqualTo(expectedTitle)
    }

    @Test
    fun showAlarmTimeEdit_WhenResumed() {
        // Given
        val edit = createAlarmEdit()
        val expectedTime = "12:15"

        // When
        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.alarmTime))
            .check(
                matches(
                    allOf(
                        withText(expectedTime),
                        isDisplayed()
                    )
                )
            )

        // When
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withText(R.string.title_dialog_alarm_time_picker))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.material_hour_tv))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText("12")
                    )
                )
            )
        onView(withId(com.google.android.material.R.id.material_minute_tv))
            .inRoot(isDialog())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText("15")
                    )
                )
            )
    }

    @Test
    fun editAlarmTime_WhenUserSelectTime() {
        // Given
        val edit = createAlarmEdit()

        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("14 o'clock"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("15 minutes"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(edit.hour).isEqualTo(14)
        assertThat(edit.minute).isEqualTo(15)
        onView(withId(R.id.alarmTime))
            .check(matches(allOf(isDisplayed(), withText("14:15"))))
    }

    @Test
    fun showRepeatDaysEdit_WhenResumed() {
        // Given
        val edit = createAlarmEdit()

        // When
        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.chipSun))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.title_repeat_day_sun),
                        isChecked()
                    )
                )
            )
        onView(withId(R.id.chipFri))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.title_repeat_day_fri),
                        isChecked()
                    )
                )
            )
    }

    @Test
    fun editRepeatDays_WHenUserSelectDays() {
        // Given
        val edit = createAlarmEdit()
        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.chipWed))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.chipSun))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.title_repeat_day_sun),
                        isChecked()
                    )
                )
            )
        onView(withId(R.id.chipWed))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.title_repeat_day_wed),
                        isChecked()
                    )
                )
            )
        onView(withId(R.id.chipFri))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.title_repeat_day_fri),
                        isChecked()
                    )
                )
            )
        assertThat(edit.repeatDays).isEqualTo(setOf(RepeatDay.SUN, RepeatDay.WED, RepeatDay.FRI))
    }

    @Test
    fun showAlarmName_WhenResumed() {
        // Given
        val edit = createAlarmEdit()

        // When
        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.editTextAlarmName))
            .check(matches(allOf(isDisplayed(), withText(edit.getName()))))
    }

    @Test
    fun editAlarmName_WhenUserSelectName() {
        // Given
        val selectedName = "my alarm"
        val edit = createAlarmEdit()
        alarmEdit.value = edit

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertThat(edit.getName()).isNotEqualTo(selectedName)

        // When
        scenario.onActivity {
            val et = it.findViewById<EditText>(R.id.editTextAlarmName)

            et.setText(selectedName)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.editTextAlarmName))
            .check(matches(allOf(isDisplayed(), withText(selectedName))))
        assertThat(edit.getName()).isEqualTo(selectedName)
    }

    @Test
    fun showRingtoneName_WhenResumed() {
        // Given
        val edit = createAlarmEdit()

        // When
        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val ringtoneEntryIndex = edit.ringtoneValues.indexOf(edit.ringtone)
        val ringtoneName = edit.ringtoneEntries[ringtoneEntryIndex]
        onView(withId(R.id.ringtone))
            .check(matches(allOf(isDisplayed(), withText(ringtoneName))))
        onView(withId(R.id.labelSound))
            .check(matches(allOf(isDisplayed(), withText(R.string.title_label_ringtone))))
    }

    @Test
    fun editRingtone_WhenUserSelect() {
        // Given
        val edit = createAlarmEdit()
        val selectedRingtoneIndex = edit.ringtoneEntries.size - 1
        alarmEdit.value = edit

        every { viewModel.playRingtoneSample(any()) } returns Unit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.layoutSound))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(edit.ringtoneEntries[selectedRingtoneIndex]))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.ringtone))
            .check(matches(allOf(isDisplayed(), withText(edit.ringtoneEntries[selectedRingtoneIndex]))))
        verify { viewModel.playRingtoneSample(edit.ringtoneValues[selectedRingtoneIndex]) }
    }

    @Test
    fun showVibration_WhenResumed() {
        // Given
        val edit = createAlarmEdit()

        // When
        alarmEdit.value = edit

        // Then
        val checkedCheck = if (edit.getVibration()) isChecked() else isNotChecked()
        onView(withId(R.id.switchVibration))
            .check(matches(checkedCheck))
        onView(withId(R.id.labelVibration))
            .check(matches(allOf(isDisplayed(), withText(R.string.title_label_vibration))))
    }

    @Test
    fun editVibration_WhenUserSelect() {
        // Given
        val edit = createAlarmEdit()
        val initialVibration = edit.getVibration()

        alarmEdit.value = edit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.switchVibration))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(edit.getVibration()).isNotEqualTo(initialVibration)
    }

    @Test
    fun showVolume_WhenResumed() {
        // Given
        val edit = createAlarmEdit()

        // When
        alarmEdit.value = edit

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val volumeSeekBar = it.findViewById<SeekBar>(R.id.volumeSeekBar)

            assertThat(volumeSeekBar.progress).isEqualTo(edit.volume)
        }
    }

    @Test
    fun editVolume_WhenSelected() {
        // Given
        val edit = createAlarmEdit()
        alarmEdit.value = edit
        val selectedVolume = edit.volume + 1

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        every { viewModel.playRingtoneSample(any()) } returns Unit

        // When
        scenario.onActivity {
            val volumeSeekBar = it.findViewById<SeekBar>(R.id.volumeSeekBar)

            volumeSeekBar.tag = true
            volumeSeekBar.progress = selectedVolume
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val volumeSeekBar = it.findViewById<SeekBar>(R.id.volumeSeekBar)

            assertThat(volumeSeekBar.progress).isEqualTo(selectedVolume)
        }
        assertThat(edit.volume).isEqualTo(selectedVolume)
        verify { viewModel.playRingtoneSample(edit.ringtone) }
    }

    @Test
    fun scheduleAlarmEdit_WhenUserConfirmEdit() {
        // Given
        val edit = createAlarmEdit()
        alarmEdit.value = edit

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        every { viewModel.schedule() } returns Unit

        // When
        onView(withId(R.id.fab))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.fab))
            .check(matches(isNotEnabled()))
        verify { viewModel.schedule() }
    }

    @Test
    fun showTimeLeftToAlarm_WhenAlarmScheduled() {
        // Given
        val timeToAlarm = "time_left"

        // When
        scheduleAlarmDate.value = timeToAlarm
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(timeToAlarm)
    }

    @Test
    fun navigateBackToAlarmsBrowser_WhenAlarmScheduled() {
        // Given
        val timeToAlarm = "time_left"

        // When
        scheduleAlarmDate.value = timeToAlarm
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(navController.currentDestination!!.id).isEqualTo(R.id.alarmsFragment)
    }
}