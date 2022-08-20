package com.diskin.alon.coolclock.alarms.domain

data class Duration(val minutes: Int) {

    init {
        require(minutes in 1..5)
    }
}