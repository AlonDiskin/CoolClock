package com.diskin.alon.coolclock.worldclocks.di

import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.data.CitiesRepositoryImpl
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
    abstract fun bindCityRepository(repositoryImpl: CitiesRepositoryImpl): CitiesRepository
}