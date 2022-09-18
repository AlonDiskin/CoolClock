package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.usecase.DeleteAlarmUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class DeleteAlarmUseCaseTest {

    // Test subject
    private lateinit var useCase: DeleteAlarmUseCase

    // Collaborators
    private val alarmsRepo: AlarmsRepository = mockk()
    private val alarmsScheduler: AlarmsScheduler = mockk()

    @Before
    fun setUp() {
        useCase = DeleteAlarmUseCase(alarmsRepo, alarmsScheduler)
    }

    @Test
    fun deleteAlarmFromStorageAndCancelActivation_WhenExecuted() {
        // Given
        val id = 1
        val alarm = mockk<Alarm>()

        every { alarmsRepo.get(id) } returns Single.just(AppResult.Success(alarm))
        every { alarmsScheduler.cancel(alarm) } returns Single.just(AppResult.Success(Unit))
        every { alarmsRepo.delete(id) } returns Single.just(AppResult.Success(Unit))

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify(exactly = 1) { alarmsRepo.get(id) }
        verify(exactly = 1) { alarmsScheduler.cancel(alarm) }
        verify(exactly = 1) { alarmsRepo.delete(id) }
        observer.assertValue(AppResult.Success(Unit))
    }
}