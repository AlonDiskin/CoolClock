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
import org.joda.time.DateTimeUtils
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class AlarmsSchedulerImplTest(
    private val currentDate: DateTime,
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
                    Time(16,45),
                    setOf(WeekDay.SUN,WeekDay.TUE,WeekDay.FRI),
                    true,
                    true,
                    Sound.Ringtone("sound_1"),
                    Duration(1),
                    Volume(5),
                    Snooze.None
                ),
                listOf(
                    DateTime(2022,8,30,16,45),
                    DateTime(2022,8,26,16,45),
                    DateTime(2022,8,28,16,45)
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
    private val alarmManager: AlarmManager = mockk()
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        scheduler = AlarmsSchedulerImpl(context,alarmManager)
    }

    @Test
    fun scheduleRepeatedAlarm() {
        // Given
        val intentSlot = slot<Intent>()
        val requestCodeSlot = slot<Int>()
        val pendingIntent = mockk<PendingIntent>()
        val weekMillis = 1000L * 60 * 60 * 24 * 7

        mockkStatic(PendingIntent::class)
        every { alarmManager.setRepeating(any(),any(),any(),any()) } returns Unit
        every { PendingIntent.getBroadcast(any(),capture(requestCodeSlot),capture(intentSlot),any()) } returns pendingIntent
        DateTimeUtils.setCurrentMillisFixed(currentDate.millis)

        // When
        val observer = scheduler.schedule(repeatedAlarm).test()

        // Then
        expectedRepeatedAlarmDates.forEach {
            verify(exactly = 1) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    it.millis,
                    weekMillis,
                    pendingIntent
                )
                assertThat(requestCodeSlot.captured).isEqualTo(repeatedAlarm.id)
                assertThat(intentSlot.captured.action).isEqualTo(ACTION_ALARM)
                assertThat(intentSlot.captured.getIntExtra(ALARM_ID,-1)).isEqualTo(repeatedAlarm.id)
            }
        }
    }

    @Test
    fun cancelScheduledAlarm() {
        // Given
        val id = 1
        val alarm = mockk<Alarm>()
        val intentSlot = slot<Intent>()
        val requestCodeSlot = slot<Int>()
        val pendingIntent = mockk<PendingIntent>()

        mockkStatic(PendingIntent::class)
        every { alarm.id } returns id
        every { alarmManager.cancel(any<PendingIntent>()) } returns Unit
        every { PendingIntent.getBroadcast(any(),capture(requestCodeSlot),capture(intentSlot),any()) } returns pendingIntent
        every { pendingIntent.cancel() } returns Unit

        // When
        val observer = scheduler.cancel(alarm).test()

        // Then
        observer.assertValue(AppResult.Success(Unit))
        verify(exactly = 1) { alarmManager.cancel(pendingIntent) }
        assertThat(requestCodeSlot.captured).isEqualTo(alarm.id)
        assertThat(intentSlot.captured.action).isEqualTo(ACTION_ALARM)
        assertThat(intentSlot.captured.getIntExtra(ALARM_ID,-1)).isEqualTo(alarm.id)
    }

    @Test
    fun scheduleNewUnrepeatedAlarm() {
        val id = 1
        val nextAlarm = 10L
        val alarm = mockk<Alarm>()
        val intentSlot = slot<Intent>()
        val requestCodeSlot = slot<Int>()
        val pendingIntent = mockk<PendingIntent>()

        mockkStatic(PendingIntent::class)
        every { alarm.id } returns id
        every { alarm.nextAlarm() } returns nextAlarm
        every { alarm.repeatDays } returns emptySet()
        every { alarmManager.setExact(any(),any(),any()) } returns Unit
        every { PendingIntent.getBroadcast(any(),capture(requestCodeSlot),capture(intentSlot),any()) } returns pendingIntent

        // When
        val observer = scheduler.schedule(alarm).test()

        // Then
        observer.assertValue(AppResult.Success(Unit))
        verify(exactly = 1) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextAlarm,
                pendingIntent
            )
        }
        assertThat(requestCodeSlot.captured).isEqualTo(id)
        assertThat(intentSlot.captured.action).isEqualTo(ACTION_ALARM)
        assertThat(intentSlot.captured.getIntExtra(ALARM_ID,-1)).isEqualTo(id)
    }
}