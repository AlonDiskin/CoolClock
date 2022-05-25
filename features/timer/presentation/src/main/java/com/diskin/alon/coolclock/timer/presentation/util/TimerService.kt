package com.diskin.alon.coolclock.timer.presentation.util

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import com.diskin.alon.coolclock.timer.presentation.model.TimerControl
import com.diskin.alon.coolclock.timer.presentation.model.UiTimer
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerProgress
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerState
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val KEY_TIMER_DURATION = "duration"

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var eventBus: EventBus
    @Inject
    lateinit var notificationsManager: TimerNotificationsManager
    @VisibleForTesting
    lateinit var countDownTimer: CountDownTimer
    private var remainTime: Long? = null
    private var lastUpdated: UiTimer? = null
    private var initialDuration: Long? = null
    @VisibleForTesting
    var isTimerRunning = false

    override fun onCreate() {
        super.onCreate()
        eventBus.register(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getLongExtra(KEY_TIMER_DURATION,0L)?.let {
            if (it > 0L) startTimer(it,false)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTimerControlEvent(event: TimerControl) {
        when(event) {
            TimerControl.RESUME -> resumeTimer()
            TimerControl.PAUSE -> pauseTimer()
            TimerControl.CANCEL -> cancelTimer()
        }
    }

    private fun startTimer(time: Long,resumed: Boolean = false) {
        isTimerRunning = true
        when(resumed) {
            true -> {
                updateTimer(time,UiTimerState.RESUMED)
            }

            false -> {
                initialDuration = time
                eventBus.postSticky(UiTimerProgress(time.toInt(),time.toInt()))
                updateTimer(time,UiTimerState.START)
            }
        }

        remainTime = time
        countDownTimer = object : CountDownTimer(time, 10) {

            override fun onTick(millisUntilFinished: Long) {
                remainTime = millisUntilFinished
                EventBus.getDefault().postSticky(UiTimerProgress(initialDuration!!.toInt(),millisUntilFinished.toInt()))
                val roundedMillis = if (millisUntilFinished % 1000 < 1000) {
                    (1000 - (millisUntilFinished % 1000)) + millisUntilFinished

                } else {
                    millisUntilFinished
                }

                if (roundedMillis != lastUpdated!!.remainTime) {
                    updateTimer(roundedMillis,UiTimerState.RUNNING)
                }
            }

            override fun onFinish() {
                isTimerRunning = false

                notificationsManager.showTimerAlertNotification()
                stopSelf()
                updateTimer(0,UiTimerState.DONE)
                eventBus.postSticky(UiTimerProgress(0,0))
            }

        }.start()
    }

    private fun updateTimer(time: Long,state: UiTimerState) {
        val hours = TimeUnit.MILLISECONDS.toHours(time).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(time) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))).toInt()
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(time) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))).toInt()
        lastUpdated = UiTimer(
            seconds,
            minutes,
            hours,
            time,
            state
        )

        lastUpdated?.let { eventBus.postSticky(it) }
    }

    private fun cancelTimer() {
        isTimerRunning = false

        countDownTimer.cancel()
        updateTimer(lastUpdated!!.remainTime,UiTimerState.DONE)
        eventBus.postSticky(UiTimerProgress(0,0))
        stopSelf()
    }

    private fun pauseTimer() {
        isTimerRunning = false

        countDownTimer.cancel()
        updateTimer(lastUpdated!!.remainTime,UiTimerState.PAUSED)
    }

    private fun resumeTimer() {
        remainTime?.let { startTimer(it,true) }
    }
}