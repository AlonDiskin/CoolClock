package com.diskin.alon.coolclock.worldclocksfeaturetesting.search

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withHint
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CitiesSearchFragment
import com.diskin.alon.coolclock.worldclocksfeaturetesting.di.TestDatabase
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

class AddCitySteps(
    private val db: TestDatabase
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val notAddedCityName= "Rome"
    private val addedCityName= "London"
    private val addedCitiesCount = 2

    init {
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

    @Given("^user found a \"([^\"]*)\" via search$")
    fun user_found_a_city_via_search(city: String) {
        scenario = launchFragmentInHiltContainer<CitiesSearchFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val query = when(city) {
            "not added city" -> notAddedCityName
            "added city" -> addedCityName
            else -> throw IllegalArgumentException("Unknown scenario arg:$city")
        }

        onView(withHint(R.string.search_hint))
            .perform(typeText(query))
            .perform(pressImeActionButton())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he select to add it to his list$")
    fun he_select_to_add_it_to_his_list() {
        Thread.sleep(1000)
        onView(withRecyclerView(R.id.results).atPositionOnView(0, R.id.button_add))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
    }

    @Then("^app should \"([^\"]*)\" city to user list$")
    fun app_should_add_city_to_user_list(add: String) {
        val expectedAddedCitiesCount = when(add) {
            "add" -> {
                addedCitiesCount + 1
            }

            "not add" -> {
                addedCitiesCount
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$add")
        }

        val actualAddedCities = db.compileStatement("SELECT COUNT(*) FROM cities WHERE isSelected = 1")
            .simpleQueryForLong()

        assertThat(actualAddedCities).isEqualTo(expectedAddedCitiesCount)
    }
}