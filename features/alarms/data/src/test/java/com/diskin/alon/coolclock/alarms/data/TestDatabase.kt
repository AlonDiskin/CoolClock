package com.diskin.alon.coolclock.alarms.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diskin.alon.coolclock.alarms.data.local.AlarmDao
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntityConverters

@TypeConverters(AlarmEntityConverters::class)
@Database(entities = [AlarmEntity::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase(){

    abstract fun alarmDao(): AlarmDao
}