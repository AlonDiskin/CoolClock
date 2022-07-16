package com.diskin.alon.coolclock.worldclocks.data

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.worldclocks.domain.City
import javax.inject.Inject

class CityMapper @Inject constructor() {

    fun map(cities: PagingData<CityEntity>): PagingData<City> {
        return cities.map {
            City(
                it.id!!,
                it.name,
                it.country,
                it.state,
                it.timezone,
                it.isSelected
            )
        }
    }
}