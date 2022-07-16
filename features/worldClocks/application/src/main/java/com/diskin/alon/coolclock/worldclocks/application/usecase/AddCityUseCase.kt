package com.diskin.alon.coolclock.worldclocks.application.usecase

import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.application.util.UseCase
import io.reactivex.Single
import javax.inject.Inject

class AddCityUseCase @Inject constructor(
    private val repository: CitiesRepository
) : UseCase<Long, Single<AppResult<Unit>>> {

    override fun execute(param: Long): Single<AppResult<Unit>> {
        return repository.addToSelected(param)
    }
}