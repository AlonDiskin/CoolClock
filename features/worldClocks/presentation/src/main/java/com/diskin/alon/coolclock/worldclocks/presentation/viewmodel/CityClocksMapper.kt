package com.diskin.alon.coolclock.worldclocks.presentation.viewmodel

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock
import javax.inject.Inject

class CityClocksMapper @Inject constructor() {

    fun map(cities:  PagingData<CityDto>): PagingData<UiCityClock> {
        return cities.map {
            UiCityClock(
                it.id,
                it.name,
                it.country,
                it.state,
                it.gmt
            )
        }
    }
}
