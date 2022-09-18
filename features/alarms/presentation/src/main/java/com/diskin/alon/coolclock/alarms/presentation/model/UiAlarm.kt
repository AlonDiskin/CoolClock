package com.diskin.alon.coolclock.alarms.presentation.model

import com.diskin.alon.coolclock.alarms.application.model.RepeatDay

data class UiAlarm(val id: Int,
                   val time: String,
                   val name: String,
                   val isActive: Boolean,
                   val nextAlarm: String,
                   val repeatDays: Set<RepeatDay>)