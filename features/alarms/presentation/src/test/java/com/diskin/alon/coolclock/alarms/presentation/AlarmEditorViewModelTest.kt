package com.diskin.alon.coolclock.alarms.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.coolclock.alarms.application.model.AlarmEdit
import com.diskin.alon.coolclock.alarms.application.model.GetEditRequest
import com.diskin.alon.coolclock.alarms.application.model.PlayRingtoneSampleRequest
import com.diskin.alon.coolclock.alarms.application.model.ScheduleAlarmRequest
import com.diskin.alon.coolclock.alarms.application.usecase.GetAlarmEditUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.PlayRingtoneSampleUseCase
import com.diskin.alon.coolclock.alarms.application.usecase.ScheduleAlarmUseCase
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.*
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.presentation.VolumeButtonPressEvent
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class AlarmEditorViewModelTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: AlarmEditorViewModel

    // Collaborators
    private val getAlarmEdit: GetAlarmEditUseCase = mockk()
    private val playRingtone: PlayRingtoneSampleUseCase = mockk()
    private val scheduleAlarm: ScheduleAlarmUseCase = mockk()
    private val alarmEditMapper: UiAlarmEditMapper = mockk()
    private val scheduleRequestMapper: ScheduleAlarmRequestMapper = mockk()
    private val dateFormatter: ScheduledAlarmDateFormatter = mockk()
    private val savedState = SavedStateHandle()
    private val eventBus: EventBus = mockk()

    // Stub data
    private val alarmEdit = mockk<AlarmEdit>()
    private val alarmUiEdit = createAlarmEdit()

    @Before
    fun setUp() {
        // Stub collaborators for default instantiation
        every { getAlarmEdit.execute(any()) } returns Single.just(AppResult.Success(alarmEdit))
        every { alarmEditMapper.map(any()) } returns alarmUiEdit
        every { eventBus.register(any()) } returns Unit
        every { eventBus.unregister(any()) } returns Unit

        // Init subject
        viewModel = AlarmEditorViewModel(
            getAlarmEdit,
            playRingtone,
            scheduleAlarm,
            alarmEditMapper,
            scheduleRequestMapper,
            dateFormatter,
            savedState,
            eventBus
        )
    }

    @Test
    fun getNewAlarmEditFromModel_WhenCreatedWithoutAlarmIdAndSavedEdit() {
        // Given
        val expectedRequest = GetEditRequest.New

        // Then
        verify(exactly = 1) { getAlarmEdit.execute(expectedRequest) }
        assertThat(viewModel.alarmEdit.value).isEqualTo(alarmUiEdit)
    }

    @Test
    fun getExistingAlarmEditFromModel_WhenCreatedWithAlarmId() {
        // Given
        val id = 1
        val expectedRequest = GetEditRequest.Existing(id)
        savedState[KEY_ALARM_ID_ARG] = id
        savedState[KEY_ALARM_EDIT_ARG] = null

        // When
        viewModel = AlarmEditorViewModel(
            getAlarmEdit,
            playRingtone,
            scheduleAlarm,
            alarmEditMapper,
            scheduleRequestMapper,
            dateFormatter,
            savedState,
            eventBus
        )

        // Then
        verify(exactly = 1) { getAlarmEdit.execute(expectedRequest) }
        assertThat(viewModel.alarmEdit.value).isEqualTo(alarmUiEdit)
    }

    @Test
    fun getAlarmEditFromSavedState_WhenCreatedWithSavedEdit() {
        // Given
        val uiEdit = createAlarmEdit()
        savedState[KEY_ALARM_EDIT_ARG] = uiEdit

        // When
        viewModel = AlarmEditorViewModel(
            getAlarmEdit,
            playRingtone,
            scheduleAlarm,
            alarmEditMapper,
            scheduleRequestMapper,
            dateFormatter,
            savedState,
            eventBus
        )

        // Then
        assertThat(viewModel.alarmEdit.value).isEqualTo(uiEdit)
    }

    @Test
    fun playRingtoneSample() {
        // Given
        val ringtonePath = "path"
        val expectedRequest = PlayRingtoneSampleRequest.Ringtone(ringtonePath,alarmUiEdit.volume)

        every { playRingtone.execute(any()) } returns Single.just(AppResult.Success(Unit))

        // When
        viewModel.playRingtoneSample(ringtonePath)

        // Then
        verify(exactly = 1) { playRingtone.execute(expectedRequest) }
    }

    @Test
    fun stopRingtoneSamplePlaying() {
        // Given
        val expectedRequest = PlayRingtoneSampleRequest.Stop

        every { playRingtone.execute(any()) } returns Single.just(AppResult.Success(Unit))

        // When
        viewModel.stopRingtonePlayback()

        // Then
        verify(exactly = 1) { playRingtone.execute(expectedRequest) }
    }

    @Test
    fun addNewAlarm_WhenNewAlarmEditScheduled() {
        // Given
        val nextAlarmDate = 12345L
        val nextAlarmDateFormat = "next alarm format"
        val mappedRequest = mockk<ScheduleAlarmRequest.NewAlarm>()

        every { scheduleRequestMapper.mapNew(any()) } returns mappedRequest
        every { scheduleAlarm.execute(any()) } returns Single.just(AppResult.Success(nextAlarmDate))
        every { dateFormatter.format(nextAlarmDate) } returns nextAlarmDateFormat

        // When
        viewModel.schedule()

        // Then
        verify(exactly = 1) { scheduleRequestMapper.mapNew(viewModel.alarmEdit.value!!) }
        verify(exactly = 1) { scheduleAlarm.execute(mappedRequest) }
        verify(exactly = 1) { dateFormatter.format(nextAlarmDate) }
        assertThat(viewModel.scheduledAlarmDate.value).isEqualTo(nextAlarmDateFormat)
    }

    @Test
    fun updateExistingAlarm_WhenExistingAlarmEditScheduled() {
        // Given
        val id = 1
        val nextAlarmDate = 12345L
        val nextAlarmDateFormat = "next alarm format"
        val mappedRequest = mockk<ScheduleAlarmRequest.UpdateAlarm>()
        savedState[KEY_ALARM_ID_ARG] = id

        every { scheduleRequestMapper.mapUpdate(any(),any()) } returns mappedRequest
        every { scheduleAlarm.execute(any()) } returns Single.just(AppResult.Success(nextAlarmDate))
        every { dateFormatter.format(nextAlarmDate) } returns nextAlarmDateFormat

        // When
        viewModel.schedule()

        // Then
        verify(exactly = 1) { scheduleRequestMapper.mapUpdate(viewModel.alarmEdit.value!!,id) }
        verify(exactly = 1) { scheduleAlarm.execute(mappedRequest) }
        verify(exactly = 1) { dateFormatter.format(nextAlarmDate) }
        assertThat(viewModel.scheduledAlarmDate.value).isEqualTo(nextAlarmDateFormat)
    }

    @Test
    fun registerToEventBus_WhenCreated() {
        // Given

        // Then
        verify(exactly = 1) { eventBus.register(viewModel) }
    }

    @Test
    fun updateViewVolumeButtonPressEvent_WhenDeviceVolumeButtonPressed() {
        // Given
        val event = VolumeButtonPressEvent.VOLUME_UP

        // When
        viewModel.onVolumeButtonPressedEvent(event)

        // Then
        assertThat(viewModel.volumeButtonPress.value).isEqualTo(event)
    }

    @Test
    fun name() {

    }
}