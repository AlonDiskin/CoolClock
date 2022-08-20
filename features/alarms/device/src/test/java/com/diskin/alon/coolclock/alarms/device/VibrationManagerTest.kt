package com.diskin.alon.coolclock.alarms.device

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class VibrationManagerTest {

    // Test subject
    private lateinit var manager: VibrationManager

    // Collaborators
    private val vibrator: Vibrator = mockk()

    @Before
    fun setUp() {
        manager = VibrationManager(vibrator)
    }

    @Test
    fun vibrateDevice_WhenStarted() {
        // Given

        every { vibrator.vibrate(any<VibrationEffect>()) } returns Unit

        // When
        manager.startDeviceVibration()

        // Then
        verify(exactly = 1) {
            vibrator.vibrate(VibrationEffect.createWaveform(manager.pattern,0))
        }
    }

    @Test
    @Config(sdk = [25])
    fun vibrateDevice_WhenStartedOnApiLowerThanQ() {
        // Given

        every { vibrator.vibrate(any(),any<Int>()) } returns Unit

        // When
        manager.startDeviceVibration()

        // Then
        verify(exactly = 1) { vibrator.vibrate(manager.pattern,0) }
    }
}