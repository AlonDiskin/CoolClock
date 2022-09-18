package com.diskin.alon.coolclock.alarms.featuretesting.di

import android.app.AlarmManager
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
        return mockk()
    }
}