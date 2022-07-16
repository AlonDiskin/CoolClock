package com.diskin.alon.coolclock.worldclocksfeaturetesting.search

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CitiesSearchFragment
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CitiesSearchResultsAdapter
import com.diskin.alon.coolclock.worldclocksfeaturetesting.di.TestDatabase
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

class SearchCitySteps(
    db: TestDatabase
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val existingCitiesQuery = "lo"
    private val nonExistingCitiesQuery = "ppppp"
    private val expectedSearchResults = mutableListOf<TestUiSearchResult>()

    private data class TestUiSearchResult(val name: String,
                                          val country: String,
                                          val state: String)

    init {
        // Set expected test result
        expectedSearchResults.add(
            TestUiSearchResult("London","United Kingdom","")
        )
        expectedSearchResults.add(
            TestUiSearchResult("Los Angeles","United States","CA")
        )
        expectedSearchResults.add(
            TestUiSearchResult("London","United States","CA")
        )

        // Populate test db with test data
        val insert1 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',1)"
        val insert2 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Moscow','Russian Federation','',10381222,'Europe/Moscow',0)"
        val insert3 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Rome','Italy','',2318895,'Europe/Rome', 0)"
        val insert4 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Jerusalem','Israel','',855234,'Asia/Jerusalem',0)"
        val insert5 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Los Angeles','United States','CA',3971883,'America/Los Angeles',1)"
        val insert6 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Chicago','United States','IL',2921853,'America/Chicago',0)"
        val insert7 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('London','United States','CA',1869,'America/Los Angeles',0)"

        db.compileStatement(insert1).executeInsert()
        db.compileStatement(insert2).executeInsert()
        db.compileStatement(insert3).executeInsert()
        db.compileStatement(insert4).executeInsert()
        db.compileStatement(insert5).executeInsert()
        db.compileStatement(insert6).executeInsert()
        db.compileStatement(insert7).executeInsert()
    }

    @Given("^user opened cities search screen$")
    fun user_opened_cities_search_screen() {
        scenario = launchFragmentInHiltContainer<CitiesSearchFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he perform a city search with query that has \"([^\"]*)\" results$")
    fun he_perform_a_city_search_with_query_that_has_something_results(existing: String) {
        val query = when(existing) {
            "matching" -> existingCitiesQuery
            "no matching" -> nonExistingCitiesQuery
            else -> throw IllegalArgumentException("Unknown scenario arg$existing")
        }

        onView(withHint(R.string.search_hint))
            .perform(typeText(query))
            .perform(pressImeActionButton())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app \"([^\"]*)\" show results start with query,ordered by city population$")
    fun app_something_show_results_start_with_query_ordered_by_city_population(display: String) {
        Thread.sleep(1000)
        when(display) {
            "should" -> {
                onView(withId(R.id.results))
                    .check(matches(isRecyclerViewItemsCount(expectedSearchResults.size)))

                expectedSearchResults.forEachIndexed { index, result ->
                    onView(withId(R.id.results))
                        .perform(scrollToPosition<CitiesSearchResultsAdapter.ResultViewHolder>(index))
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

            "should not" -> {
                onView(withId(R.id.results))
                    .check(matches(isRecyclerViewItemsCount(0)))
            }

            else -> throw IllegalArgumentException("Unknown scenario arg$display")
        }
    }
}