package com.diskin.alon.coolclock.timer.di

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimerInfrastructureModule {

    @Singleton
    @Provides
    fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }

    @Singleton
    @Provides
    fun provideNotificationManagerCompat(app: Application): NotificationManagerCompat {
        return NotificationManagerCompat.from(app)
    }
}