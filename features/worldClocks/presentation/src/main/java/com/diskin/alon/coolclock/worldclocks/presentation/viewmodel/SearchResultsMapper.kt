package com.diskin.alon.coolclock.worldclocks.presentation.viewmodel

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult
import javax.inject.Inject

class SearchResultsMapper @Inject constructor() {

    fun map(results: PagingData<CityDto>): PagingData<UiCitySearchResult> {
        return results.map {
            UiCitySearchResult(
                it.id,
                it.name,
                it.country,
                it.state,
                it.isSelected
            )
        }
    }
}
