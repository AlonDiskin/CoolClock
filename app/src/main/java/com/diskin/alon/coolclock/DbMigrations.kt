package com.diskin.alon.coolclock

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add 'selectedDate' column to cities table
        database.execSQL("ALTER TABLE cities ADD COLUMN selectedDate INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add 'selectedDate' column to cities table
        database.execSQL("CREATE TABLE `alarms` (`name` TEXT NOT NULL,`hour` INTEGER NOT NULL,`minute` INTEGER NOT NULL,`repeatDays` TEXT NOT NULL,`isActive` INTEGER NOT NULL,`ringtone` TEXT NOT NULL,`isVibrate` INTEGER NOT NULL,`isSound` INTEGER NOT NULL,`id` INTEGER PRIMARY KEY)")
    }
}