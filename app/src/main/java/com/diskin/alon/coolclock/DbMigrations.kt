package com.diskin.alon.coolclock

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add 'selectedDate' column to cities table
        database.execSQL("ALTER TABLE cities ADD COLUMN selectedDate INTEGER")
    }
}