package com.diskin.alon.coolclock.worldclocks.application

import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.usecase.UnSelectCityUseCase
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class UnSelectCityUseCaseTest {

    // Test subject
    private lateinit var useCase: UnSelectCityUseCase

    // Collaborators
    private val repository: CitiesRepository = mockk()

    @Before
    fun setUp() {
        useCase = UnSelectCityUseCase(repository)
    }

    @Test
    fun unSelectCityFromUserCities_WhenExecuted() {
        // Given
        val result = mockk<Single<AppResult<Unit>>>()

        every { repository.removeFromSelected(any()) } returns result

        // When
        val id = 1L
        val actual = useCase.execute(id)

        // Then
        verify(exactly = 1) { repository.removeFromSelected(id) }
        assertThat(actual).isEqualTo(result)
    }
}