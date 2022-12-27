package com.diskin.alon.coolclock.worldclocks.presentation

import android.content.Context
import android.os.Looper
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
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.coolclock.common.presentation.SingleLiveEvent
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.worldclocks.application.util.AppError
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CitiesSearchFragment
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CitiesSearchResultsAdapter.ResultViewHolder
import com.diskin.alon.coolclock.worldclocks.presentation.controller.loadIconResIntoImageButton
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CitiesSearchViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"],qualifiers = "w411dp-h891dp")
class CitiesSearchFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: CitiesSearchViewModel = mockk()
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val results = MutableLiveData<PagingData<UiCitySearchResult>>()
    private val addedCity = MutableLiveData<String>()
    private val searchTextSlot = slot<String>()
    private val addCityError = SingleLiveEvent<AppError>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub view model
        searchTextSlot.captured = ""
        every { viewModel.results } returns results
        every { viewModel.addedCity } returns addedCity
        every { viewModel.searchText = capture(searchTextSlot) } answers { }
        every { viewModel.searchText } answers { searchTextSlot.captured }
        every { viewModel.addCityError } returns addCityError

        // Setup test nav controller
        navController.setGraph(R.navigation.world_clocks_graph)
        navController.setCurrentDestination(R.id.citiesSearchFragment)

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<CitiesSearchFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun showOpenedSearchFieldWithHintInAppBar_WhenResumed() {
        // Given

        // Then
        onView(withHint(R.string.search_hint))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun navigateToCitiesClocksScreen_WhenBackPressed() {
        // Given

        // When
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.cityClocksFragment)
    }

    @Test
    fun performSearch_WhenSearchQuerySubmitted() {
        // Given
        val query = "query"

        every { viewModel.search(any()) } returns Unit

        // When
        onView(withHint(R.string.search_hint))
            .perform(typeText(query))
            .perform(pressImeActionButton())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.search(query) }
    }

    @Test
    fun showSpinningProgressBar_WhileSearchPerformed() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as CitiesSearchFragment
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
            .check(matches(isDisplayed()))
    }

    @Test
    fun hideSpinningProgressBar_WhenSearchIsDone() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as CitiesSearchFragment
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
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun showSearchResults_WhenResultsAvailable() {
        // Given
        val searchResults = createUiCitySearchResults()

        // When
        results.value = PagingData.from(searchResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        searchResults.forEachIndexed { index, result ->
            onView(withId(R.id.results))
                .perform(RecyclerViewActions.scrollToPosition<ResultViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.results).atPositionOnView(index, R.id.city_name))
                .check(matches(withText(result.name)))

            val expectedLocation = if (result.state.isNotEmpty()) {
                "${result.country},${result.state}"
            } else{
                result.country
            }

            onView(withRecyclerView(R.id.results).atPositionOnView(index, R.id.city_location))
                .check(matches(withText(expectedLocation)))
        }
    }

    @Test
    fun restoreSearchQuery_WhenRecreatedFromPrevState() {
        // Given
        val query = "query"

        // When
        onView(withId(androidx.appcompat.R.id.search_src_text))
            .perform(typeText(query))

        // And
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(androidx.appcompat.R.id.search_src_text))
            .check(matches(withText(query)))
    }

    @Test
    fun notifyWithErrorMessage_WhenSearchFailUponUnknownError() {
        // Given

        // When
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as CitiesSearchFragment
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
        val expectedMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(com.diskin.alon.coolclock.common.presentation.R.string.error_message_unknown,"Search")

        assertThat(toastMessage).isEqualTo(expectedMessage)
    }

    @Test
    fun enableToAddResultToUsersCityClocks_WhenResultNotAlreadyAdded() {
        // Given
        val searchResults = listOf(
            UiCitySearchResult(1,
                "name",
                "country",
                "state",
                false
            )
        )

        results.value = PagingData.from(searchResults)

        mockkStatic("com.diskin.alon.coolclock.worldclocks.presentation.controller.BindingAdaptersKt")
        every { viewModel.addCity(any()) } returns Unit
        every { loadIconResIntoImageButton(any(),any()) } returns Unit

        // Then
        onView(withId(R.id.button_add))
            .check(matches(isEnabled()))
        verify(exactly = 1) { loadIconResIntoImageButton(any(),R.drawable.ic_baseline_add_24) }

        // When
        onView(withId(R.id.button_add))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.addCity(searchResults.first()) }
    }

    @Test
    fun disableAddingResultToUsersCityClocks_WhenResultAlreadyAdded() {
        // Given
        val searchResults = listOf(
            UiCitySearchResult(1,
                "name",
                "country",
                "state",
                true
            )
        )

        results.value = PagingData.from(searchResults)

        mockkStatic("com.diskin.alon.coolclock.worldclocks.presentation.controller.BindingAdaptersKt")
        every { viewModel.addCity(any()) } returns Unit
        every { loadIconResIntoImageButton(any(),any()) } returns Unit

        // Then
        onView(withId(R.id.button_add))
            .check(matches(not(isEnabled())))
        verify(exactly = 1) { loadIconResIntoImageButton(any(),R.drawable.ic_baseline_done_24) }

        // When
        onView(withId(R.id.button_add))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 0) { viewModel.addCity(searchResults.first()) }
    }

    @Test
    fun notifyUser_WhenCityAddedToHisClocksList() {
        // Given
        val cityName = "Paris"

        // When
        addedCity.value = cityName

        // Then
        val toastMessage = ShadowToast.getTextOfLatestToast()
        val expectedMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.city_added_message,cityName)

        assertThat(toastMessage).isEqualTo(expectedMessage)
    }

    @Test
    fun notifyWithErrorMessage_WhenAddingCityFailUponUnknownError() {
        // Given

        // When
        addCityError.value = AppError.UNKNOWN_ERROR

        // Then
        val toastMessage = ShadowToast.getTextOfLatestToast()
        val expectedMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(com.diskin.alon.coolclock.common.presentation.R.string.error_message_unknown,"Add city")

        assertThat(toastMessage).isEqualTo(expectedMessage)
    }
}