package com.diskin.alon.coolclock.worldclocks.application.interfaces

import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.domain.City
import io.reactivex.Observable
import io.reactivex.Single

interface CitiesRepository {

    fun search(query: String): Observable<PagingData<City>>

    fun addToSelected(id: Long): Single<AppResult<Unit>>
}