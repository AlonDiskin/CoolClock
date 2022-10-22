package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.model.AlarmActivation
import com.diskin.alon.coolclock.alarms.application.model.NextAlarm
import com.diskin.alon.coolclock.alarms.application.usecase.SetAlarmActivationUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class SetAlarmActivationUseCaseTest {

    // Test subject
    private lateinit var useCase: SetAlarmActivationUseCase

    // Collaborators
    private val alarmsRepo: AlarmsRepository = mockk()
    private val alarmsScheduler: AlarmsScheduler = mockk()

    @Before
    fun setUp() {
        useCase = SetAlarmActivationUseCase(alarmsRepo, alarmsScheduler)
    }

    @Test
    fun scheduleAlarm_WhenExecutedToActivate() {
        // Given
        val request = AlarmActivation(1,true)
        val alarm = mockk<Alarm>()
        val next = 12345L

        every { alarm.nextAlarm() } returns next
        every { alarmsRepo.get(any()) } returns Single.just(AppResult.Success(alarm))
        every { alarmsScheduler.schedule(any()) } returns Single.just(AppResult.Success(next))
        every { alarmsRepo.setActive(any(),any()) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 1) { alarmsRepo.get(request.alarmId) }
        verify(exactly = 1) { alarmsScheduler.schedule(alarm) }
        verify(exactly = 1) { alarmsRepo.setActive(request.alarmId,request.activation) }
        observer.assertValue(AppResult.Success(NextAlarm.Next(next)))
    }

    @Test
    fun cancelAlarm_WhenExecutedToDeActivate() {
        // Given
        val request = AlarmActivation(1,false)
        val alarm = mockk<Alarm>()

        every { alarmsRepo.get(any()) } returns Single.just(AppResult.Success(alarm))
        every { alarmsScheduler.cancel(any()) } returns Single.just(AppResult.Success(Unit))
        every { alarmsRepo.setActive(any(),any()) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 1) { alarmsRepo.get(request.alarmId) }
        verify(exactly = 1) { alarmsScheduler.cancel(alarm) }
        verify(exactly = 1) { alarmsRepo.setActive(request.alarmId,request.activation) }
        observer.assertValue(AppResult.Success(NextAlarm.None))
    }
}