package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmExecutor
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.usecase.StopAlarmUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class StopAlarmUseCaseTest {

    // Test subject
    private lateinit var useCase: StopAlarmUseCase

    // Collaborators
    private val alarmsRepo: AlarmsRepository = mockk()
    private val alarmExecutor: AlarmExecutor = mockk()

    // Stub data
    private val id = 1
    private val alarm: Alarm = mockk()

    @Before
    fun setUp() {
        // Stub mocks
        every { alarmsRepo.get(id) } returns Single.just(AppResult.Success(alarm))
        every { alarmsRepo.setActive(id,any()) } returns Single.just(AppResult.Success(Unit))
        every { alarmExecutor.stopAlarm() } returns Single.just(AppResult.Success(Unit))
        every { alarmsRepo.setSnoozed(any(),any()) } returns Single.just(AppResult.Success(Unit))

        // Init subject
        useCase = StopAlarmUseCase(alarmsRepo, alarmExecutor)
    }

    @Parameters(method = "stopAlarmParams")
    @Test
    fun stopCurrentAlarm_WhenExecuted(isRepeated: Boolean) {
        // Given

        every { alarm.isRepeated } returns isRepeated

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { alarmExecutor.stopAlarm() }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun setAlarmAsNotActive_WhenExecutedForOneOffAlarm() {
        // Given

        every { alarm.isRepeated } returns false

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { alarmsRepo.setActive(id,false) }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun doNotChangeAlarmActivation_WhenExecutedForRepeatedAlarm() {
        // Given

        every { alarm.isRepeated } returns true

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 0) { alarmsRepo.setActive(id,false) }
        observer.assertValue(AppResult.Success(Unit))
    }

    @Test
    fun setAlarmStateAsNotSnoozed_WhenExecuted() {
        // Given

        every { alarm.isRepeated } returns true

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { alarmsRepo.setSnoozed(id,false) }
        observer.assertValue(AppResult.Success(Unit))
    }

    fun stopAlarmParams() = arrayOf(true,false)
}