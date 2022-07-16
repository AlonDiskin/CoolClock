package com.diskin.alon.coolclock.worldclocks.application.usecase

import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.application.util.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val repository: CitiesRepository,
    private val mapper: CitiesPagingMapper
) : UseCase<String,Observable<PagingData<CityDto>>> {

    override fun execute(param: String): Observable<PagingData<CityDto>> {
        return repository.search(param)
            .map(mapper::map)
    }
}