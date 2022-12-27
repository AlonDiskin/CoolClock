package com.diskin.alon.coolclock.alarms.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.diskin.alon.coolclock.alarms.application.usecase.SnoozeAlarmUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.StartAlarmUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.StopAlarmUseCase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

const val ACTION_ALARM = "ACTION_ALARM"
const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
const val ACTION_SNOOZE_ALARM = "ACTION_SNOOZE_ALARM"
const val KEY_ALARM_ID = "alarm id"
const val MISSING_ID_ERROR = "must contain alarm id extra value!"

@AndroidEntryPoint
@OptionalInject
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var startAlarm: StartAlarmUseCase

    @Inject
    lateinit var stopAlarm: StopAlarmUseCase

    @Inject
    lateinit var snoozeAlarm: SnoozeAlarmUseCase

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_ALARM -> startAlarm(intent)
            ACTION_STOP_ALARM -> stopAlarm(intent)
            ACTION_SNOOZE_ALARM -> snoozeAlarm(intent)
        }
    }

    private fun startAlarm(intent: Intent) {
        when(intent.hasExtra(KEY_ALARM_ID)) {
            true -> {
                val pendingResult: PendingResult = goAsync()

                @Suppress("CheckResult")
                startAlarm.execute(intent.getIntExtra(KEY_ALARM_ID,0))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        pendingResult.finish()
                    },{
                        println("ALARM START ERROR!")
                        it.printStackTrace()
                        pendingResult.finish()
                    })
            }

            false -> {
                throw IllegalStateException(MISSING_ID_ERROR)
            }
        }
    }

    private fun stopAlarm(intent: Intent) {
        when(intent.hasExtra(KEY_ALARM_ID)) {
            true -> {
                val pendingResult: PendingResult = goAsync()

                @Suppress("CheckResult")
                stopAlarm.execute(intent.getIntExtra(KEY_ALARM_ID,0))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        pendingResult.finish()
                    },{
                        println("ALARM START ERROR!")
                        it.printStackTrace()
                        pendingResult.finish()
                    })
            }

            false -> {
                throw IllegalStateException(MISSING_ID_ERROR)
            }
        }
    }

    private fun snoozeAlarm(intent: Intent) {
        when(intent.hasExtra(KEY_ALARM_ID)) {
            true -> {
                val pendingResult: PendingResult = goAsync()

                @Suppress("CheckResult")
                snoozeAlarm.execute(intent.getIntExtra(KEY_ALARM_ID,0))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        pendingResult.finish()
                    },{
                        println("ALARM START ERROR!")
                        it.printStackTrace()
                        pendingResult.finish()
                    })
            }

            false -> {
                throw IllegalStateException(MISSING_ID_ERROR)
            }
        }
    }
}