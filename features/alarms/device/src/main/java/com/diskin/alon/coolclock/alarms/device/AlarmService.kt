package com.diskin.alon.coolclock.alarms.device

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import javax.inject.Inject

const val NOTIFICATION_ID_ALARM = 100
const val ALARM_REQUEST = "alarm request"

@AndroidEntryPoint
@OptionalInject
class AlarmService : Service() {

    @Inject
    lateinit var ringtonePlayer: AlarmRingtonePlayer
    @Inject
    lateinit var vibrationManager: AlarmVibrationManager
    @Inject
    lateinit var notificationFactory: AlarmNotificationFactory
    @Inject
    lateinit var notificationManager: NotificationManagerCompat
    @VisibleForTesting
    lateinit var countDownTimer: CountDownTimer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Get alarm data from service intent
        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(ALARM_REQUEST,DeviceAlarm::class.java)
        } else {
            intent.getSerializableExtra(ALARM_REQUEST) as DeviceAlarm
        }!!

        // Start requested alarm
        startAlarm(alarm)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    private fun startAlarm(alarm: DeviceAlarm) {
        if (this::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        // Start alarm duration countdown
        countDownTimer = object : CountDownTimer(alarm.duration * 60000L, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                // Stop service when finished
                stopForeground(true)
                // Stop alarm
                stopAlarm()
            }

        }.start()

        // Start alarm vibration
        if (alarm.isVibrate) {
            vibrationManager.startDeviceVibration()
        }

        // Start alarm ringtone
        if (alarm.ringtone.isNotEmpty()) {
            ringtonePlayer.play(alarm.ringtone,alarm.volume)
        }

        // Show alarm notification
        startForeground(
            NOTIFICATION_ID_ALARM,
            notificationFactory.createAlarmNotification(alarm)
        )
    }

    private fun stopAlarm() {
        if (this::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        ringtonePlayer.stop()
        vibrationManager.stopDeviceVibration()
        notificationManager.cancel(NOTIFICATION_ID_ALARM)
    }
}