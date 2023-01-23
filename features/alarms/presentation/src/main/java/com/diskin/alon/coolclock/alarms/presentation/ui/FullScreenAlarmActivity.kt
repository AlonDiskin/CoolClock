package com.diskin.alon.coolclock.alarms.presentation.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.databinding.ActivityAlarmBinding
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class FullScreenAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set layout and binding
        binding = DataBindingUtil.setContentView(this,R.layout.activity_alarm)

        // turn screen on
        val pm = (getSystemService(Context.POWER_SERVICE) as PowerManager)
        val levelAndFlags = PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP
        val wakeLock = pm.newWakeLock(levelAndFlags, "CoolClockApp::MyWakelockTag")

        wakeLock.acquire()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        wakeLock.release()

        // Observe view model data
        viewModel.alarmData.observe(this) { binding.alarm = it }
        viewModel.alarmStopped.observe(this) { if (it) finish() }

        // Set snooze and dismiss buttons listeners
        binding.buttonSnooze.setOnSlidingListener {
            when {
                it == 1.0f -> viewModel.snooze()
                it > 0.0f && it < 1.0f -> binding.buttonDismiss.isEnabled = false
                it == 0.0f -> binding.buttonDismiss.isEnabled = true
            }
        }
        binding.buttonDismiss.setOnSlidingListener {
            when {
                it == 1.0f -> viewModel.dismiss()
                it > 0.0f && it < 1.0f -> binding.buttonSnooze.isEnabled = false
                it == 0.0f -> binding.buttonSnooze.isEnabled = true
            }
        }
    }
}