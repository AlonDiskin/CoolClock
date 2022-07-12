package com.diskin.alon.coolclock.worldclocks.application.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.domain.City
import javax.inject.Inject

class CitiesPagingMapper @Inject constructor() {

    fun map(results: PagingData<City>): PagingData<CityDto> {
        return results.map {
            CityDto(
                it.id,
                it.name,
                it.country,
                it.state,
                it.gmt,
                it.isSelected
            )
        }
    }
}
