package com.diskin.alon.coolclock.worldclocksfeaturetesting.browser

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher
import com.diskin.alon.coolclock.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CityClocksFragment
import com.diskin.alon.coolclock.worldclocksfeaturetesting.di.TestDatabase
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.robolectric.Shadows

class DeleteCityClockSteps(
    private val db: TestDatabase
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val expectedUiCityClocks = listOf(
        TestUiCityClock(
            "Los Angeles",
            "United States",
            "CA",
            "America/Los Angeles"
        )
    )

    private data class TestUiCityClock(val name: String,
                                       val country: String,
                                       val state: String,
                                       val timeZone: String)

    init {
        // Populate test db with test data
        val insert1 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',1,1234)"
        val insert2 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Moscow','Russian Federation','',10381222,'Europe/Moscow',0)"
        val insert3 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Rome','Italy','',2318895,'Europe/Rome',0)"
        val insert4 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Jerusalem','Israel','',855234,'Asia/Jerusalem',0)"
        val insert5 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('Los Angeles','United States','CA',3971883,'America/Los Angeles',1,12345)"
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

    @Given("^user opened clocks screen$")
    fun user_opened_clocks_screen() {
        scenario = launchFragmentInHiltContainer<CityClocksFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
    }

    @When("^he select to remove the first shown clock$")
    fun he_select_to_remove_the_first_shown_clock() {
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.clocks)
                .atPositionOnView(0, R.id.options_button)
        )
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.title_action_delete_clock))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should remove it from listing$")
    fun app_should_remove_it_from_listing() {
        val selectedCitiesCount = db.compileStatement("SELECT COUNT(*) FROM cities WHERE isSelected = 1")
            .simpleQueryForLong()

        assertThat(selectedCitiesCount).isEqualTo(expectedUiCityClocks.size)
    }

    @And("^update shown clocks accordingly$")
    fun update_shown_clocks_accordingly() {
        onView(withId(R.id.clocks))
            .check(matches(isRecyclerViewItemsCount(expectedUiCityClocks.size)))
    }
}