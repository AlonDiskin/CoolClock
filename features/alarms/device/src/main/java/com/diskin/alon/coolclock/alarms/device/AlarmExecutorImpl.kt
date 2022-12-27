package com.diskin.alon.coolclock.alarms.device

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.NO_CURRENT_ALARM
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@VisibleForTesting
const val KEY_CURRENT_ALARM_ID = "alarm_id"

class AlarmExecutorImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val sharedPreferences: SharedPreferences
) : AlarmExecutor {

    override fun startAlarm(alarm: Alarm): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            val deviceAlarm = DeviceAlarm(
                alarm.id,
                alarm.isVibrate,
                when(val sound = alarm.sound) {
                    is Sound.AlarmSound -> sound.path
                    else -> ""
                },
                alarm.duration,
                alarm.volume,
                alarm.snooze != 0,
                alarm.name
            )
            val serviceIntent = Intent(appContext,AlarmService::class.java).also { intent ->
                intent.putExtra(ALARM_REQUEST,deviceAlarm)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }

            sharedPreferences.edit()
                .putInt(
                    KEY_CURRENT_ALARM_ID,
                    deviceAlarm.id
                )
                .apply()
            it.onSuccess(Unit)
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .toSingleAppResult()
    }

    override fun stopAlarm(): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            appContext.stopService(Intent(appContext,AlarmService::class.java))
            sharedPreferences.edit()
                .putInt(
                    KEY_CURRENT_ALARM_ID,
                    NO_CURRENT_ALARM
                )
                .apply()
            it.onSuccess(Unit)
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .toSingleAppResult()
    }

    override fun currentAlarm(): Single<AppResult<Int>> {
        return Single.create<Int> {
            it.onSuccess(
                sharedPreferences.getInt(
                    KEY_CURRENT_ALARM_ID,
                    NO_CURRENT_ALARM
                )
            )
        }
            .subscribeOn(Schedulers.io())
            .toSingleAppResult()
    }
}