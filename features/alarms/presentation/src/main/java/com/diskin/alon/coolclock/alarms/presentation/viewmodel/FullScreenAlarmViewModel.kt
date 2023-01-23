package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.coolclock.alarms.application.usecase.SnoozeAlarmUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.StopAlarmUseCase
import com.diskin.alon.coolclock.alarms.presentation.model.AlarmStoppedEvent
import com.diskin.alon.coolclock.alarms.presentation.model.UiFullScreenAlarm
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

const val KEY_ALARM_DATA = "alarm_data"

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val stopAlarm: StopAlarmUseCase,
    private val snoozeAlarm: SnoozeAlarmUseCase,
    private val savedState: SavedStateHandle,
    private val eventBus: EventBus
) : RxViewModel() {

    val alarmData: LiveData<UiFullScreenAlarm> = initAlarmData()
    val alarmStopped = SingleLiveEvent<Boolean>()

    init {
        eventBus.register(this)
    }

    override fun onCleared() {
        super.onCleared()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlarmStopped(event: AlarmStoppedEvent) {
        alarmStopped.value = true
    }

    fun snooze() {
        alarmData.value?.let { alarm ->
            addSubscription(
                snoozeAlarm.execute(alarm.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        alarmStopped.value = true
                    },{ error ->
                        println("ALARM SNOOZE FAIL!")
                        error.printStackTrace()
                    })
            )
        }
    }

    fun dismiss() {
        alarmData.value?.let { alarm ->
            addSubscription(
                stopAlarm.execute(alarm.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        alarmStopped.value = true
                    },{ error ->
                        println("ALARM DISMISS FAIL!")
                        error.printStackTrace()
                    })
            )
        }
    }

    private fun initAlarmData(): LiveData<UiFullScreenAlarm> {
        return savedState.get<UiFullScreenAlarm>(KEY_ALARM_DATA)?.let {
            MutableLiveData(it)
        } ?: throw IllegalStateException("${AlarmViewModel::class.java.simpleName} must contain alarm id in saved state")
    }
}