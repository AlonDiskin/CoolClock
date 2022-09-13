package com.diskin.alon.coolclock

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDeviceModule {

    @Singleton
    @Provides
    fun provideAlarmManager(app: Application): AlarmManager {
        return app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
}