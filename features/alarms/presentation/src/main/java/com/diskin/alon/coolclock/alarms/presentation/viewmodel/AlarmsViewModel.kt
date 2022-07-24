package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.coolclock.alarms.application.usecase.GetCreatedAlarmsUseCase
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val getAlarms: GetCreatedAlarmsUseCase,
    private val alarmsMapper: AlarmsMapper
) : RxViewModel() {

    private val _alarms = MutableLiveData<PagingData<UiAlarm>>()
    val alarms: LiveData<PagingData<UiAlarm>> get() = _alarms

    init {
        addSubscription(createAlarmsSubscription())
    }

    private fun createAlarmsSubscription(): Disposable {
        return getAlarms.execute(Unit)
            .observeOn(AndroidSchedulers.mainThread())
            .cachedIn(viewModelScope)
            .map(alarmsMapper::map)
            .subscribe{ _alarms.value = it }
    }
}