package com.diskin.alon.coolclock.alarms.presentation.model

import java.io.Serializable

data class UiFullScreenAlarm(val id: Int,
                             val name: String,
                             val time: String,
                             val snoozedEnabled: Boolean) : Serializable