package com.diskin.alon.coolclock.alarms.application.model

sealed class AlarmSound {

    data class Ringtone(val path: String,val name: String) : AlarmSound()

    object Silent : AlarmSound()

}
