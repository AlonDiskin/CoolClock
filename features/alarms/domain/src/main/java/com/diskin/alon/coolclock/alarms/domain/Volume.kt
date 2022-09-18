package com.diskin.alon.coolclock.alarms.domain

data class Volume(val vol: Int) {

    init {
        require( vol in 0..10)
    }
}