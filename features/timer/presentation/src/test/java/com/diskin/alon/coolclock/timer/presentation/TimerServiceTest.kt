package com.diskin.alon.coolclock.timer.presentation

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.timer.presentation.model.TimerControl
import com.diskin.alon.coolclock.timer.presentation.model.UiTimer
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerState
import com.diskin.alon.coolclock.timer.presentation.util.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.util.TimerService
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"],application = HiltTestApplication::class)
class TimerServiceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var service: TimerService

    // Collaborators
    @BindValue
    @JvmField
    val eventBus: EventBus = mockk()

    @Before
    fun setUp() {
        // Stub collaborators
        every { eventBus.postSticky(any()) } returns Unit
        every { eventBus.register(any()) } returns Unit
        every { eventBus.unregister(any()) } returns Unit

        // Start service under test
        service = Robolectric.setupService(TimerService::class.java)
    }

    @Test
    fun registerToEventBus_WhenCreated() {
        // Given

        // When
        service.onCreate()

        // Then
        verify { eventBus.register(service) }
    }

    @Test
    fun unregisterFromEventBus_WhenDestroyed() {
        // Given

        // When
        service.onDestroy()

        // Then
        verify { eventBus.unregister(service) }
    }

    @Test
    fun startTimer_WhenServiceStarted() {
        // Given
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        // When
        service.onStartCommand(intent,0,0)

        // Then
        assertThat(service.isTimerRunning).isTrue()
    }

    @Test
    fun stopService_WhenTimerCanceled() {
        // Given
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        service.onStartCommand(intent,0,0)

        // When
        service.onTimerControlEvent(TimerControl.CANCEL)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Shadows.shadowOf(service).isStoppedBySelf).isTrue()
    }

    @Test
    fun notifyTimerCountdownObservers_WhenTimerCanceled() {
        // Given
        val slot = slot<UiTimer>()
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        every { eventBus.postSticky(capture(slot)) } returns Unit
        service.onStartCommand(intent,0,0)

        // When
        service.onTimerControlEvent(TimerControl.CANCEL)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(slot.captured.state).isEqualTo(UiTimerState.DONE)
    }

    @Test
    fun stopService_WhenTimerFinish() {
        // Given
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        service.onStartCommand(intent,0,0)

        // When
        service.countDownTimer.onFinish()

        // Then
        assertThat(Shadows.shadowOf(service).isStoppedBySelf).isTrue()
    }

    @Test
    fun cancelTimer_WhenTimerPaused() {
        // Given
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        service.onStartCommand(intent,0,0)

        // When
        service.onTimerControlEvent(TimerControl.PAUSE)

        // Then
        assertThat(service.isTimerRunning).isFalse()
    }

    @Test
    fun notifyTimerObservers_WhenTimerPaused() {
        // Given
        val slot = slot<UiTimer>()
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        every { eventBus.postSticky(capture(slot)) } returns Unit
        service.onStartCommand(intent,0,0)

        // When
        service.onTimerControlEvent(TimerControl.PAUSE)

        // Then
        assertThat(slot.captured.state).isEqualTo(UiTimerState.PAUSED)
    }

    @Test
    fun resumeTimer_WhenTimerResumed() {
        // Given
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        service.onStartCommand(intent,0,0)
        service.onTimerControlEvent(TimerControl.PAUSE)

        // When
        service.onTimerControlEvent(TimerControl.RESUME)

        // Then
        assertThat(service.isTimerRunning).isTrue()
    }

    @Test
    fun notifyTimerObservers_WhenTimerResumed() {
        // Given
        val slot = slot<UiTimer>()
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val duration = 2000L
        val intent = Intent(appContext,TimerService::class.java)
            .apply {
                putExtra(KEY_TIMER_DURATION,duration)
            }

        every { eventBus.postSticky(capture(slot)) } returns Unit

        service.onStartCommand(intent,0,0)
        service.onTimerControlEvent(TimerControl.PAUSE)

        // When
        service.onTimerControlEvent(TimerControl.RESUME)

        // Then
        assertThat(slot.captured.state).isEqualTo(UiTimerState.RESUMED)
    }
}