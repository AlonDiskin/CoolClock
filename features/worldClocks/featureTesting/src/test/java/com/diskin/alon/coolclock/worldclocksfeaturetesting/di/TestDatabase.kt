package com.diskin.alon.coolclock.worldclocksfeaturetesting.di

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diskin.alon.coolclock.worldclocks.data.CityDao
import com.diskin.alon.coolclock.worldclocks.data.CityEntity

@Database(entities = [CityEntity::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase(){

    abstract fun cityDao(): CityDao
}