package com.diskin.alon.coolclock.worldclocks.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase(){

    abstract fun cityDao(): CityDao
}