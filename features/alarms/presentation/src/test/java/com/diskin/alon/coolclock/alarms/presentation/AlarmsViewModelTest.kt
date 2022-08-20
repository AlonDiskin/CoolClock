package com.diskin.alon.coolclock.alarms.presentation

import android.os.Looper
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.application.model.AlarmActivation
import com.diskin.alon.coolclock.alarms.application.model.BrowserAlarm
import com.diskin.alon.coolclock.alarms.application.model.NextAlarm
import com.diskin.alon.coolclock.alarms.application.usecase.GetAlarmsBrowserUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.SetAlarmActivationUseCase
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsMapper
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsViewModel
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.ScheduledAlarmDateFormatter
import com.diskin.alon.coolclock.common.application.AppError
import com.diskin.alon.coolclock.common.application.AppResult
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
import org.robolectric.annotation.LooperMode

// Using android junit test runner due to robolectric incompatibility with paging 'cachedIn' rx operator
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AlarmsViewModelTest {

    // Test subject
    private lateinit var viewModel: AlarmsViewModel

    // Collaborators
    private val getAlarms: GetAlarmsBrowserUseCase = mockk()
    private val alarmsMapper: AlarmsMapper = mockk()
    private val setAlarmActivation: SetAlarmActivationUseCase = mockk()
    private val dateFormatter: ScheduledAlarmDateFormatter = mockk()

    // Stub data
    private val alarmsSubject = BehaviorSubject.create<PagingData<BrowserAlarm>>()

    @Before
    fun setUp() {
        // Stub mock
        every { getAlarms.execute(Unit) } returns alarmsSubject

        // Init subject
        viewModel = AlarmsViewModel(getAlarms,setAlarmActivation,alarmsMapper,dateFormatter)
    }

    @Test
    fun getCreatedAlarmsFromModel_WhenCreated() {
        // Given
        val modelAlarms = mockk<PagingData<BrowserAlarm>>()

        // When
        alarmsSubject.onNext(modelAlarms)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { getAlarms.execute(Unit) }
    }

    @Test
    fun changeAlarmActivationAccordingly_WhenActivationChanged() {
        // Given
        val changes = listOf(true,false)
        val id = 1
        val activationRes: AppResult<NextAlarm> = AppResult.Success(NextAlarm.None)

        every { setAlarmActivation.execute(any()) } returns Single.just(activationRes)

        changes.forEach {
            // When
            viewModel.changeAlarmActivation(id,it)

            // Then
            val expected = AlarmActivation(id,it)
            verify(exactly = 1) { setAlarmActivation.execute(expected) }
        }
    }

    @Test
    fun updateLatestScheduledAlarmDate_WhenAlarmActivated() {
        // Given
        val scheduledDate = 12345L
        val formattedDate = "format date"
        val id = 1
        val activate = true
        val result: AppResult<NextAlarm> = AppResult.Success(NextAlarm.Next(scheduledDate))

        every { setAlarmActivation.execute(AlarmActivation(id,activate)) } returns Single.just(result)
        every { dateFormatter.format(scheduledDate) } returns formattedDate

        // When
        viewModel.changeAlarmActivation(id,activate)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(viewModel.latestScheduledAlarm.value).isEqualTo(formattedDate)
    }

    @Test
    fun updateAlarmActivationError_WhenAlarmActivationFail() {
        // Given
        val error = AppError.UNKNOWN_ERROR

        every { setAlarmActivation.execute(any()) } returns Single.just(AppResult.Error(error))

        // When
        viewModel.changeAlarmActivation(1,false)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(viewModel.alarmActivationError.value).isEqualTo(error)
    }
}