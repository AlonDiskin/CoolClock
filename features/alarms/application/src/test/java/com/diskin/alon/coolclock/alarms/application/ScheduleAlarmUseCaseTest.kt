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
import io.reactivex.Maybe
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
    fun scheduleNewAlarm_WhenExecutedAndScheduledAlarmForSameTimeNotExist() {
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

        every { repo.getWithNextAlarm(any()) } returns Maybe.empty()
        every { scheduler.schedule(expectedScheduledAlarm) } returns Single.just(AppResult.Success(next))
        every { repo.add(expectedAddedAlarm) } returns Single.just(AppResult.Success(id))

        // When
        val observer = useCase.execute(request).test()

        // Then
        observer.assertValue(AppResult.Success(next))
    }

    @Test
    fun scheduleNewAlarmAndReplaceExisting_WhenExecutedAndScheduledAlarmForSameTimeExist() {
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
        val existingAlarm = mockk<Alarm>()
        val existingAlarmId = 10
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

        every { repo.getWithNextAlarm(any()) } returns Maybe.just(AppResult.Success(existingAlarm))
        every { existingAlarm.isScheduled } returns true
        every { existingAlarm.nextAlarm } returns expectedScheduledAlarm.nextAlarm
        every { existingAlarm.id } returns existingAlarmId
        every { scheduler.cancel(existingAlarm) } returns Single.just(AppResult.Success(Unit))
        every { repo.delete(existingAlarmId) } returns Single.just(AppResult.Success(Unit))
        every { scheduler.schedule(expectedScheduledAlarm) } returns Single.just(AppResult.Success(next))
        every { repo.add(expectedAddedAlarm) } returns Single.just(AppResult.Success(id))

        // When
        val observer = useCase.execute(request).test()

        // Then
        observer.assertValue(AppResult.Success(next))
    }

    @Test
    fun rescheduleExistingAlarm_WhenExecutedToUpdateAndAlarmForSameTimeNotExist() {
        // Given
        val request = ScheduleAlarmRequest.UpdateAlarm(
            1,
            12,
            15,
            emptySet(),
            "name",
            AlarmSound.Silent,
            true,
            5,
            5,
            10)
        val updatedAlarm = Alarm(
            request.id,
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
            true,
            request.vibration,
            when(val ringtone = request.ringtone) {
                is AlarmSound.Ringtone -> Sound.AlarmSound(ringtone.path)
                else -> Sound.Silent
            },
            request.duration,
            request.volume,
            request.snooze,
            false)

        every { repo.getWithNextAlarm(updatedAlarm.nextAlarm) } returns Maybe.empty()
        every { scheduler.schedule(updatedAlarm) } returns Single.just(AppResult.Success(updatedAlarm.nextAlarm))
        every { repo.update(updatedAlarm) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 1) { repo.getWithNextAlarm(updatedAlarm.nextAlarm) }
        verify(exactly = 1) { scheduler.schedule(updatedAlarm) }
        verify(exactly = 1) { repo.update(updatedAlarm) }
        observer.assertValue(AppResult.Success(updatedAlarm.nextAlarm))
    }

    @Test
    fun rescheduleExistingAlarmAndDeleteOther_WhenExecutedToUpdateAndAlarmForSameTimeExist() {
        // Given
        val request = ScheduleAlarmRequest.UpdateAlarm(
            1,
            12,
            15,
            emptySet(),
            "name",
            AlarmSound.Silent,
            true,
            5,
            5,
            10)
        val sameTriggerTimeAlarm = mockk<Alarm>()
        val sameTriggerTimeAlarmId = 2
        val updatedAlarm = Alarm(
            request.id,
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
            true,
            request.vibration,
            when(val ringtone = request.ringtone) {
                is AlarmSound.Ringtone -> Sound.AlarmSound(ringtone.path)
                else -> Sound.Silent
            },
            request.duration,
            request.volume,
            request.snooze,
            false)

        every { sameTriggerTimeAlarm.id } returns sameTriggerTimeAlarmId
        every { sameTriggerTimeAlarm.isScheduled } returns true
        every { repo.getWithNextAlarm(updatedAlarm.nextAlarm) } returns Maybe.just(AppResult.Success(sameTriggerTimeAlarm))
        every { repo.delete(sameTriggerTimeAlarmId) } returns Single.just(AppResult.Success(Unit))
        every { scheduler.cancel(sameTriggerTimeAlarm) } returns Single.just(AppResult.Success(Unit))
        every { scheduler.schedule(updatedAlarm) } returns Single.just(AppResult.Success(updatedAlarm.nextAlarm))
        every { repo.update(updatedAlarm) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 1) { repo.getWithNextAlarm(updatedAlarm.nextAlarm) }
        verify(exactly = 1) { repo.delete(sameTriggerTimeAlarmId) }
        verify(exactly = 1) { scheduler.schedule(updatedAlarm) }
        verify(exactly = 1) { repo.update(updatedAlarm) }
        observer.assertValue(AppResult.Success(updatedAlarm.nextAlarm))
    }
}