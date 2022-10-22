package com.diskin.alon.coolclock.alarms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.domain.WeekDay

@Entity(tableName = "user_alarms")
data class AlarmEntity(val name: String,
                       val hour: Int,
                       val minute: Int,
                       val repeatDays: Set<WeekDay>,
                       val isScheduled: Boolean,
                       val sound: Sound,
                       val isVibrate: Boolean,
                       val duration: Int,
                       val volume: Int,
                       val snooze: Int,
                       val isSnoozed: Boolean,
                       @PrimaryKey(autoGenerate = true) val id: Int? = null)