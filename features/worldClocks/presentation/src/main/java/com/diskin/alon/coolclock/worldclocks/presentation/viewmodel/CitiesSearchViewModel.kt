package com.diskin.alon.coolclock.worldclocks.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import com.diskin.alon.coolclock.worldclocks.application.usecase.AddCityUseCase
import com.diskin.alon.coolclock.worldclocks.application.usecase.SearchCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.application.util.AppError
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.application.util.mapAppResult
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

const val KEY_SEARCH_TEXT ="search_text"
const val KEY_SEARCH_QUERY = "search_query"
const val DEF_SEARCH_TXT = ""

@HiltViewModel
class CitiesSearchViewModel @Inject constructor(
    private val searchUseCase: SearchCitiesUseCase,
    private val addCityUseCase: AddCityUseCase,
    private val savedState: SavedStateHandle,
    private val resultsMapper: SearchResultsMapper
) : RxViewModel() {

    private val _results = MutableLiveData<PagingData<UiCitySearchResult>>()
    val results: LiveData<PagingData<UiCitySearchResult>> get() = _results
    private val _addedCity = MutableLiveData<String>()
    val addedCity: LiveData<String> get() = _addedCity
    var searchText: String
        set(value) = savedState.set(KEY_SEARCH_TEXT,value)
        get() = savedState[KEY_SEARCH_TEXT] ?: DEF_SEARCH_TXT
    private val querySubject = BehaviorSubject.create<String>()
    private val citySubject = BehaviorSubject.create<UiCitySearchResult>()
    val addCityError = SingleLiveEvent<AppError>()

    init {
        addSubscription(
            createSearchSubscription(),
            createAddCitySubscription()
        )
        restoreLastSearchIfExist()
    }

    fun search(query: String) {
        savedState.set(KEY_SEARCH_QUERY,query)
        querySubject.onNext(query)
    }

    fun addCity(result: UiCitySearchResult) {
        citySubject.onNext(result)
    }

    private fun createSearchSubscription(): Disposable {
        return querySubject.switchMap(searchUseCase::execute)
            .observeOn(AndroidSchedulers.mainThread())
            .cachedIn(viewModelScope)
            .map(resultsMapper::map)
            .subscribe { _results.value = it }
    }

    private fun createAddCitySubscription(): Disposable {
        return citySubject.concatMapSingle { city ->
            addCityUseCase.execute(city.id).mapAppResult{ city } }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when(it) {
                    is AppResult.Success -> _addedCity.value = it.data.name
                    is AppResult.Error -> addCityError.value = it.error
                }
            },{
                addCityError.value = AppError.UNKNOWN_ERROR
                it.printStackTrace()
            })
    }

    private fun restoreLastSearchIfExist() {
        savedState.get<String>(KEY_SEARCH_QUERY)?.let { querySubject.onNext(it) }
    }
}