package com.diskin.alon.coolclock.alarms.presentation

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Looper
import android.os.PowerManager
import android.view.Display
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.alarms.presentation.model.UiFullScreenAlarm
import com.diskin.alon.coolclock.alarms.presentation.ui.FullScreenAlarmActivity
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import com.google.common.truth.Truth.*
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"],qualifiers = "w411dp-h891dp")
class FullScreenAlarmActivityTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<FullScreenAlarmActivity>

    // Collaborators
    private val viewModel: AlarmViewModel = mockk()

    // Stub data
    private val alarmData = MutableLiveData<UiFullScreenAlarm>()
    private val alarmStopped = SingleLiveEvent<Boolean>()

    @Before
    fun setUp() {
        // Replace view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborator
        every { viewModel.alarmData } returns alarmData
        every { viewModel.alarmStopped } returns alarmStopped

        // Launch activity under test
        scenario = ActivityScenario.launch(FullScreenAlarmActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showAlarmData_WhenResumed() {
        // Given
        val alarm = UiFullScreenAlarm(1,"my_alarm","18:35",true)

        // When
        alarmData.value = alarm
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.alarmTime))
            .check(
                matches(
                    allOf(
                        withEffectiveVisibility(Visibility.VISIBLE),
                        withText(alarm.time)
                    )
                )
            )
        onView(withId(R.id.alarmName))
            .check(
                matches(
                    allOf(
                        withEffectiveVisibility(Visibility.VISIBLE),
                        withText(alarm.name)
                    )
                )
            )
    }

    @Test
    fun hideSnoozeAction_WhenAlarmSnoozeDisabled() {
        // Given
        val alarm = UiFullScreenAlarm(1,"my_alarm","18:35",false)

        // When
        alarmData.value = alarm
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.buttonSnooze))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }

    @Test
    fun snoozeAlarm_WhenUserSelectToSnoozeIt() {
        // Given
        every { viewModel.snooze() } returns Unit

        // When
        onView(withId(R.id.buttonSnooze))
            .perform(swipeRight())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.snooze() }
    }

    @Test
    fun finish_WhenAlarmStopped() {
        // Given

        // When
        alarmStopped.value = true
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    fun dismissAlarm_WhenUserSelectToDismissIt() {
        // Given
        every { viewModel.dismiss() } returns Unit

        // When
        onView(withId(R.id.buttonDismiss))
            .perform(swipeRight())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.dismiss() }
    }

    @Test
    fun hideAppbar_WhenResumed() {
        // Given

        // Then
        scenario.onActivity {
            assertThat(it.actionBar).isNull()
            assertThat(it.supportActionBar).isNull()
        }
    }

    @Test
    fun disableAlarmSnooze_WhenSlidingToDismiss() {
        // Given
        every { viewModel.dismiss() } returns Unit

        // When
        onView(withId(R.id.buttonDismiss))
            .perform(swipeRight())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.buttonSnooze))
            .check(matches(isNotEnabled()))
    }

    @Test
    fun disableAlarmDismiss_WhenSlidingToSnooze() {
        // Given
        every { viewModel.snooze() } returns Unit

        // When
        onView(withId(R.id.buttonSnooze))
            .perform(swipeRight())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.buttonDismiss))
            .check(matches(isNotEnabled()))
    }

    @Test
    fun hideStatusBar_WhenResumed() {
        // TODO
    }

    @Test
    fun wakeScreen_WhenCreatedAndScreenIsTurnedOff() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val ds = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        Shadows.shadowOf(pm).turnScreenOn(false)

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(ds.displays[0].state).isEqualTo(Display.STATE_ON)
    }
}