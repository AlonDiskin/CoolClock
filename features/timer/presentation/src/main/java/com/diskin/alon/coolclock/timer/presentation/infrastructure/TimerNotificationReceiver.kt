package com.diskin.alon.coolclock.timer.presentation.infrastructure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.diskin.alon.coolclock.timer.presentation.model.TimerControl
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class TimerNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmManager: TimerAlarmManager
    @Inject
    lateinit var notificationManager: NotificationManagerCompat
    @Inject
    lateinit var eventBus: EventBus

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            "ACTION_TIMER_PAUSE" -> eventBus.post(TimerControl.PAUSE)
            "ACTION_TIMER_CANCEL" -> eventBus.post(TimerControl.CANCEL)
            "ACTION_TIMER_RESUME" -> eventBus.post(TimerControl.RESUME)
            "ACTION_TIMER_ALERT_CANCEL" -> {
                alarmManager.stopAlarm()
                notificationManager.cancel(NOTIFICATION_ID_TIMER_ALERT)
            }
        }
    }
}