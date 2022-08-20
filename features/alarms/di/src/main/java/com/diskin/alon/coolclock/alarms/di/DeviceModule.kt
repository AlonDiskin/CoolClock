package com.diskin.alon.coolclock.alarms.di

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.device.AlarmsSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class DeviceModule {

    @ActivityRetainedScoped
    @Binds
    abstract fun bindAlarmsScheduler(scheduler: AlarmsSchedulerImpl): AlarmsScheduler
}