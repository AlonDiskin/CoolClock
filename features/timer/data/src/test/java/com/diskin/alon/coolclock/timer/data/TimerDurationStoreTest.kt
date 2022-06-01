package com.diskin.alon.coolclock.timer.data

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TimerDurationStoreTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: TimerDurationStore

    // Collaborators
    private val sharedPreferences: SharedPreferences = mockk()

    @Before
    fun setUp() {
        store = TimerDurationStore(sharedPreferences)
    }

    @Test
    fun getLastTimerDurationFromStorage_WhenQueried() {
        // Given
        val seconds = 10
        val minutes = 1
        val hours = 0
        val timerDuration = TimerDuration(seconds, minutes, hours)

        every { sharedPreferences.getInt(KEY_SECONDS,any()) } returns seconds
        every { sharedPreferences.getInt(KEY_MINUTES,any()) } returns minutes
        every { sharedPreferences.getInt(KEY_HOURS,any()) } returns hours

        // When
        val observer = store.getLast().test()

        // Then
        observer.assertValue(timerDuration)
    }

    @Test
    fun saveTimerDuration() {
        // Given
        val seconds = 10
        val minutes = 1
        val hours = 0
        val timerDuration = TimerDuration(seconds, minutes, hours)
        val editor = mockk<SharedPreferences.Editor>()

        every { sharedPreferences.edit() } returns editor
        every { editor.putInt(KEY_SECONDS,any()) } returns editor
        every { editor.putInt(KEY_MINUTES,any()) } returns editor
        every { editor.putInt(KEY_HOURS,any()) } returns editor
        every { editor.apply() } returns Unit

        // When
        store.save(timerDuration)

        // Then
        verify { editor.putInt(KEY_SECONDS,timerDuration.seconds) }
        verify { editor.putInt(KEY_MINUTES,timerDuration.minutes) }
        verify { editor.putInt(KEY_SECONDS,timerDuration.seconds) }
    }
}