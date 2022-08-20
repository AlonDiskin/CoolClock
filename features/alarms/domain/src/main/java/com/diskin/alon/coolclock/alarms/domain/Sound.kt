package com.diskin.alon.coolclock.alarms.domain

sealed class Sound {

    data class Ringtone(val path: String) : Sound() {

        init {
            require(path.isNotEmpty())
        }
    }

    object None : Sound()
}