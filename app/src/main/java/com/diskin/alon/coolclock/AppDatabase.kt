package com.diskin.alon.coolclock

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diskin.alon.coolclock.worldclocks.data.CityDao
import com.diskin.alon.coolclock.worldclocks.data.CityEntity

@Database(entities = [CityEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase(){

    abstract fun cityDao(): CityDao
}