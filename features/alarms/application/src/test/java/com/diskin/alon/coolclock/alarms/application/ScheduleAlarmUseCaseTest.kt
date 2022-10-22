package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.application.model.ScheduleAlarmRequest
import com.diskin.alon.coolclock.alarms.application.usecase.EMPTY_ID
import com.diskin.alon.coolclock.alarms.application.usecase.ScheduleAlarmUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class ScheduleAlarmUseCaseTest {

    // Test subject
    private lateinit var useCase: ScheduleAlarmUseCase

    // Collaborators
    private val repo: AlarmsRepository = mockk()
    private val scheduler: AlarmsScheduler = mockk()

    @Before
    fun setUp() {
        useCase = ScheduleAlarmUseCase(repo, scheduler)
    }

    @Test
    fun scheduleNewAlarm_WhenExecuted() {
        // Given
        val request = ScheduleAlarmRequest.NewAlarm(
            12,
            15,
            emptySet(),
            "name",
            AlarmSound.Silent,
            true,
            5,
            5,
            10)
        val next = 12345L
        val id = 1
        val expectedAddedAlarm = Alarm(
            EMPTY_ID,
            request.name,
            request.hour,
            request.minute,
            request.repeatDays.map {
                when(it) {
                    RepeatDay.SUN -> WeekDay.SUN
                    RepeatDay.MON -> WeekDay.MON
                    RepeatDay.TUE -> WeekDay.TUE
                    RepeatDay.WED -> WeekDay.WED
                    RepeatDay.THU -> WeekDay.THU
                    RepeatDay.FRI -> WeekDay.FRI
                    RepeatDay.SAT -> WeekDay.SAT
                }
            }.toSet(),
            true,request.vibration,
            when(val ringtone = request.ringtone) {
                is AlarmSound.Ringtone -> Sound.AlarmSound(ringtone.path)
                else -> Sound.Silent
            },
            request.duration,
            request.volume,
            request.snooze,
            false)
        val expectedScheduledAlarm = Alarm(
            id,
            request.name,
            request.hour,
            request.minute,
            request.repeatDays.map {
                when(it) {
                    RepeatDay.SUN -> WeekDay.SUN
                    RepeatDay.MON -> WeekDay.MON
                    RepeatDay.TUE -> WeekDay.TUE
                    RepeatDay.WED -> WeekDay.WED
                    RepeatDay.THU -> WeekDay.THU
                    RepeatDay.FRI -> WeekDay.FRI
                    RepeatDay.SAT -> WeekDay.SAT
                }
            }.toSet(),
            true,request.vibration,
            when(val ringtone = request.ringtone) {
                is AlarmSound.Ringtone -> Sound.AlarmSound(ringtone.path)
                else -> Sound.Silent
            },
            request.duration,
            request.volume,
            request.snooze,false)

        every { scheduler.schedule(any()) } returns Single.just(AppResult.Success(next))
        every { repo.add(any()) } returns Single.just(AppResult.Success(id))

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 1) { repo.add(expectedAddedAlarm) }
        verify(exactly = 1) { scheduler.schedule(expectedScheduledAlarm) }
        observer.assertValue(AppResult.Success(next))
    }
}