package com.diskin.alon.coolclock.worldclocks.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.domain.City
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Observable
import androidx.paging.rxjava2.observable
import com.diskin.alon.coolclock.worldclocks.application.util.AppError
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.application.util.toSingleAppResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

const val PAGE_SIZE = 20

@ActivityRetainedScoped
class CitiesRepositoryImpl @Inject constructor(
    private val dao: CityDao,
    private val mapper: CityMapper
) : CitiesRepository {

    override fun search(query: String): Observable<PagingData<City>> {
        return Pager(PagingConfig(PAGE_SIZE)) { dao.getStartsWith(query) }
            .observable
            .subscribeOn(Schedulers.io())
            .map(mapper::map)
    }

    override fun addToSelected(id: Long): Single<AppResult<Unit>> {
        return dao.select(id)
            .toSingleDefault(Unit)
            .subscribeOn(Schedulers.io())
            .toSingleAppResult { AppError.UNKNOWN_ERROR }
    }
}
