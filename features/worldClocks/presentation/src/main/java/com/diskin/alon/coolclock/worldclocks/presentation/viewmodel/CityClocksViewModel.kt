package com.diskin.alon.coolclock.worldclocks.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import com.diskin.alon.coolclock.worldclocks.application.usecase.GetSelectedCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.application.usecase.UnSelectCityUseCase
import com.diskin.alon.coolclock.worldclocks.application.util.AppError
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class CityClocksViewModel @Inject constructor(
    private val getSelectedCitiesUseCase: GetSelectedCitiesUseCase,
    private val unSelectCityUseCase: UnSelectCityUseCase,
    private val cityClocksMapper: CityClocksMapper
) : RxViewModel() {

    private val _cityClocks = MutableLiveData<PagingData<UiCityClock>>()
    val cityClocks: LiveData<PagingData<UiCityClock>> get() = _cityClocks
    private val deleteCitySubject = BehaviorSubject.create<UiCityClock>()
    val deleteCityClockError = SingleLiveEvent<AppError>()

    init {
        addSubscription(
            createClocksSubscription(),
            createDeleteCitySubscription()
        )
    }

    fun deleteCityClock(clock: UiCityClock) {
        deleteCitySubject.onNext(clock)
    }

    private fun createClocksSubscription(): Disposable {
        return getSelectedCitiesUseCase.execute(Unit)
            .observeOn(AndroidSchedulers.mainThread())
            .cachedIn(viewModelScope)
            .map(cityClocksMapper::map)
            .subscribe{ _cityClocks.value = it }
    }

    private fun createDeleteCitySubscription(): Disposable {
        return deleteCitySubject.concatMapSingle { unSelectCityUseCase.execute(it.id) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when(it) {
                    is AppResult.Error -> deleteCityClockError.value = it.error
                }
            },{
                println("DELETE CITY CLOCK ERROR:${it.message}")
                it.printStackTrace()
            })
    }
}