package com.diskin.alon.coolclock.alarms.device

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.domain.Alarm
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

    override fun schedule(alarm: Alarm): Single<AppResult<Long>> {
        return when(alarm.repeatDays.isEmpty()) {
            true -> scheduleUnrepeatedAlarm(alarm)
            false -> scheduleRepeatedAlarm(alarm)
        }
    }

    override fun cancel(alarm: Alarm): Single<AppResult<Unit>> {
        return Single.create<Unit> {
            when(alarm.repeatDays.isEmpty()) {
                true -> createUnrepeatedAlarmCancelingPendingIntent(alarm)
                    ?.let(alarmManager::cancel)

                false -> alarm.repeatDays.forEach { day ->
                    createRepeatedAlarmCancelPendingIntent(alarm,day.name)
                        ?.let(alarmManager::cancel)
                }
            }
            it.onSuccess(Unit)
        }
            .subscribeOn(Schedulers.computation())
            .toSingleAppResult()
    }

    private fun scheduleUnrepeatedAlarm(alarm: Alarm): Single<AppResult<Long>> {
        return Single.create<Long> {
            val alarmTime = alarm.nextAlarm()

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                createUnrepeatedAlarmPendingIntent(alarm)
            )

            it.onSuccess(alarmTime)
        }
            .subscribeOn(Schedulers.computation())
            .toSingleAppResult()
    }

    private fun scheduleRepeatedAlarm(alarm: Alarm): Single<AppResult<Long>> {
        return Single.create<Long> {
            val weekMillisInterval = 1000L * 60 * 60 * 24 * 7
            alarm.repeatDays.forEach { day ->
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    getNextRepeatedAlarm(day,alarm.hour,alarm.minute),
                    weekMillisInterval,
                    createRepeatedAlarmPendingIntent(alarm,day.name)
                )
            }

            it.onSuccess(alarm.nextAlarm())
        }
            .subscribeOn(Schedulers.computation())
            .toSingleAppResult()
    }

    private fun createUnrepeatedAlarmPendingIntent(alarm: Alarm): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(ALARM_ID,alarm.id)
            PendingIntent.getBroadcast(context, alarm.id, intent, 0)
        }
    }

    private fun createUnrepeatedAlarmCancelingPendingIntent(alarm: Alarm): PendingIntent? {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(ALARM_ID,alarm.id)
            PendingIntent.getBroadcast(context, alarm.id, intent, PendingIntent.FLAG_NO_CREATE)
        }
    }

    private fun createRepeatedAlarmCancelPendingIntent(alarm: Alarm,day: String): PendingIntent? {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM
            intent.addCategory(day)

            intent.putExtra(ALARM_ID,alarm.id)
            PendingIntent.getBroadcast(context, alarm.id, intent, PendingIntent.FLAG_NO_CREATE)
        }
    }

    private fun createRepeatedAlarmPendingIntent(alarm: Alarm,day: String): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM
            intent.addCategory(day)

            intent.putExtra(ALARM_ID,alarm.id)
            PendingIntent.getBroadcast(context, alarm.id, intent, 0)
        }
    }

    private fun getNextRepeatedAlarm(day: WeekDay, hour: Int,minute: Int): Long {
        val current = DateTime()
        val repeatDay = when(day) {
            WeekDay.MON -> 1
            WeekDay.TUE -> 2
            WeekDay.WED -> 3
            WeekDay.THU -> 4
            WeekDay.FRI -> 5
            WeekDay.SAT -> 6
            WeekDay.SUN -> 7
        }
        val tempAlarmDate = current.withDayOfWeek(repeatDay)
            .withHourOfDay(hour)
            .withMinuteOfHour(minute)

        return if (tempAlarmDate.millis > current.millis) {
            tempAlarmDate.millis
        } else {
            tempAlarmDate.plusDays(7).millis
        }
    }
}