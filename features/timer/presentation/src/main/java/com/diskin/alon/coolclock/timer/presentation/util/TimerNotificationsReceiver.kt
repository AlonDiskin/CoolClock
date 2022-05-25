package com.diskin.alon.coolclock.timer.presentation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerNotificationsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: TimerNotificationsManager

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            "ACTION_TIMER_ALERT_CANCEL" -> notificationManager.dismissTimerAlertNotification()
        }
    }
}