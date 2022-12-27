package com.diskin.alon.coolclock.alarms.device

import java.io.Serializable

data class DeviceAlarm(val id: Int,
                       val isVibrate: Boolean,
                       val ringtone: String,
                       val duration: Int,
                       val volume: Int,
                       val isSnooze: Boolean,
                       val name: String) : Serializable
