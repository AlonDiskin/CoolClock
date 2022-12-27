package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.NO_CURRENT_ALARM
import com.diskin.alon.coolclock.alarms.application.usecase.StartAlarmUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.StopAlarmUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class StartAlarmUseCaseTest {

    // Test subject
    private lateinit var useCase: StartAlarmUseCase

    // Collaborators
    private val alarmsRepo: AlarmsRepository = mockk()
    private val alarmExecutor: AlarmExecutor = mockk()
    private val stopAlarmUseCase: StopAlarmUseCase = mockk()

    @Before
    fun setUp() {
        // Init subject
        useCase = StartAlarmUseCase(alarmsRepo, alarmExecutor,stopAlarmUseCase)
    }

    @Test
    fun stopCurrentAlarm_WhenExecuted() {
        // Given
        val alarmId = 1
        val alarm: Alarm = mockk()
        val currentAlarmId = 2

        every { alarmExecutor.currentAlarm() } returns Single.just(AppResult.Success(currentAlarmId))
        every { stopAlarmUseCase.execute(currentAlarmId) } returns Single.just(AppResult.Success(Unit))
        every { alarmsRepo.get(alarmId) } returns Single.just(AppResult.Success(alarm))
        every { alarmExecutor.startAlarm(alarm) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(alarmId).test()

        // Then
        verify(exactly = 1) { stopAlarmUseCase.execute(currentAlarmId) }
        verify(exactly = 1) { alarmExecutor.startAlarm(alarm) }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun launchAlarmOnDevice_WhenExecuted() {
        // Given
        val alarmId = 1
        val alarm: Alarm = mockk()
        val currentAlarmId = NO_CURRENT_ALARM

        every { alarmExecutor.currentAlarm() } returns Single.just(AppResult.Success(currentAlarmId))
        every { alarmsRepo.get(alarmId) } returns Single.just(AppResult.Success(alarm))
        every { alarmExecutor.startAlarm(alarm) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(alarmId).test()

        // Then
        verify(exactly = 0) { stopAlarmUseCase.execute(any()) }
        verify(exactly = 1) { alarmExecutor.startAlarm(alarm) }
        observer.assertValue(AppResult.Success(Unit))
    }
}