package com.diskin.alon.coolclock.timer.presentation

import android.app.Application
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.timer.presentation.infrastructure.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.infrastructure.TimerService
import com.diskin.alon.coolclock.timer.presentation.model.*
import com.diskin.alon.coolclock.timer.presentation.viewmodel.TimerViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerViewModelTest {

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: TimerViewModel

    // Collaborators
    private val app: Application = mockk()
    private val eventBus: EventBus = mockk()

    @Before
    fun setUp() {
        // Stub mock
        every { eventBus.register(any()) } returns Unit

        viewModel = TimerViewModel(app,eventBus)
    }

    @Test
    fun registerToEventBUs_WhenCreated() {

        // Then
        verify { eventBus.register(viewModel) }
    }

    @Test
    fun startTimer_WhenTimerStarted() {
        // Given
        val duration = 1000L
        val intentSlot = slot<Intent>()

        every { app.startService(capture(intentSlot)) } returns mockk()
        every { app.packageName } returns "package_name"

        // When
        viewModel.startTimer(duration)

        // Then
        assertThat(intentSlot.captured.extras!!.containsKey(KEY_TIMER_DURATION))
        assertThat(intentSlot.captured.extras!!.get(KEY_TIMER_DURATION)).isEqualTo(duration)
        assertThat(intentSlot.captured.component!!.className).isEqualTo(TimerService::class.java.name)
    }

    @Test
    fun cancelTimer_WhenTimerCanceled() {
        // Given
        every { eventBus.post(any()) } returns Unit

        // When
        viewModel.cancelTimer()

        // Then
        verify { eventBus.post(TimerControl.CANCEL) }
    }

    @Test
    fun pauseTimer_WhenTimerPaused() {
        // Given
        every { eventBus.post(any()) } returns Unit

        // When
        viewModel.pauseTimer()

        // Then
        verify { eventBus.post(TimerControl.PAUSE) }
    }

    @Test
    fun resumeTimer_WhenTimerResumed() {
        // Given
        every { eventBus.post(any()) } returns Unit

        // When
        viewModel.resumeTimer()

        // Then
        verify { eventBus.post(TimerControl.RESUME) }
    }

    @Test
    fun updateTimerViewState_TimerUpdates() {
        // Given
        val update = UiTimer(10,0,0,1200,UiTimerState.RUNNING)

        // When
        viewModel.onTimerUpdateEvent(update)

        // Then
        assertThat(viewModel.timer.value).isEqualTo(update)
    }

    @Test
    fun updateTimerProgressViewState_TimerProgressUpdates() {
        // Given
        val update = UiTimerProgress(2000,1500)

        // When
        viewModel.onTimerProgressUpdateEvent(update)

        // Then
        assertThat(viewModel.progress.value).isEqualTo(update)
    }

    @Test
    fun showTimerNotification() {
        // Given
        every { eventBus.post(any()) } returns Unit

        // When
        viewModel.showTimerNotification()

        // Then
        verify { eventBus.post(NotificationRequest.SHOW) }
    }

    @Test
    fun hideTimerNotification() {
        // Given
        every { eventBus.post(any()) } returns Unit

        // When
        viewModel.hideTimerNotification()

        // Then
        verify { eventBus.post(NotificationRequest.HIDE) }
    }
}