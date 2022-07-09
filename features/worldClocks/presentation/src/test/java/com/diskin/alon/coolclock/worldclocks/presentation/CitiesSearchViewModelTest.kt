package com.diskin.alon.coolclock.worldclocks.presentation

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.worldclocks.application.usecase.AddCityUseCase
import com.diskin.alon.coolclock.worldclocks.application.usecase.SearchCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CitiesSearchViewModel
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.KEY_SEARCH_QUERY
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.KEY_SEARCH_TEXT
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.SearchResultsMapper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class CitiesSearchViewModelTest {

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: CitiesSearchViewModel

    // Collaborators
    private val searchUseCase: SearchCitiesUseCase = mockk()
    private val addCitiesUseCase: AddCityUseCase = mockk()
    private val savedState = SavedStateHandle()
    private val resultsMapper: SearchResultsMapper = mockk()

    @Before
    fun setUp() {
        viewModel = CitiesSearchViewModel(searchUseCase, addCitiesUseCase,savedState,resultsMapper)
    }

    @Test
    fun restoreSearchTextFromPrevState_WhenCreatedAndStateExist() {
        // Given
        val searchText = "text"
        savedState.set(KEY_SEARCH_TEXT,searchText)

        // When
        viewModel = CitiesSearchViewModel(searchUseCase, addCitiesUseCase, savedState,resultsMapper)

        // Then
        assertThat(viewModel.searchText).isEqualTo(searchText)
    }

    @Test
    fun restoreCitiesSearchPrevState_WhenCreatedStateExist() {
        // Given
        val searchQuery = "query"
        savedState.set(KEY_SEARCH_QUERY,searchQuery)

        every { searchUseCase.execute(any()) } returns Observable.just(PagingData.empty())
        every { resultsMapper.map(any()) } returns PagingData.empty()

        // When
        viewModel = CitiesSearchViewModel(searchUseCase, addCitiesUseCase, savedState,resultsMapper)

        // Then
        verify { searchUseCase.execute(searchQuery) }
    }

    @Test
    fun searchModelCities_WhenSearchPerformed() {
        // Given
        val query = "query"
        val results = PagingData.from(createCityDtoSearchResults())
        val mappedResults = PagingData.from(createUiCitySearchResults())

        every { searchUseCase.execute(any()) } returns Observable.just(results)
        every { resultsMapper.map(any()) } returns mappedResults

        // When
        viewModel.search(query)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { searchUseCase.execute(query) }
        verify { resultsMapper.map(any()) }
        assertThat(viewModel.results.value).isEqualTo(mappedResults)
    }

    @Test
    fun addCityToUserClocks_WhenCityAdded() {
        // Given
        val city = createUiCitySearchResults().first()

        every { addCitiesUseCase.execute(any()) } returns Single.just(AppResult.Success(Unit))

        // When
        viewModel.addCity(city)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { addCitiesUseCase.execute(city.id) }
        assertThat(viewModel.addedCity.value).isEqualTo(city.name)
    }
}