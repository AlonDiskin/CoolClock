package com.diskin.alon.coolclock.worldclocks.presentation

import android.os.Looper
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CitiesTimeFragment
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class CitiesTimeFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        // Setup test nav controller
        navController.setGraph(R.navigation.world_clocks_graph)
        navController.setCurrentDestination(R.id.citiesTimeFragment)

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<CitiesTimeFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun navigateToCitiesSearchScreen_WhenSearchSelected() {
        // Given

        // When
        onView(withId(R.id.action_search))
            .perform(click())

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.citiesSearchFragment)
    }
}