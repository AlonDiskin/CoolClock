package com.diskin.alon.coolclock.worldclocks.domain

import org.joda.time.DateTime

data class City(val id: Long,
                val name: String,
                val country: String,
                val state: String,
                val gmt: String,
                val isSelected: Boolean) {

    val dateTime: DateTime? get() = null

    init {
        require(name.isNotEmpty())
        require(country.isNotEmpty())
        require(gmt.isNotEmpty())
    }

    fun s(): DateTime {
        TODO()
    }
}