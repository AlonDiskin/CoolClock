package com.diskin.alon.coolclock.worldclocks.application.interfaces

import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.domain.City
import io.reactivex.Observable

interface CitiesRepository {

    fun search(query: String): Observable<PagingData<City>>
}