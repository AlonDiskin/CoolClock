package com.diskin.alon.coolclock.alarms.device

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_ALARM_ID = "alarm notification channel id"
const val CHANNEL_ALARM_NAME = "alarm notification channel name"
const val CHANNEL_ALARM_DESCRIPTION = "alarm notification channel description"

@Singleton
class AlarmNotificationFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val notificationManager: NotificationManagerCompat,
) {

    init {
        createAlarmNotificationChannel()
    }

    fun createAlarmNotification(alarm: DeviceAlarm): Notification {
        val currentTimeFormat = SimpleDateFormat(appContext.getString(R.string.format_alarm_titme))
        val currentTimeDate = Calendar.getInstance().time
        val notificationTitle = currentTimeFormat.format(currentTimeDate)
        val cancelAlarmPendingIntent = Intent(appContext, AlarmReceiver::class.java).run {
            action = ACTION_STOP_ALARM

            putExtra(KEY_ALARM_ID,alarm.id)
            PendingIntent.getBroadcast(
                appContext,
                0,
                this,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val fullScreenIntent = Intent(appContext, AlarmActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(appContext, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =  NotificationCompat.Builder(appContext, CHANNEL_ALARM_ID)
            .setSmallIcon(R.drawable.ic_baseline_alarm_18)
            .setAutoCancel(true)
            .setContentTitle(notificationTitle)
            .setContentText(alarm.name)
            .setCategory(Notification.CATEGORY_ALARM)
            .addAction(NotificationCompat.Action.Builder(
                0,
                appContext.getString(R.string.button_notification_dismiss),
                cancelAlarmPendingIntent).build()
            )
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (alarm.isSnooze) {
            val snoozePendingIntent = Intent(appContext, AlarmReceiver::class.java).run {
                action = ACTION_SNOOZE_ALARM

                putExtra(KEY_ALARM_ID,alarm.id)
                PendingIntent.getBroadcast(
                    appContext,
                    0,
                    this,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            builder.addAction(NotificationCompat.Action.Builder(
                0,
                appContext.getString(R.string.button_notification_snooze),
                snoozePendingIntent).build()
            )
        }

        return builder.build()
    }

    private fun createAlarmNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ALARM_ID, CHANNEL_ALARM_NAME, importance).apply {
                description = CHANNEL_ALARM_DESCRIPTION
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }
}