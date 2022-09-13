package com.diskin.alon.coolclock

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE cities ADD COLUMN selectedDate INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `alarms` (`name` TEXT NOT NULL,`hour` INTEGER NOT NULL,`minute` INTEGER NOT NULL,`repeatDays` TEXT NOT NULL,`isActive` INTEGER NOT NULL,`ringtone` TEXT NOT NULL,`isVibrate` INTEGER NOT NULL,`isSound` INTEGER NOT NULL,`id` INTEGER PRIMARY KEY)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE alarms ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE alarms ADD COLUMN volume INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE alarms ADD COLUMN isSnooze INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE alarms ADD COLUMN snoozeRepeat INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE alarms ADD COLUMN snoozeInterval INTEGER NOT NULL DEFAULT 0")
    }
}