package com.diskin.alon.coolclock.timer.presentation

import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.ProgressBar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.timer.presentation.controller.TimerFragment
import com.diskin.alon.coolclock.timer.presentation.model.UiTimer
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerDuration
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerProgress
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerState
import com.diskin.alon.coolclock.timer.presentation.viewmodel.TimerViewModel
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class TimerFragmentTest {

    // System under test
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: TimerViewModel = mockk()

    // Stub data
    private val timer = MutableLiveData<UiTimer>()
    private val timerDuration = MutableLiveData<UiTimerDuration>()
    private val progress = MutableLiveData<UiTimerProgress>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.startTimer(any()) } returns Unit
        every { viewModel.resumeTimer() } returns Unit
        every { viewModel.pauseTimer() } returns Unit
        every { viewModel.cancelTimer() } returns Unit
        every { viewModel.timer } returns timer
        every { viewModel.progress } returns progress
        every { viewModel.showTimerNotification() } returns Unit
        every { viewModel.hideTimerNotification() } returns Unit
        every { viewModel.timerDuration } returns timerDuration

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<TimerFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun startTimer_WhenUserSelectToStartTimer() {
        // Given
        val selectedDuration = 30000L
        timer.value = UiTimer(0,0,0,0,UiTimerState.NOT_SET)

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val hoursPicker = fragment.view!!.findViewById<NumberPicker>(R.id.hours_picker)
            val minutesPicker = fragment.view!!.findViewById<NumberPicker>(R.id.minutes_picker)
            val secondsPicker = fragment.view!!.findViewById<NumberPicker>(R.id.seconds_picker)

            hoursPicker.value = 0
            minutesPicker.value = 0
            secondsPicker.value = selectedDuration.toInt() / 1000
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]

            fragment.view!!.findViewById<Button>(R.id.buttonStartCancel).performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.startTimer(selectedDuration) }
    }

    @Test
    fun cancelTimer_WhenUserSelectToCancelTimer() {
        // Given
        timer.value = UiTimer(20,0,0,0,UiTimerState.START)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]

            fragment.view!!.findViewById<Button>(R.id.buttonStartCancel).performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.cancelTimer() }
    }

    @Test
    fun pauseTimer_WhenUserSelectToPauseTimer() {
        // Given
        timer.value = UiTimer(20,0,0,0,UiTimerState.RUNNING)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]

            fragment.view!!.findViewById<Button>(R.id.buttonPauseResume).performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.pauseTimer() }
    }

    @Test
    fun resumeTimer_WhenUserSelectToResumeTimer() {
        // Given
        timer.value = UiTimer(20,0,0,0,UiTimerState.PAUSED)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]

            fragment.view!!.findViewById<Button>(R.id.buttonPauseResume).performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.resumeTimer() }
    }

    @Test
    fun showTimerPicker_WhenTimerIsNotActive() {
        // Given
        timer.value = UiTimer(0,0,0,0,UiTimerState.NOT_SET)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.hours_picker))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.minutes_picker))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.seconds_picker))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun hideTimerPicker_WhenTimerIsActive() {
        // Given
        timer.value = UiTimer(0,0,0,0,UiTimerState.NOT_SET)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        timer.value = UiTimer(10,0,0,0,UiTimerState.START)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
            val endState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).endState

            assertThat(state).isEqualTo(endState)
        }
    }

    @Test
    fun showTimerCountdown_WhenTimerIsActive() {
        // Given
        timer.value = UiTimer(0,0,0,0,UiTimerState.NOT_SET)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        timer.value = UiTimer(10,0,0,0,UiTimerState.START)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
            val endState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).endState

            assertThat(state).isEqualTo(endState)
        }
    }

    @Test
    fun disableTimerStartButton_WhenSelectedTimeDurationIsZero() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val hoursPicker = fragment.view!!.findViewById<NumberPicker>(R.id.hours_picker)
            val minutesPicker = fragment.view!!.findViewById<NumberPicker>(R.id.minutes_picker)
            val secondsPicker = fragment.view!!.findViewById<NumberPicker>(R.id.seconds_picker)

            hoursPicker.value = 0
            minutesPicker.value = 0
            secondsPicker.value = 0
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.buttonStartCancel))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun enableTimerStartButton_WhenSelectedTimeDurationGraterThenZero() {
        // Given

        // When
        scenario.onActivity {
            val secondsPicker = it.findViewById<NumberPicker>(R.id.seconds_picker)

            secondsPicker.value = 10
            Shadows.shadowOf(secondsPicker).onValueChangeListener.onValueChange(null,0,0)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.buttonStartCancel))
            .check(matches(isEnabled()))
    }

    @Test
    fun showTimerProgress_WhenTimerIsActive() {
        // Given
        val timerProgress = UiTimerProgress(2000,1500)

        // When
        timer.value = UiTimer(10,0,0,0,UiTimerState.START)
        progress.value = timerProgress
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val state = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).currentState
            val endState = fragment.view!!.findViewById<MotionLayout>(R.id.motionLayout).endState
            val progressBar = fragment.view!!.findViewById<ProgressBar>(R.id.progressBar)

            assertThat(state).isEqualTo(endState)
            assertThat(progressBar.progress).isEqualTo(timerProgress.progress)
        }
    }

    @Test
    fun showTimerNotification_WhenStopped() {
        // Given

        // When
        scenario.moveToState(Lifecycle.State.DESTROYED)

        // Then
        verify { viewModel.showTimerNotification() }
    }

    @Test
    fun hideTimerNotification_WhenStarted() {
        // Given

        // Then
        verify { viewModel.hideTimerNotification() }
    }

    @Test
    fun showTimerLastPickedDuration_WhenLoaded() {
        // Given
        val uiTimerDuration = UiTimerDuration(15,1,0)
        timer.value = UiTimer(0,0,0,0,UiTimerState.NOT_SET)

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        timerDuration.value = uiTimerDuration
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.findViewById<NumberPicker>(R.id.seconds_picker).visibility).isEqualTo(View.VISIBLE)
            assertThat(it.findViewById<NumberPicker>(R.id.minutes_picker).visibility).isEqualTo(View.VISIBLE)
            assertThat(it.findViewById<NumberPicker>(R.id.hours_picker).visibility).isEqualTo(View.VISIBLE)

            assertThat(it.findViewById<NumberPicker>(R.id.seconds_picker).value).isEqualTo(uiTimerDuration.seconds)
            assertThat(it.findViewById<NumberPicker>(R.id.minutes_picker).value).isEqualTo(uiTimerDuration.minutes)
            assertThat(it.findViewById<NumberPicker>(R.id.hours_picker).value).isEqualTo(uiTimerDuration.hours)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun saveTimerPickDuration_WhenStopped() {
        // Given
        val uiTimerDuration = UiTimerDuration(15,1,0)

        scenario.onActivity {
            it.findViewById<NumberPicker>(R.id.seconds_picker).value = uiTimerDuration.seconds
            it.findViewById<NumberPicker>(R.id.minutes_picker).value = uiTimerDuration.minutes
            it.findViewById<NumberPicker>(R.id.hours_picker).value = uiTimerDuration.hours
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.moveToState(Lifecycle.State.DESTROYED)

        // Then
        assertThat(viewModel.timerDuration.value).isEqualTo(uiTimerDuration)
    }
}