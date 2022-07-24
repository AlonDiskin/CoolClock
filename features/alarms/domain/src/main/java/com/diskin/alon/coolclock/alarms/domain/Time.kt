package com.diskin.alon.coolclock.alarms.domain

data class Time(val hour: Int, val minute: Int) {

    init {
        require(hour in 0..23)
        require(minute in 0..59)
    }
}