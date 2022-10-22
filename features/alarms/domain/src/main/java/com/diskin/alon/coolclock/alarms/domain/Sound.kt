package com.diskin.alon.coolclock.alarms.domain

sealed class Sound {

    data class AlarmSound(val path: String) : Sound() {

        init {
            require(path.isNotEmpty())
        }
    }

    object Silent : Sound()
}