package com.diskin.alon.coolclock.worldclocks.application.model

data class CityDto(val id: Long,
                   val name: String,
                   val country: String,
                   val state: String,
                   val gmt: String,
                   val isSelected: Boolean)