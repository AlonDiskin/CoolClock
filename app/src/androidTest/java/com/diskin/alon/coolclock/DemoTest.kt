package com.diskin.alon.coolclock

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diskin.alon.coolclock.util.DeviceUtil
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DemoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.diskin.alon.coolclock", appContext.packageName)
        DeviceUtil.launchAppFromHome()
        Thread.sleep(4000)
    }
}