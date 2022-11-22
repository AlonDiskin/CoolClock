package com.diskin.alon.coolclock.alarms.device

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.alarms.domain.*
import com.diskin.alon.coolclock.common.application.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeUtils
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(ParameterizedRobolectricTestRunner::class)
class AlarmsSchedulerImplTest(
    private val currentDate: DateTime,
    private val unrepeatedAlarm: Alarm,
    private val expectedUnrepeatedAlarmDate: DateTime,
    private val repeatedAlarm: Alarm,
    private val expectedRepeatedAlarmDates: List<DateTime>
) {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun testCases() = listOf(
            arrayOf(
                DateTime(2022,8,23,16,45),
                Alarm(
                    1,
                    "alarm_1",
                    16,
                    40,
                    emptySet(),
                    true,
                    true,
                    Sound.AlarmSound("sound_1"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,24,16,40),
                Alarm(
                    1,
                    "alarm_1",
                    16,
                    45,
                    setOf(WeekDay.SUN,WeekDay.TUE,WeekDay.FRI),
                    true,
                    true,
                    Sound.AlarmSound("sound_1"),
                    1,
                    5,
                    0,
                    false
                ),
                listOf(
                    DateTime(2022,8,26,16,45),
                    DateTime(2022,8,28,16,45),
                    DateTime(2022,8,30,16,45)
                )
            ),
            arrayOf(
                DateTime(2022,8,23,16,45),
                Alarm(
                    2,
                    "alarm_1",
                    16,
                    46,
                    emptySet(),
                    true,
                    true,
                    Sound.AlarmSound("sound_1"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,23,16,46),
                Alarm(
                    1,
                    "alarm_1",
                    16,
                    46,
                    setOf(WeekDay.MON,WeekDay.THU),
                    true,
                    true,
                    Sound.AlarmSound("sound_1"),
                    1,
                    5,
                    0,
                    false
                ),
                listOf(
                    DateTime(2022,8,25,16,46),
                    DateTime(2022,8,29,16,46)
                )
            ),
            arrayOf(
                DateTime(2022,8,23,16,45),
                Alarm(
                    3,
                    "alarm_1",
                    16,
                    45,
                    emptySet(),
                    true,
                    true,
                    Sound.AlarmSound("sound_1"),
                    1,
                    5,
                    0,
                    false
                ),
                DateTime(2022,8,24,16,45),
                Alarm(
                    1,
                    "alarm_1",
                    16,
                    46,
                    setOf(WeekDay.TUE),
                    true,
                    true,
                    Sound.AlarmSound("sound_1"),
                    1,
                    5,
                    0,
                    false
                ),
                listOf(
                    DateTime(2022,8,23,16,46)
                )
            )
        )

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var scheduler: AlarmsSchedulerImpl

    // Collaborators
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val alarmManager: AlarmManager = (appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager)

    @Before
    fun setUp() {
        // Set fake current date
        DateTimeUtils.setCurrentMillisFixed(currentDate.millis)

        // Init subject
        scheduler = AlarmsSchedulerImpl(appContext,alarmManager)
    }

    @Test
    fun scheduleUnrepeatedAlarm() {
        // Given
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)

        // When
        val observer = scheduler.schedule(unrepeatedAlarm).test()

        // Then
        val alarmPendingIntent = shadowAlarmManager.scheduledAlarms[0].operation!!
        val alarmIntent = Shadows.shadowOf(alarmPendingIntent).savedIntent

        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(1)
        assertThat(shadowAlarmManager.scheduledAlarms[0].triggerAtTime)
            .isEqualTo(expectedUnrepeatedAlarmDate.millis)
        assertThat(shadowAlarmManager.scheduledAlarms[0].type).isEqualTo(AlarmManager.RTC_WAKEUP)
        observer.assertValue(AppResult.Success(expectedUnrepeatedAlarmDate.millis))
        assertThat(alarmPendingIntent.isBroadcast).isTrue()
        assertThat(alarmIntent.action).isEqualTo(ACTION_ALARM)
        assertThat(alarmIntent.getIntExtra(ALARM_ID,-1)).isEqualTo(unrepeatedAlarm.id)
        assertThat(alarmIntent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)
    }

    @Test
    fun cancelUnrepeatedAlarm() {
        // Given
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        val alarmPendingIntent = Intent(appContext, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM

            intent.putExtra(ALARM_ID,unrepeatedAlarm.id)
            PendingIntent.getBroadcast(appContext, unrepeatedAlarm.id, intent, 0)
        }
        val scheduledAlarm = ShadowAlarmManager.ScheduledAlarm(
            AlarmManager.RTC_WAKEUP,
            unrepeatedAlarm.nextAlarm,
            alarmPendingIntent,
            null
        )

        shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
        // When
        val observer = scheduler.cancel(unrepeatedAlarm).test()

        // Then
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(0)
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun scheduleRepeatedAlarm() {
        // Given
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        val weekMillisInterval = 1000L * 60 * 60 * 24 * 7

        // When
        val observer = scheduler.schedule(repeatedAlarm).test()

        // Then
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(repeatedAlarm.repeatDays.size)
        shadowAlarmManager.scheduledAlarms.forEachIndexed { index, scheduledAlarm ->
            val alarmPendingIntent = scheduledAlarm.operation!!
            val alarmIntent = Shadows.shadowOf(alarmPendingIntent).savedIntent

            assertThat(scheduledAlarm.triggerAtTime).isEqualTo(expectedRepeatedAlarmDates[index].millis)
            assertThat(scheduledAlarm.interval).isEqualTo(weekMillisInterval)
            assertThat(scheduledAlarm.type).isEqualTo(AlarmManager.RTC_WAKEUP)
            assertThat(alarmPendingIntent.isBroadcast).isTrue()

            assertThat(alarmIntent.action).isEqualTo(ACTION_ALARM)
            assertThat(alarmIntent.hasCategory(getWeekDayFromScheduledAlarm(expectedRepeatedAlarmDates[index])
                .name)).isTrue()

            assertThat(alarmIntent.getIntExtra(ALARM_ID,-1)).isEqualTo(repeatedAlarm.id)
            assertThat(alarmIntent.component!!.className).isEqualTo(AlarmReceiver::class.java.name)

        }

        observer.assertValue(AppResult.Success(expectedRepeatedAlarmDates[0].millis))
    }

    @Test
    fun cancelRepeatedAlarm() {
        // Given
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        val weekMillisInterval = 1000L * 60 * 60 * 24 * 7

        expectedRepeatedAlarmDates.forEach { dateTime ->
            val alarmPendingIntent = Intent(appContext, AlarmReceiver::class.java).let { intent ->
                intent.action = ACTION_ALARM
                intent.addCategory(getWeekDayFromScheduledAlarm(dateTime).name)

                intent.putExtra(ALARM_ID,repeatedAlarm.id)
                PendingIntent.getBroadcast(appContext, repeatedAlarm.id, intent, 0)
            }
            val scheduledAlarm = ShadowAlarmManager.ScheduledAlarm(
                AlarmManager.RTC_WAKEUP,
                dateTime.millis,
                weekMillisInterval,
                alarmPendingIntent,
                null
            )

            shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
        }

        // When
        val observer = scheduler.cancel(repeatedAlarm).test()

        // Then
        assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(0)
        observer.assertValue(AppResult.Success(Unit))
    }

    private fun getWeekDayFromScheduledAlarm(date: DateTime): WeekDay {
        return when(date.dayOfWeek) {
            DateTimeConstants.SUNDAY -> WeekDay.SUN
            DateTimeConstants.MONDAY -> WeekDay.MON
            DateTimeConstants.TUESDAY -> WeekDay.TUE
            DateTimeConstants.WEDNESDAY -> WeekDay.WED
            DateTimeConstants.THURSDAY -> WeekDay.THU
            DateTimeConstants.FRIDAY -> WeekDay.FRI
            DateTimeConstants.SATURDAY -> WeekDay.SAT
            else -> throw IllegalArgumentException("Unknown week day arg")
        }
    }
}