package com.diskin.alon.coolclock.worldclocks.presentation

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.text.format.DateFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.common.uitesting.*
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CityClocksAdapter
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CityClocksFragment
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CityClocksViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"],qualifiers = "w411dp-h891dp")
class CityClocksFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: CityClocksViewModel = mockk()
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val cityClocks = MutableLiveData<PagingData<UiCityClock>>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        every { viewModel.cityClocks } returns cityClocks

        // Setup test nav controller
        navController.setGraph(R.navigation.world_clocks_graph)
        navController.setCurrentDestination(R.id.cityClocksFragment)

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<CityClocksFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun openCitiesSearchScreen_WhenUserNavigateToIt() {
        // Given

        // When
        onView(withId(R.id.action_search))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.citiesSearchFragment)
    }

    @Test
    fun showUserWorldClocks_WhenResumed() {
        // Given
        val cityClocksData = createUiCityClocks()
        val context = ApplicationProvider.getApplicationContext<Context>()

        // When
        cityClocks.value = PagingData.from(cityClocksData)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.clocks))
            .check(matches(isRecyclerViewItemsCount(cityClocksData.size)))

        cityClocksData.forEachIndexed { index, uiCityClock ->
            onView(withId(R.id.clocks))
                .perform(scrollToPosition<CityClocksAdapter.CityClockViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.clocks).atPositionOnView(index, R.id.city_name))
                .check(matches(withText(uiCityClock.name)))

            val expectedLocation = if (uiCityClock.state.isNotEmpty()) {
                "${uiCityClock.country},${uiCityClock.state}"
            } else{
                uiCityClock.country
            }

            onView(withRecyclerView(R.id.clocks).atPositionOnView(index, R.id.city_location))
                .check(matches(withText(expectedLocation)))

            onView(withRecyclerView(R.id.clocks).atPositionOnView(index, R.id.textClockTime))
                .check(matches(withTimeZone(uiCityClock.gmt)))
            onView(withRecyclerView(R.id.clocks).atPositionOnView(index, R.id.textClockTime))
                .check(matches(withTimeFormat24(context.getString(R.string.clock_time_format_24))))

            onView(withRecyclerView(R.id.clocks).atPositionOnView(index, R.id.textClockDate))
                .check(matches(withTimeZone(uiCityClock.gmt)))
            onView(withRecyclerView(R.id.clocks).atPositionOnView(index, R.id.textClockDate))
                .check(matches(withTimeFormat24(context.getString(R.string.clock_date_format_24))))
        }
    }

    @Test
    fun showLoadingIndicator_WhileCitiesLoading() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as CityClocksFragment
            fragment.handleLoadStateUpdate(
                CombinedLoadStates(
                    LoadState.Loading,
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadStates(
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true)
                    )
                )
            )
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.progressBar))
            .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun hideLoadingIndicator_WhenCitiesLoaded() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as CityClocksFragment
            fragment.handleLoadStateUpdate(
                CombinedLoadStates(
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadStates(
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true)
                    )
                )
            )
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.progressBar))
            .check(matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun notifyError_WhenCitiesLoadingFailDueToUnknownError() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as CityClocksFragment
            fragment.handleLoadStateUpdate(
                CombinedLoadStates(
                    LoadState.Error(Throwable()),
                    LoadState.NotLoading(true),
                    LoadState.NotLoading(true),
                    LoadStates(
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true),
                        LoadState.NotLoading(true)
                    )
                )
            )
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val toastMessage = ShadowToast.getTextOfLatestToast()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(
            com.diskin.alon.coolclock.common.presentation.R.string.error_message_unknown,
            context.getString(R.string.clocks_browser_feature)
        )

        assertThat(toastMessage).isEqualTo(expectedMessage)
    }

    @Test
    fun deleteCityClock_WheUserSelectToDeleteIt() {
        // Given
        val clocks = createUiCityClocks()
        cityClocks.value = PagingData.from(clocks)

        every { viewModel.deleteCityClock(any()) } returns Unit

        // When
        onView(withRecyclerView(R.id.clocks).atPositionOnView(0, R.id.options_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.title_action_delete_clock))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.deleteCityClock(clocks.first()) }
    }

    @Test
    fun shareCityTime_WhenUserSelectToShareIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clocks = createUiCityClocks()
        cityClocks.value = PagingData.from(clocks)
        val tz = TimeZone.getTimeZone(clocks.first().gmt)
        val calendar = Calendar.getInstance(tz)
        val date = calendar.time
        val format = if(DateFormat.is24HourFormat(context)) {
            context.getString(R.string.clock_time_format_24)
        } else {
            context.getString(R.string.clock_time_format_12)
        }
        val df = SimpleDateFormat(format)
        df.timeZone = tz
        val expectedCityTimeString = context.getString(
            R.string.share_city_time_message,
            clocks.first().name,
            df.format(date)
        )

        Intents.init()

        // When
        onView(withRecyclerView(R.id.clocks).atPositionOnView(0, R.id.options_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.title_action_share))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        Intents.intended(IntentMatchers.hasExtraWithKey(Intent.EXTRA_INTENT))

        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.type).isEqualTo(context.getString(R.string.mime_type_text))
        assertThat(intent.getStringExtra(Intent.EXTRA_TEXT)).isEqualTo(expectedCityTimeString)

        Intents.release()
    }
}