package com.diskin.alon.coolclock.alarms.application.model

sealed class NextAlarm {

    data class Next(val millis: Long): NextAlarm() {

        init {
            require(millis != 0L)
        }
    }

    object None : NextAlarm()
}