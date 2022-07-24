package com.diskin.alon.coolclock.alarms.presentation

import android.os.Looper
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.application.model.CreatedAlarm
import com.diskin.alon.coolclock.alarms.application.usecase.GetCreatedAlarmsUseCase
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsMapper
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

// Using android junit test runner due to robolectric incompatibility with paging 'cachedIn' rx operator
@RunWith(AndroidJUnit4::class)
class AlarmsViewModelTest {

    // Test subject
    private lateinit var viewModel: AlarmsViewModel

    // Collaborators
    private val getAlarms: GetCreatedAlarmsUseCase = mockk()
    private val alarmsMapper: AlarmsMapper = mockk()

    // Stub data
    private val alarmsSubject = BehaviorSubject.create<PagingData<CreatedAlarm>>()

    @Before
    fun setUp() {
        // Stub mock
        every { getAlarms.execute(Unit) } returns alarmsSubject

        // Init subject
        viewModel = AlarmsViewModel(getAlarms,alarmsMapper)
    }

    @Test
    fun getCreatedAlarmsFromModel_WhenCreated() {
        // Given
        val modelAlarms = mockk<PagingData<CreatedAlarm>>()

        // When
        alarmsSubject.onNext(modelAlarms)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { getAlarms.execute(Unit) }
    }
}