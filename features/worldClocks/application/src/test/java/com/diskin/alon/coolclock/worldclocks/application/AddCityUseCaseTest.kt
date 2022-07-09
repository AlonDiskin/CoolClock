package com.diskin.alon.coolclock.worldclocks.application

import com.diskin.alon.coolclock.worldclocks.application.interfaces.CitiesRepository
import com.diskin.alon.coolclock.worldclocks.application.usecase.AddCityUseCase
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class AddCityUseCaseTest {

    // Test subject
    private lateinit var useCase: AddCityUseCase

    // Collaborators
    private val repository: CitiesRepository = mockk()

    @Before
    fun setUp() {
        useCase = AddCityUseCase(repository)
    }

    @Test
    fun addCityToUserSelected_WhenExecuted() {
        // Given
        val result = mockk<Single<AppResult<Unit>>>()

        every { repository.addToSelected(any()) } returns result

        // When
        val id = 1L
        val actual = useCase.execute(id)

        // Then
        verify(exactly = 1) { repository.addToSelected(id) }
        assertThat(actual).isEqualTo(result)
    }
}