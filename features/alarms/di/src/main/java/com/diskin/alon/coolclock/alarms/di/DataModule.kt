package com.diskin.alon.coolclock.alarms.di

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.data.implementation.AlarmsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class DataModule {

    @ActivityRetainedScoped
    @Binds
    abstract fun bindAlarmRepository(repositoryImpl: AlarmsRepositoryImpl): AlarmsRepository
}