package com.diskin.alon.coolclock.timer.presentation.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.diskin.alon.coolclock.timer.presentation.R
import javax.inject.Inject
import javax.inject.Singleton

const val NOTIFICATION_ID_TIMER_ALERT = 200
const val CHANNEL_ID_TIMER_ALERT = "timer alert notification channel id"
const val CHANNEL_NAME_TIMER_ALERT = "timer alert notification channel name"
const val CHANNEL_DESCRIPTION_TIMER_ALERT = "timer alert notification channel description"

@Singleton
class TimerNotificationsManager @Inject constructor(
    private val app: Application
) {

    init {
        //createTimerAlertNotificationChannel()
    }

    fun dismissTimerAlertNotification() {
        with(NotificationManagerCompat.from(app)) { cancel(NOTIFICATION_ID_TIMER_ALERT) }
    }

    fun showTimerAlertNotification() {
        createTimerAlertNotificationChannel()
        val cancelTimerIntent = Intent(app, TimerNotificationsReceiver::class.java).apply {
            action = "ACTION_TIMER_ALERT_CANCEL"
        }
        val cancelTimerPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            0,
            cancelTimerIntent,
            0
        )
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val fullScreenIntent = app.packageManager.getLaunchIntentForPackage(app.packageName)
        val fullScreenPendingIntent = PendingIntent.getActivity(app, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(app, CHANNEL_ID_TIMER_ALERT )
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContentTitle(app.getString(R.string.timer_alert_notification_title))
            .setContentText(app.getString(R.string.timer_alert_notification_content))
            .setSound(alarmSound)
            .addAction(NotificationCompat.Action.Builder(0, "dismiss", cancelTimerPendingIntent).build())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
        with(NotificationManagerCompat.from(app)) { notify(NOTIFICATION_ID_TIMER_ALERT, builder.build()) }
    }
    
    private fun createTimerAlertNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_TIMER_ALERT, CHANNEL_NAME_TIMER_ALERT, importance).apply {
                description = CHANNEL_DESCRIPTION_TIMER_ALERT
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = app
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(alarmSound, audioAttributes)

            notificationManager.createNotificationChannel(channel)
        }
    }
}
