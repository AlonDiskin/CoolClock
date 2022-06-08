package com.diskin.alon.coolclock.worldclocks.domain

data class City(val id: Long,
                val name: String,
                val country: String,
                val state: String,
                val gmt: String,
                val isSelected: Boolean) {

    init {
        require(name.isNotEmpty())
        require(country.isNotEmpty())
        require(gmt.isNotEmpty())
    }
}