package com.diskin.alon.coolclock.alarms.application.model

sealed class GetEditRequest {

    object New : GetEditRequest()

    data class Existing(val id: Int) : GetEditRequest()
}
