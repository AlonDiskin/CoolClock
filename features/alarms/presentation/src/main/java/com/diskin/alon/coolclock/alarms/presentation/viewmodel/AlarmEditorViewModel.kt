package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.coolclock.alarms.application.model.GetEditRequest
import com.diskin.alon.coolclock.alarms.application.model.PlayRingtoneSampleRequest
import com.diskin.alon.coolclock.alarms.application.model.ScheduleAlarmRequest
import com.diskin.alon.coolclock.alarms.application.usecase.GetAlarmEditUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.PlayRingtoneSampleUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.ScheduleAlarmUseCase
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarmEdit
import com.diskin.alon.coolclock.common.presentation.VolumeButtonPressEvent
import com.diskin.alon.coolclock.common.application.AppError
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.mapAppResult
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

const val KEY_ALARM_ID_ARG = "alarm_id"
const val KEY_ALARM_EDIT_ARG = "alarm_edit"

@HiltViewModel
class AlarmEditorViewModel @Inject constructor(
    private val getAlarmEdit: GetAlarmEditUseCase,
    private val playRingtone: PlayRingtoneSampleUseCase,
    private val scheduleAlarm: ScheduleAlarmUseCase,
    private val alarmEditMapper: UiAlarmEditMapper,
    private val scheduleRequestMapper: ScheduleAlarmRequestMapper,
    private val dateFormatter: ScheduledAlarmDateFormatter,
    private val savedState: SavedStateHandle,
    private val eventBus: EventBus
) : RxViewModel() {

    val alarmEdit: LiveData<UiAlarmEdit> = savedState.getLiveData(KEY_ALARM_EDIT_ARG)
    private val playRingtoneSubject = BehaviorSubject.create<PlayRingtoneSampleRequest>()
    val playRingtoneError = SingleLiveEvent<AppError>()
    val alarmEditError = SingleLiveEvent<AppError>()
    private val scheduleRequestSubject = BehaviorSubject.create<ScheduleAlarmRequest>()
    val scheduledAlarmDate = SingleLiveEvent<String>()
    val scheduleAlarmError = SingleLiveEvent<AppError>()
    val volumeButtonPress = SingleLiveEvent<VolumeButtonPressEvent>()

    init {
        eventBus.register(this)
        addSubscription(
            createScheduleAlarmSubscription()
        )
        createPlayRingtoneSubscription()

        // Load new/existing edit if there is no existing edit value
        if (alarmEdit.value == null) {
            val editRequest = when(savedState.contains(KEY_ALARM_ID_ARG)) {
                true -> GetEditRequest.Existing(savedState[KEY_ALARM_ID_ARG]!!)
                false -> GetEditRequest.New
            }

            addSubscription(createAlarmEditSubscription(editRequest))
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVolumeButtonPressedEvent(event: VolumeButtonPressEvent) {
        volumeButtonPress.value = event
    }

    fun stopRingtonePlayback() {
        playRingtoneSubject.onNext(PlayRingtoneSampleRequest.Stop)
    }

    fun playRingtoneSample(path: String) {
        playRingtoneSubject.onNext(PlayRingtoneSampleRequest.Ringtone(path,alarmEdit.value!!.volume))
    }

    fun schedule() {
        alarmEdit.value?.let {
            scheduleRequestSubject.onNext(
                when(savedState.contains(KEY_ALARM_ID_ARG)) {
                    false -> scheduleRequestMapper.mapNew(it)
                    else -> scheduleRequestMapper.mapUpdate(it,savedState[KEY_ALARM_ID_ARG]!!)
                }
            )
        }
    }

    private fun createPlayRingtoneSubscription(): Disposable {
        return playRingtoneSubject.switchMapSingle { playRingtone.execute(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

            },{
                playRingtoneError.value = AppError.INTERNAL_ERROR
                it.printStackTrace()
                println("PLAY RINGTONE RESULT ERROR!")
            })
    }

    private fun createAlarmEditSubscription(request: GetEditRequest): Disposable {
        return getAlarmEdit.execute(request)
            .observeOn(AndroidSchedulers.mainThread())
            .mapAppResult(alarmEditMapper::map)
            .subscribe({
                when(it) {
                    is AppResult.Success -> savedState[KEY_ALARM_EDIT_ARG] = it.data
                    is AppResult.Error -> {
                        alarmEditError.value = it.error
                        println("GET ALARM EDIT RESULT ERROR!")
                    }
                }
            },{
                alarmEditError.value = AppError.INTERNAL_ERROR
                it.printStackTrace()
                println("GET ALARM EDIT SUBSCRIPTION ERROR!")
            })
    }

    private fun createScheduleAlarmSubscription(): Disposable {
        return scheduleRequestSubject.concatMapSingle { scheduleAlarm.execute(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when(it) {
                    is AppResult.Success -> scheduledAlarmDate.value = dateFormatter.format(it.data)
                    is AppResult.Error -> {
                        scheduleAlarmError.value = it.error
                        println("SCHEDULE ALARM SUBSCRIPTION ERROR!")
                    }
                }
            },{
                scheduleAlarmError.value = AppError.INTERNAL_ERROR
                it.printStackTrace()
                println("SCHEDULE ALARM SUBSCRIPTION ERROR!")
            })
    }
}