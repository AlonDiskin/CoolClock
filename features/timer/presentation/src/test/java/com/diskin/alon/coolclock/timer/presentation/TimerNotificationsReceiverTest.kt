package com.diskin.alon.coolclock.timer.presentation

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.timer.presentation.util.TimerNotificationsManager
import com.diskin.alon.coolclock.timer.presentation.util.TimerNotificationsReceiver
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"],application = HiltTestApplication::class)
class TimerNotificationsReceiverTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var receiver: TimerNotificationsReceiver

    // Collaborators
    @BindValue
    @JvmField
    val eventBus: EventBus = mockk()

    @BindValue
    @JvmField
    val notificationsManager: TimerNotificationsManager = mockk()

    @Before
    fun setUp() {
        receiver = TimerNotificationsReceiver()
    }

    @Test
    fun cancelAlertNotification_WhenCancelTimerAlertBroadcastReceived() {
        // Given
        val broadcastIntent = Intent().apply { action = "ACTION_TIMER_ALERT_CANCEL" }

        every { notificationsManager.dismissTimerAlertNotification() } returns Unit

        // When
        receiver.onReceive(ApplicationProvider.getApplicationContext(),broadcastIntent)

        // Then
        verify { notificationsManager.dismissTimerAlertNotification() }
    }
}