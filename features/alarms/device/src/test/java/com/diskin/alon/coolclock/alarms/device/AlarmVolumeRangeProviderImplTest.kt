package com.diskin.alon.coolclock.alarms.device

import android.content.Context
import android.media.AudioManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.application.model.AlarmVolumeRange
import com.diskin.alon.coolclock.common.application.AppResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class AlarmVolumeRangeProviderImplTest {

    // Test subject
    private lateinit var provider: AlarmVolumeRangeProviderImpl

    // Collaborators
    private val appContext = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        provider = AlarmVolumeRangeProviderImpl(appContext)
    }

    @Test
    fun provideDeviceAlarmStreamVolumeRange() {
        // Given
        val deviceMinVolume = 1
        val deviceMaxVolume = (appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val expectedVolumeRange = AlarmVolumeRange(deviceMinVolume,deviceMaxVolume)

        // When
        val observer = provider.get().test()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        observer.assertValue(AppResult.Success(expectedVolumeRange))
    }
}