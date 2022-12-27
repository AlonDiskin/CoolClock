package com.diskin.alon.coolclock.alarms.featuretesting.di

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppDeviceModule {

    @Singleton
    @Provides
    fun provideAlarmManager(): AlarmManager {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Singleton
    @Provides
    fun provideAudioManager(): AudioManager {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideRingtoneManager(): RingtoneManager {
        return RingtoneManager(ApplicationProvider.getApplicationContext<Context>()
            .applicationContext)
    }

    @Singleton
    @Provides
    fun provideNotificationManagerCompat(app: Application): NotificationManagerCompat {
        return NotificationManagerCompat.from(app)
    }

    @Singleton
    @Provides
    fun provideVibrator(app: Application): Vibrator {
        return app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}