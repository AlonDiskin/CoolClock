package com.diskin.alon.coolclock.scenario

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.coolclock.di.AppTestDatabase
import com.diskin.alon.coolclock.util.DeviceUtil
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class CheckWorldCityTimeSteps(
    db: AppTestDatabase
) : GreenCoffeeSteps() {

    private val cityQuery = "rome"
    private val expectedWorldCitiesBrowserCount = 1
    private val expectedAddedCityName = "Rome"
    private val expectedAddedCityCountry = "Italy"

    init {
        // Populate test db with test data
        val insert1 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',0,1234)"
        val insert2 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Moscow','Russian Federation','',10381222,'Europe/Moscow',0)"
        val insert3 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Rome','Italy','',2318895,'Europe/Rome',0)"
        val insert4 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Jerusalem','Israel','',855234,'Asia/Jerusalem',0)"
        val insert5 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('Los Angeles','United States','CA',3971883,'America/Los Angeles',0,12345)"
        val insert6 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Chicago','United States','IL',2921853,'America/Chicago',0)"
        val insert7 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('London','United States','CA',1869,'America/Los Angeles',0)"

        db.compileStatement(insert1).executeInsert()
        db.compileStatement(insert2).executeInsert()
        db.compileStatement(insert3).executeInsert()
        db.compileStatement(insert4).executeInsert()
        db.compileStatement(insert5).executeInsert()
        db.compileStatement(insert6).executeInsert()
        db.compileStatement(insert7).executeInsert()
    }

    @Given("^user added a city after searching for it$")
    fun user_added_a_city_after_searching_for_it() {
        // Launch app from home
        DeviceUtil.launchAppFromHome()

        // Open world clocks screen
        onView(withContentDescription("World Clock"))
            .perform(click())

        // Open search screen
        onView(withId(R.id.action_search))
            .perform(click())

        // Search for city
        onView(withHint(R.string.search_hint))
            .perform(typeText(cityQuery))
            .perform(pressImeActionButton())

        Thread.sleep(2000)
        // Add city to user world clocks
        onView(withId(R.id.button_add))
            .perform(click())
    }

    @When("^he browse to city in world clocks listing$")
    fun he_browse_to_city_in_world_clocks_listing() {
        // Open city clocks browser screen
        DeviceUtil.pressBack()
        DeviceUtil.pressBack()
    }

    @Then("^app should show city local time$")
    fun app_should_show_city_local_time() {
        Thread.sleep(2000)
        onView(withId(R.id.clocks))
            .check(matches(isRecyclerViewItemsCount(expectedWorldCitiesBrowserCount)))
        onView(withText(expectedAddedCityName))
            .check(matches(isDisplayed()))
        onView(withText(expectedAddedCityCountry))
            .check(matches(isDisplayed()))
    }
}