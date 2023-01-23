package com.diskin.alon.coolclock.alarms.device

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.application.interfaces.NO_CURRENT_ALARM
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.presentation.model.AlarmStoppedEvent
import com.diskin.alon.coolclock.common.application.AppResult
import com.google.common.truth.Truth.*
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(instrumentedPackages = ["androidx.loader.content"],application = HiltTestApplication::class)
class AlarmExecutorImplTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var executor: AlarmExecutorImpl

    // Collaborators
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val sharedPreferences: SharedPreferences = mockk()
    private val eventBus: EventBus = mockk()

    @Before
    fun setUp() {
        executor = AlarmExecutorImpl(appContext,sharedPreferences,eventBus)
    }

    @Test
    fun startAlarmService_WhenAlarmStarted() {
        // Given
        val alarm = Alarm(
            1,
            "alarm_1",
            16,
            46,
            emptySet(),
            true,
            true,
            Sound.AlarmSound("sound_1"),
            1,
            5,
            0,
            false
        )
        val deviceAlarm = DeviceAlarm(
            alarm.id,
            alarm.isVibrate,
            "sound_1",
            alarm.duration,
            alarm.volume,
            false,
            alarm.name
        )
        val shadowApp = Shadows.shadowOf(appContext as Application)
        val editor: SharedPreferences.Editor = mockk()

        every { sharedPreferences.edit() } returns editor
        every { editor.putInt(KEY_CURRENT_ALARM_ID,alarm.id) } returns editor
        every { editor.apply() } returns Unit

        // When
        val observer = executor.startAlarm(alarm).test()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val actualServiceIntent = shadowApp.nextStartedService

        assertThat(actualServiceIntent.component!!.className).isEqualTo(AlarmService::class.java.name)
        assertThat(actualServiceIntent.getSerializableExtra(ALARM_REQUEST,DeviceAlarm::class.java))
            .isEqualTo(deviceAlarm)
        verify(exactly = 1) { editor.putInt(KEY_CURRENT_ALARM_ID,alarm.id) }
        verify(exactly = 1) { editor.apply() }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun stopAlarmService_WhenAlarmStopped() {
        // Given
        val shadowApp = Shadows.shadowOf(appContext as Application)
        val editor: SharedPreferences.Editor = mockk()

        every { sharedPreferences.edit() } returns editor
        every { editor.putInt(KEY_CURRENT_ALARM_ID, NO_CURRENT_ALARM) } returns editor
        every { editor.apply() } returns Unit
        every { eventBus.post(any()) } returns Unit

        // When
        val observer = executor.stopAlarm().test()

        // Then
        val actualServiceIntent = shadowApp.nextStoppedService

        assertThat(actualServiceIntent.component!!.className).isEqualTo(AlarmService::class.java.name)
        verify(exactly = 1) { editor.putInt(KEY_CURRENT_ALARM_ID, NO_CURRENT_ALARM) }
        verify(exactly = 1) { editor.apply() }
        verify(exactly = 1) { eventBus.post(AlarmStoppedEvent) }
        observer.assertValue(AppResult.Success(Unit))
    }
}