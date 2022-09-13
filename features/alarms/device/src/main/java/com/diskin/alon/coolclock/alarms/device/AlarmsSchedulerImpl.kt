package com.diskin.alon.coolclock.alarms.device

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.Time
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import javax.inject.Inject

class AlarmsSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmsScheduler {

    override fun cancel(alarm: Alarm): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            val alarmIntent = createAlarmPendingIntent(alarm)
            alarmManager.cancel(alarmIntent)
            alarmIntent.cancel()
            it.onSuccess(Unit)
        }
            .subscribeOn(Schedulers.computation())
            .toSingleAppResult()
    }

    override fun schedule(alarm: Alarm): Single<AppResult<Unit>> {
        return when(alarm.repeatDays.isEmpty()) {
            true -> scheduleUnrepeatedAlarm(alarm)
            false -> scheduleRepeatedAlarm(alarm)
        }
    }

    private fun createAlarmPendingIntent(alarm: Alarm): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(ALARM_ID,alarm.id)
            PendingIntent.getBroadcast(context, alarm.id, intent, 0)
        }
    }

    private fun scheduleUnrepeatedAlarm(alarm: Alarm): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarm.nextAlarm(),
                createAlarmPendingIntent(alarm)
            )

            it.onSuccess(Unit)
        }
            .subscribeOn(Schedulers.computation())
            .toSingleAppResult()
    }

    private fun scheduleRepeatedAlarm(alarm: Alarm): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            val weekMillisInterval = 1000L * 60 * 60 * 24 * 7
            alarm.repeatDays.forEach { day ->
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    getNextRepeatedAlarm(day,alarm.time),
                    weekMillisInterval,
                    createAlarmPendingIntent(alarm)
                )
            }

            it.onSuccess(Unit)
        }
            .subscribeOn(Schedulers.computation())
            .toSingleAppResult()
    }

    private fun getNextRepeatedAlarm(day: WeekDay, time: Time): Long {
        val current = DateTime()
        val repeatDay = when(day) {
            WeekDay.MON -> 1
            WeekDay.TUE -> 2
            WeekDay.WED -> 3
            WeekDay.THU -> 4
            WeekDay.FRI -> 5
            WeekDay.SUT -> 6
            WeekDay.SUN -> 7
        }
        val tempAlarmDate = current.withDayOfWeek(repeatDay)
            .withHourOfDay(time.hour)
            .withMinuteOfHour(time.minute)

        return if (tempAlarmDate.millis > current.millis) {
            tempAlarmDate.millis
        } else {
            tempAlarmDate.plusDays(7).millis
        }
    }
}