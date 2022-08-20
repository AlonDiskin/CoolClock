package com.diskin.alon.coolclock.alarms.domain

sealed class Snooze {

    data class Active(val repeat: Int,val interval: Int) : Snooze() {

        init {
            require(repeat in 1..5)
            require(interval in 1..30)
        }
    }

    object None : Snooze()
}
