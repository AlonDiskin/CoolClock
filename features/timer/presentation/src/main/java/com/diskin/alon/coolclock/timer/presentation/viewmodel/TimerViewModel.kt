package com.diskin.alon.coolclock.timer.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diskin.alon.coolclock.timer.presentation.infrastructure.KEY_TIMER_DURATION
import com.diskin.alon.coolclock.timer.presentation.infrastructure.TimerService
import com.diskin.alon.coolclock.timer.presentation.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val app: Application,
    private val eventBus: EventBus
) : ViewModel() {

    private val _timer = MutableLiveData(UiTimer(0,0,0,0,UiTimerState.NOT_SET))
    val timer: LiveData<UiTimer> get() = _timer
    private val _progress = MutableLiveData<UiTimerProgress>()
    val progress: LiveData<UiTimerProgress> get() = _progress

    init {
        eventBus.register(this)
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
}