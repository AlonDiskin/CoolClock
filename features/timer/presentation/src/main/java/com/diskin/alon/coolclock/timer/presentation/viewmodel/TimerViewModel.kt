package com.diskin.alon.coolclock.timer.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.coolclock.common.presentation.RxViewModel
import com.diskin.alon.coolclock.timer.data.TimerDuration
import com.diskin.alon.coolclock.timer.data.TimerDurationStore
import com.diskin.alon.coolclock.timer.presentation.device.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.device.TimerService
import com.diskin.alon.coolclock.timer.presentation.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val app: Application,
    private val eventBus: EventBus,
    private val timerDurationStore: TimerDurationStore
) : RxViewModel() {

    val timerDuration = MutableLiveData<UiTimerDuration>()
    private val _timer = MutableLiveData<UiTimer>()
    val timer: LiveData<UiTimer> get() = _timer
    private val _progress = MutableLiveData<UiTimerProgress>()
    val progress: LiveData<UiTimerProgress> get() = _progress

    init {
        eventBus.register(this)

        // Set default timer value,if non was passed as sticky event after bus registration
        if (_timer.value == null) {
            _timer.value = UiTimer(0,0,0,0,UiTimerState.NOT_SET)
        }

        // Get last timer duration
        addSubscription(createTimerDurationSubscription())
    }

    override fun onCleared() {
        super.onCleared()
        saveTimerDuration()
    }

    fun startTimer(time: Long) {
        val intent = Intent(app, TimerService::class.java).apply { putExtra(KEY_TIMER_DURATION,time) }

        app.startService(intent)
    }

    fun cancelTimer() {
        eventBus.post(TimerControl.CANCEL)
    }

    fun pauseTimer() {
        eventBus.post(TimerControl.PAUSE)
    }

    fun resumeTimer() {
        eventBus.post(TimerControl.RESUME)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onTimerUpdateEvent(event: UiTimer) {
        _timer.value = event
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onTimerProgressUpdateEvent(event: UiTimerProgress) {
        _progress.value = event
    }

    fun hideTimerNotification() {
        eventBus.post(NotificationRequest.HIDE)
    }

    fun showTimerNotification() {
        eventBus.post(NotificationRequest.SHOW)
    }

    private fun saveTimerDuration() {
        timerDuration.value?.let {
            timerDurationStore.save(
                TimerDuration(
                    it.seconds,
                    it.minutes,
                    it.hours
                )
            )
        }
    }

    private fun createTimerDurationSubscription(): Disposable {
        return timerDurationStore.getLast()
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                UiTimerDuration(
                    it.seconds,
                    it.minutes,
                    it.hours
                )
            }
            .subscribe({ timerDuration.value = it },{ it.printStackTrace() })
    }
}