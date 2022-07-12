package com.diskin.alon.coolclock.worldclocks.application

import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.application.usecase.CitiesPagingMapper
import com.diskin.alon.coolclock.worldclocks.application.usecase.GetSelectedCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.domain.City
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class GetSelectedCitiesUseCaseTest {

    // Test subject
    private lateinit var useCase: GetSelectedCitiesUseCase

    // Collaborators
    private val  repository: CitiesRepository = mockk()
    private val mapper: CitiesPagingMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetSelectedCitiesUseCase(repository, mapper)
    }

    @Test
    fun getAllUserSelectedCities_WhenExecuted() {
        // Given
        val repoCities = mockk<PagingData<City>>()
        val mappedCities = mockk<PagingData<CityDto>>()

        every { repository.getAllSelected() } returns Observable.just(repoCities)
        every { mapper.map(any()) } returns mappedCities

        // When
        val observer = useCase.execute(Unit).test()

        // Then
        verify(exactly = 1) { repository.getAllSelected() }
        verify(exactly = 1) { mapper.map(repoCities) }
        observer.assertValue(mappedCities)
    }
}