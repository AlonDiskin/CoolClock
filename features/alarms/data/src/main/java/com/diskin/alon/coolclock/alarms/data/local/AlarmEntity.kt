package com.diskin.alon.coolclock.alarms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diskin.alon.coolclock.alarms.domain.WeekDay

@Entity(tableName = "alarms")
data class AlarmEntity(val name: String,
                       val hour: Int,
                       val minute: Int,
                       val repeatDays: Set<WeekDay>,
                       val isActive: Boolean,
                       val ringtone: String,
                       val isVibrate: Boolean,
                       val isSound: Boolean,
                       @PrimaryKey(autoGenerate = true) val id: Int? = null)