package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.usecase.SnoozeAlarmUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class SnoozeAlarmUseCaseTest {

    // Test subject
    private lateinit var useCase: SnoozeAlarmUseCase

    // Collaborators
    private val repository: AlarmsRepository = mockk()
    private val scheduler: AlarmsScheduler = mockk()
    private val alarmExecutor: AlarmExecutor = mockk()

    // Stub data
    private val id = 1
    private val alarm: Alarm = mockk()

    @Before
    fun setUp() {
        // Stub mocks
        every { repository.get(id) } returns Single.just(AppResult.Success(alarm))
        every { scheduler.scheduleSnooze(any()) } returns Single.just(AppResult.Success(Unit))
        every { alarmExecutor.stopAlarm() } returns Single.just(AppResult.Success(Unit))
        every { repository.setSnoozed(any(),any()) } returns Single.just(AppResult.Success(Unit))

        // Init subject
        useCase = SnoozeAlarmUseCase(repository, scheduler, alarmExecutor)
    }

    @Test
    fun stopCurrentAlarm_WhenExecuted() {
        // Given

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { alarmExecutor.stopAlarm() }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun scheduleCurrentAlarmToSnooze_WhenExecuted() {
        // Given

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { scheduler.scheduleSnooze(alarm) }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun setAlarmStateAsSnoozed_WhenExecuted() {
        // Given

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { repository.setSnoozed(id,true) }
        observer.assertValue(AppResult.Success(Unit))
    }
}