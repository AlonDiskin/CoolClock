package com.diskin.alon.coolclock.alarms.application

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmVolumeRangeProvider
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsRepository
import com.diskin.alon.coolclock.alarms.application.interfaces.RingtonesDataStore
import com.diskin.alon.coolclock.alarms.application.model.AlarmEdit
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.application.model.AlarmVolumeRange
import com.diskin.alon.coolclock.alarms.application.model.GetEditRequest
import com.diskin.alon.coolclock.alarms.application.usecase.GetAlarmEditUseCase
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetAlarmEditUseCaseTest {

    // Test subject
    private lateinit var useCase: GetAlarmEditUseCase

    // Collaborators
    private val ringtonesDataStore: RingtonesDataStore = mockk()
    private val alarmVolumeProvider: AlarmVolumeRangeProvider = mockk()
    private val alarmsRepository: AlarmsRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetAlarmEditUseCase(ringtonesDataStore,alarmVolumeProvider,alarmsRepository)
    }

    @Test
    fun returnEditWithDefaultValues_WhenExecutedForNewEdit() {
        // Given
        val request = GetEditRequest.New
        val defaultRingtone = mockk<AlarmSound.Ringtone>()
        val deviceRingtones = mockk<List<AlarmSound.Ringtone>>()
        val volume = AlarmVolumeRange(1,10)

        every { ringtonesDataStore.getDefault() } returns Single.just(AppResult.Success(defaultRingtone))
        every { ringtonesDataStore.getAll() } returns Single.just(AppResult.Success(deviceRingtones))
        every { alarmVolumeProvider.get() } returns Single.just(AppResult.Success(volume))

        // When
        val observer = useCase.execute(request).test()

        // Then
        val expectedEdit = AlarmEdit.DefaultEdit(defaultRingtone,deviceRingtones,volume)
        observer.assertValue(AppResult.Success(expectedEdit))
    }
}