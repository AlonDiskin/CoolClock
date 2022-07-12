package com.diskin.alon.coolclock.worldclocks.application.usecase

import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.application.util.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetSelectedCitiesUseCase @Inject constructor(
    private val  repository: CitiesRepository,
    private val mapper: CitiesPagingMapper
) : UseCase<Unit,Observable<PagingData<CityDto>>> {

    override fun execute(param: Unit): Observable<PagingData<CityDto>> {
        return repository.getAllSelected()
            .map(mapper::map)
    }
}