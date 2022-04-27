package com.diskin.alon.coolclock.home.presentation

import android.os.Looper
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@MediumTest
@Config(instrumentedPackages = ["androidx.loader.content"],application = HiltTestApplication::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Test subject
    private lateinit var scenario: ActivityScenario<MainActivity>

    // Collaborators
    @BindValue
    @JvmField
    val graphProvider: AppGraphProvider = mockk()

    @Before
    fun setUp() {
        // Stub collaborators
        every { graphProvider.getAppGraph() } returns R.navigation.test_app_graph
        every { graphProvider.getTimerDest() } returns R.id.timerDest
        every { graphProvider.getClocksDest() } returns R.id.clocksDest

        // Launch activity under test
        scenario = ActivityScenario.launch(MainActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showAppNameInAppBar_WhenResumed() {
        // Given

        // Then
        scenario.onActivity {
            val toolbar = it.findViewById<Toolbar>(R.id.toolbar)
            val  appName = it.getString(R.string.app_name)

            assertThat(toolbar.title).isEqualTo(appName)
        }
    }

    @Test
    fun openAlarmsScreenAsHome_WhenResumed() {
        // Given

        // Then
        scenario.onActivity {
            val bottomView = it.findViewById<BottomNavigationView>(R.id.bottom_nav)

            assertThat(bottomView.selectedItemId).isEqualTo(R.id.alarms)
        }
    }

    @Test
    fun openTimerScreen_WhenUserSelectIt() {
        // Given

        // When
        onView(withId(R.id.timer))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val bottomView = it.findViewById<BottomNavigationView>(R.id.bottom_nav)
            val controller = it.findNavController(R.id.nav_host_container)

            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.timerDest)
            assertThat(bottomView.selectedItemId).isEqualTo(R.id.timer)
        }
    }

    @Test
    fun openWorldClocksScreen_WhenUserSelectIt() {
        // Given

        // When
        onView(withId(R.id.world_clocks))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val bottomView = it.findViewById<BottomNavigationView>(R.id.bottom_nav)
            val controller = it.findNavController(R.id.nav_host_container)

            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.clocksDest)
            assertThat(bottomView.selectedItemId).isEqualTo(R.id.world_clocks)
        }
    }

    @Test
    fun openSettingsScreen_WhenUserSelectIt() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.title_action_settings))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val controller = it.findNavController(R.id.nav_host_container)

            assertThat(controller.currentDestination!!.id).isEqualTo(R.id.settingsDest)
        }
    }

    @Test
    fun closeActivity_WhenNavigatingBackFromTimerScreen() {
        // Given

        // When
        onView(withId(R.id.timer))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // And
        Espresso.pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    fun closeActivity_WhenNavigatingBackFromClocksScreen() {
        // Given

        // When
        onView(withId(R.id.world_clocks))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // And
        Espresso.pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    fun closeActivity_WhenNavigatingBackFromAlarmsScreen() {
        // Given

        // When
        Espresso.pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }
}