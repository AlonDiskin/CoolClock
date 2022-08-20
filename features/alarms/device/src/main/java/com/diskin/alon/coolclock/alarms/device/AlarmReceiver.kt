package com.diskin.alon.coolclock.alarms.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

const val ACTION_ALARM = "action alarm"
const val ALARM_ID = "alarm id"

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_ALARM -> {

            }
        }
    }
}