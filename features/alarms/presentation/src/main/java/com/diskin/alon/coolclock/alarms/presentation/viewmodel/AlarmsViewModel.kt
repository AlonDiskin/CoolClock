package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.diskin.alon.coolclock.alarms.application.model.AlarmActivation
import com.diskin.alon.coolclock.alarms.application.model.NextAlarm
import com.diskin.alon.coolclock.alarms.application.usecase.GetAlarmsBrowserUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.SetAlarmActivationUseCase
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import com.diskin.alon.coolclock.common.application.AppError
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val getAlarms: GetAlarmsBrowserUseCase,
    private val setAlarmActivation: SetAlarmActivationUseCase,
    private val alarmsMapper: AlarmsMapper,
    private val dateFormatter: ScheduledAlarmDateFormatter
) : RxViewModel() {

    private val _alarms = MutableLiveData<PagingData<UiAlarm>>()
    val alarms: LiveData<PagingData<UiAlarm>> get() = _alarms
    val latestScheduledAlarm = SingleLiveEvent<String>()
    private val alarmActivationSubject = BehaviorSubject.create<AlarmActivation>()
    val alarmActivationError = SingleLiveEvent<AppError>()

    init {
        addSubscription(
            createAlarmsSubscription(),
            createAlarmActivationSubscription()
        )
    }

    private fun createAlarmsSubscription(): Disposable {
        return getAlarms.execute(Unit)
            .observeOn(AndroidSchedulers.mainThread())
            .cachedIn(viewModelScope)
            .map(alarmsMapper::map)
            .subscribe{ _alarms.value = it }
    }

    private fun createAlarmActivationSubscription(): Disposable {
        return alarmActivationSubject
            .concatMapSingle { setAlarmActivation.execute(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when(it) {
                    is AppResult.Success -> handleAlarmActivationSuccess(it.data)
                    is AppResult.Error -> handleAlarmActivationError(it.error)
                }
            },::handleAlarmActivationSubscriptionError)
    }

    fun changeAlarmActivation(id: Int, activation: Boolean) {
        alarmActivationSubject.onNext(AlarmActivation(id,activation))
    }

    private fun handleAlarmActivationSuccess(scheduled: NextAlarm) {
        if (scheduled is NextAlarm.Next) {
            latestScheduledAlarm.value = dateFormatter.format(scheduled.millis)
        }
    }

    private fun handleAlarmActivationError(error: AppError) {
        alarmActivationError.value = error
    }

    private fun handleAlarmActivationSubscriptionError(error: Throwable) {
        alarmActivationError.value = AppError.UNKNOWN_ERROR
    }
}