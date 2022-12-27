package com.diskin.alon.coolclock.timer.presentation.device

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.diskin.alon.coolclock.timer.presentation.R
import javax.inject.Inject
import javax.inject.Singleton

const val NOTIFICATION_ID_TIMER = 100
const val NOTIFICATION_ID_TIMER_ALERT = 200
const val CHANNEL_ID_TIMER_ALERT = "timer alert notification channel id"
const val CHANNEL_NAME_TIMER_ALERT = "timer alert notification channel name"
const val CHANNEL_DESCRIPTION_TIMER_ALERT = "timer alert notification channel description"
const val CHANNEL_ID_TIMER = "timer notification channel id"
const val CHANNEL_NAME_TIMER = "timer notification channel name"
const val CHANNEL_DESCRIPTION_TIMER = "timer notification channel description"

@Singleton
class TimerNotificationFactory @Inject constructor(
    private val app: Application
) {

    fun createTimerAlertNotification(): Notification {
        val cancelTimerIntent = Intent(app, TimerNotificationReceiver::class.java).apply {
            action = "ACTION_TIMER_ALERT_CANCEL"
        }
        val cancelTimerPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            0,
            cancelTimerIntent,
            0
        )
        val alarmSound: Uri = Settings.System.DEFAULT_NOTIFICATION_URI//RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val fullScreenIntent = app.packageManager.getLaunchIntentForPackage(app.packageName)
        val fullScreenPendingIntent = PendingIntent.getActivity(app, 0,
            fullScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(app, CHANNEL_ID_TIMER_ALERT )
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContentTitle(app.getString(R.string.timer_alert_notification_title))
            .setContentText(app.getString(R.string.timer_alert_notification_content))
            .setSound(alarmSound)
            .addAction(NotificationCompat.Action.Builder(0, "dismiss", cancelTimerPendingIntent).build())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
    }

    fun createRunningTimerNotification(seconds: Int,minutes: Int,hours: Int): Notification {
        val pauseTimerIntent = Intent(app, TimerNotificationReceiver::class.java).apply {
            action = "ACTION_TIMER_PAUSE"
        }
        val pauseTimerPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            0,
            pauseTimerIntent,
            0
        )
        val cancelTimerIntent = Intent(app, TimerNotificationReceiver::class.java).apply {
            action = "ACTION_TIMER_CANCEL"
        }
        val cancelTimerPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            0,
            cancelTimerIntent,
            0
        )
        val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(
            app, CHANNEL_ID_TIMER)
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContentTitle(app.getString(R.string.title_timer_notification))
            .setContentText(createNotificationTimeFormat(seconds, minutes, hours))
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                NotificationCompat.Action.Builder(
                    0,
                    app.getString(R.string.title_notification_action_cancel),
                    cancelTimerPendingIntent).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    0,
                    app.getString(R.string.title_notification_action_pause),
                    pauseTimerPendingIntent).build()
            )
            .build()
    }

    fun createPausedTimerNotification(seconds: Int,minutes: Int,hours: Int): Notification {
        val resumeTimerIntent = Intent(app, TimerNotificationReceiver::class.java).apply {
            action = "ACTION_TIMER_RESUME"
        }
        val resumeTimerPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            0,
            resumeTimerIntent,
            0
        )
        val cancelTimerIntent = Intent(app, TimerNotificationReceiver::class.java).apply {
            action = "ACTION_TIMER_CANCEL"
        }
        val cancelTimerPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            0,
            cancelTimerIntent,
            0
        )
        val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(
            app, CHANNEL_ID_TIMER)
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setContentTitle(app.getString(R.string.title_timer_notification))
            .setContentText(createNotificationTimeFormat(seconds, minutes, hours))
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                NotificationCompat.Action.Builder(
                    0,
                    app.getString(R.string.title_notification_action_cancel),
                    cancelTimerPendingIntent).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    0,
                    app.getString(R.string.title_notification_action_resume),
                    resumeTimerPendingIntent).build()
            )
            .build()
    }
    
    fun createTimerAlertNotificationChannel() {
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
            val alarmSound: Uri = Settings.System.DEFAULT_NOTIFICATION_URI//RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            channel.setSound(alarmSound, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createTimerNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, importance).apply {
                description = CHANNEL_DESCRIPTION_TIMER
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = app
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationTimeFormat(seconds: Int,minutes: Int,hours: Int): String {
        val secondsFormat = if (seconds >= 10) seconds.toString() else "0".plus(seconds.toString())
        val minutesFormat = if (minutes >= 10) minutes.toString() else "0".plus(minutes.toString())
        val hoursFormat = if (hours >= 10) hours.toString() else "0".plus(hours.toString())

        return "${hoursFormat}:${minutesFormat}:${secondsFormat}"
    }
}
