package com.diskin.alon.coolclock.worldclocks.application.usecase

import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.domain.City
import javax.inject.Inject

class CitiesMapper @Inject constructor() {

    fun map(cities: List<City>): List<CityDto> {
        return cities.map { city ->
            CityDto(
                city.id,
                city.name,
                city.country,
                city.state,
                city.gmt,
                city.isSelected
            )
        }
    }
}