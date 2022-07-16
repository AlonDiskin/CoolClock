package com.diskin.alon.coolclock.worldclocks.application

import androidx.paging.PagingData
import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.application.usecase.CitiesPagingMapper
import com.diskin.alon.coolclock.worldclocks.application.usecase.SearchCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.domain.City
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class SearchCitiesUseCaseTest {

    // Test subject
    private lateinit var useCase: SearchCitiesUseCase

    // Collaborators
    private val repository: CitiesRepository = mockk()
    private val mapper: CitiesPagingMapper = mockk()

    @Before
    fun setUp() {
        useCase = SearchCitiesUseCase(repository, mapper)
    }

    @Test
    fun searchForCities_WhenExecuted() {
        // Given
        val query = "query"
        val repoResults = mockk<PagingData<City>>()
        val mappedResults = mockk<PagingData<CityDto>>()

        every { repository.search(any()) } returns Observable.just(repoResults)
        every { mapper.map(any()) } returns mappedResults

        // When
        val observer = useCase.execute(query).test()

        // Then
        verify(exactly = 1) { repository.search(query) }
        verify(exactly = 1) { mapper.map(repoResults) }
        observer.assertValue(mappedResults)
    }
}