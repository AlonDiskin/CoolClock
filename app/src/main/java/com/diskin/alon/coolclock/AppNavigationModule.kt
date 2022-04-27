package com.diskin.alon.coolclock

import com.diskin.alon.coolclock.home.presentation.AppGraphProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppNavigationModule {

    @Binds
    @Singleton
    abstract fun bindAppGraphProvider(graphProvider: AppGraphProviderImpl): AppGraphProvider
}