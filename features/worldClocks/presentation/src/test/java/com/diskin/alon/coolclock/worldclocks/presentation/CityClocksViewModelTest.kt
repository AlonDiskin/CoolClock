package com.diskin.alon.coolclock.worldclocks.presentation

import android.os.Looper
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.application.usecase.GetSelectedCitiesUseCase
import com.diskin.alon.coolclock.worldclocks.application.usecase.UnSelectCityUseCase
import com.diskin.alon.coolclock.worldclocks.application.util.AppError
import com.diskin.alon.coolclock.worldclocks.application.util.AppResult
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CityClocksMapper
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CityClocksViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

// Using android junit test runner due to robolectric incompatibility with paging 'cachedIn' rx operator
@RunWith(AndroidJUnit4::class)
class CityClocksViewModelTest {

    // Test subject
    private lateinit var viewModel: CityClocksViewModel

    // Collaborators
    private val getSelectedCitiesUseCase: GetSelectedCitiesUseCase = mockk()
    private val unSelectCityUseCase: UnSelectCityUseCase = mockk()
    private val cityClocksMapper: CityClocksMapper = mockk()

    // Stub data
    private val cityClocksSubject = BehaviorSubject.create<PagingData<CityDto>>()

    @Before
    fun setUp() {
        // Stub mock
        every { getSelectedCitiesUseCase.execute(Unit) } returns cityClocksSubject

        // Init subject
        viewModel = CityClocksViewModel(
            getSelectedCitiesUseCase,
            unSelectCityUseCase,
            cityClocksMapper
        )
    }

    @Test
    fun getAllCityClocksFromModel_WhenCreated() {
        // Given
        val modelCityClocks = mockk<PagingData<CityDto>>()

        // When
        cityClocksSubject.onNext(modelCityClocks)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { getSelectedCitiesUseCase.execute(Unit) }
    }

    @Test
    fun deleteCityClockInModel_WhenClockIsDeleted() {
        // Given
        val cityClock = createUiCityClocks().first()

        every { unSelectCityUseCase.execute(any()) } returns Single.just(AppResult.Success(Unit))

        // When
        viewModel.deleteCityClock(cityClock)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { unSelectCityUseCase.execute(cityClock.id) }
    }

    @Test
    fun updateViewDeletionFail_UponDeleteError() {
        // Given
        val cityClock = createUiCityClocks().first()
        val appError = mockk<AppError>()

        every { unSelectCityUseCase.execute(any()) } returns Single.just(AppResult.Error(appError))

        // When
        viewModel.deleteCityClock(cityClock)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(viewModel.deleteCityClockError.value).isEqualTo(appError)
    }
}