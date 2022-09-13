package com.diskin.alon.coolclock.di

import android.app.Application
import androidx.room.Room
import com.diskin.alon.coolclock.alarms.data.local.AlarmDao
import com.diskin.alon.coolclock.worldclocks.data.CityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppTestDataModule {

    @Singleton
    @Provides
    fun provideDatabase(app: Application): AppTestDatabase {
        return Room.inMemoryDatabaseBuilder(app, AppTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideCityDao(database: AppTestDatabase): CityDao {
        return database.cityDao()
    }

    @Singleton
    @Provides
    fun provideAlarmsDao(database: AppTestDatabase): AlarmDao {
        return database.alarmDao()
    }
}