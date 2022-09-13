package com.diskin.alon.coolclock.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diskin.alon.coolclock.alarms.data.local.AlarmDao
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntityConverters
import com.diskin.alon.coolclock.worldclocks.data.CityDao
import com.diskin.alon.coolclock.worldclocks.data.CityEntity

@TypeConverters(AlarmEntityConverters::class)
@Database(entities = [CityEntity::class,AlarmEntity::class], version = 1, exportSchema = false)
abstract class AppTestDatabase : RoomDatabase(){

    abstract fun cityDao(): CityDao

    abstract fun alarmDao(): AlarmDao
}