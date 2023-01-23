package com.diskin.alon.coolclock.featuretesting.di

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import com.diskin.alon.coolclock.timer.presentation.device.TimerAlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestDeviceModule {

    @Singleton
    @Provides
    fun provideTimerAlarmManager(): TimerAlarmManager {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideNotificationManagerCompat(app: Application): NotificationManagerCompat {
        return NotificationManagerCompat.from(app)
    }

    @Singleton
    @Provides
    fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }
}