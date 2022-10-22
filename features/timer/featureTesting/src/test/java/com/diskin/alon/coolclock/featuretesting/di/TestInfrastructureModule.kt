package com.diskin.alon.coolclock.featuretesting.di

import com.diskin.alon.coolclock.timer.presentation.infrastructure.TimerAlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestInfrastructureModule {

    @Singleton
    @Provides
    fun provideTimerAlarmManager(): TimerAlarmManager {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }
}