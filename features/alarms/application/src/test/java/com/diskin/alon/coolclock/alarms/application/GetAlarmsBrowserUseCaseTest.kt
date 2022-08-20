package com.diskin.alon.coolclock.alarms.application

import androidx.paging.PagingData
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.model.BrowserAlarm
import com.diskin.alon.coolclock.alarms.application.usecase.AlarmsMapper
import com.diskin.alon.coolclock.alarms.application.usecase.GetAlarmsBrowserUseCase
import com.diskin.alon.coolclock.alarms.domain.Alarm
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class GetAlarmsBrowserUseCaseTest {

    // Test subject
    private lateinit var useCase: GetAlarmsBrowserUseCase

    // Collaborators
    private val alarmsRepo: AlarmsRepository = mockk()
    private val alarmsMapper: AlarmsMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetAlarmsBrowserUseCase(alarmsRepo, alarmsMapper)
    }

    @Test
    fun getAllAlarms_WhenExecuted() {
        // Given
        val repoAlarms = PagingData.empty<Alarm>()
        val createdAlarms = PagingData.empty<BrowserAlarm>()

        every { alarmsRepo.getAll() } returns Observable.just(repoAlarms)
        every { alarmsMapper.map(any()) } returns createdAlarms

        // When
        val observer = useCase.execute(Unit).test()

        // Then
        verify(exactly = 1) { alarmsRepo.getAll() }
        verify(exactly = 1) { alarmsMapper.map(repoAlarms) }
        observer.assertValue(createdAlarms)
    }
}