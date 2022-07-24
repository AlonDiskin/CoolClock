package com.diskin.alon.coolclock

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
object AppDataModule {

    @Singleton
    @Provides
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app,
            AppDatabase::class.java, "coolclock-db")
            .createFromAsset("coolclock.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Singleton
    @Provides
    fun provideCityDao(database: AppDatabase): CityDao {
        return database.cityDao()
    }

    @Singleton
    @Provides
    fun provideAlarmsDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }
}