package com.diskin.alon.coolclock.alarms.application.model

sealed class NextAlarm {

    data class Next(val millis: Long): NextAlarm()

    object None : NextAlarm()
}