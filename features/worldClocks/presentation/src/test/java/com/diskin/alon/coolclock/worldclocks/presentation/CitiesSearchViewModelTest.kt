package com.diskin.alon.coolclock.worldclocks.presentation

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.worldclocks.application.usecase.SearchCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CitiesSearchViewModel
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.KEY_SEARCH_QUERY
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.KEY_SEARCH_TEXT
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.SearchResultsMapper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
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
    private val savedState = SavedStateHandle()
    private val resultsMapper: SearchResultsMapper = mockk()

    @Before
    fun setUp() {
        viewModel = CitiesSearchViewModel(searchUseCase, savedState,resultsMapper)
    }

    @Test
    fun restoreSearchTextFromPrevState_WhenCreatedAndStateExist() {
        // Given
        val searchText = "text"
        savedState.set(KEY_SEARCH_TEXT,searchText)

        // When
        viewModel = CitiesSearchViewModel(searchUseCase, savedState,resultsMapper)

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
        viewModel = CitiesSearchViewModel(searchUseCase, savedState,resultsMapper)

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
}