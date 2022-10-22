package com.diskin.alon.coolclock.alarms.application.model

sealed class PlayRingtoneSampleRequest {

    data class Ringtone(val path: String,val volume: Int): PlayRingtoneSampleRequest()

    object Stop : PlayRingtoneSampleRequest()
}