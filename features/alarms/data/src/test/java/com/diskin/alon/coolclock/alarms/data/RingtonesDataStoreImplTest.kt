package com.diskin.alon.coolclock.alarms.data

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.data.implementation.RingtonesDataStoreImpl
import com.diskin.alon.coolclock.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

@RunWith(AndroidJUnit4::class)
@Config(shadows = [CustomShadowRingtoneManager::class])
class RingtonesDataStoreImplTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: RingtonesDataStoreImpl

    // Collaborators
    private val context: Context = mockk()
    private val ringtoneManager: RingtoneManager = RingtoneManager(
        ApplicationProvider.getApplicationContext<Context>().applicationContext
    )

    @Before
    fun setUp() {
        store = RingtonesDataStoreImpl(context,ringtoneManager)
    }

    @Test
    fun getDeviceDefaultRingtone() {
        // Given
        mockkStatic(RingtoneManager::class)
        val defaultUriStr = "default_uri"
        val defaultRingtoneUri = mockk<Uri>()
        val defaultRingtoneTitle = "default_title"
        val defaultRingtone = mockk<Ringtone>()

        every { RingtoneManager.getActualDefaultRingtoneUri(any(),any()) } returns defaultRingtoneUri
        every { defaultRingtoneUri.toString() } returns defaultUriStr
        every { RingtoneManager.getRingtone(any(),any()) } returns defaultRingtone
        every { defaultRingtone.getTitle(any()) } returns defaultRingtoneTitle

        // When
        val observer = store.getDefault().test()

        // Then
        verify(exactly = 1) { RingtoneManager.getActualDefaultRingtoneUri(context,RingtoneManager.TYPE_ALARM) }
        verify(exactly = 1) { RingtoneManager.getRingtone(context,defaultRingtoneUri)}
        every { defaultRingtone.getTitle(context) }
        observer.assertValue(AppResult.Success(AlarmSound
            .Ringtone(defaultRingtoneUri.toString(),defaultRingtoneTitle)))
    }

    @Test
    fun getAllDeviceRingtones() {
        // Given
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title","ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )
        val expectedRingtones = listOf(
            AlarmSound.Ringtone("ringtone1_uri/1","ringtone1_title"),
            AlarmSound.Ringtone("ringtone2_uri/2","ringtone2_title"),
            AlarmSound.Ringtone("ringtone3_uri/3","ringtone3_title"),
        )

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursor(ringtoneValues)

        // When
        val observer = store.getAll().test()

        // Then
        observer.assertValue(AppResult.Success(expectedRingtones))
    }

    @Test
    fun getDeviceRingtone_WhenQueriedByPath() {
        // Given
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title","ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )
        val expectedRingtone = AlarmSound.Ringtone("ringtone2_uri/2","ringtone2_title")
        val query = "ringtone2_uri/2"

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursor(ringtoneValues)

        // When
        val observer = store.getByPath(query).test()

        // Then
        observer.assertValue(AppResult.Success(expectedRingtone))
    }
}