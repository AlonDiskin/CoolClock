package com.diskin.alon.coolclock.worldclocksfeaturetesting.browser

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.coolclock.common.uitesting.*
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CityClocksAdapter
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CityClocksFragment
import com.diskin.alon.coolclock.worldclocksfeaturetesting.di.TestDatabase
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import org.robolectric.Shadows
import com.diskin.alon.coolclock.worldclocks.presentation.R

class ShowCityClocksSteps(
    db: TestDatabase
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val expectedUiCityClocks = listOf(
        TestUiCityClock(
            "Los Angeles",
            "United States",
            "CA",
            "America/Los Angeles"
        ),
        TestUiCityClock(
            "London",
            "United Kingdom",
            "",
            "Europe/London"
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

    @Then("^app should show all clocks in adding order$")
    fun app_should_show_all_clocks_in_adding_order() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        onView(withId(R.id.clocks))
            .check(matches(isRecyclerViewItemsCount(expectedUiCityClocks.size)))

        expectedUiCityClocks.forEachIndexed { index, uiCityClock ->

            onView(withId(R.id.clocks))
                .perform(
                    scrollToPosition<CityClocksAdapter.CityClockViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.clocks)
                    .atPositionOnView(index, R.id.city_name)
            )
                .check(matches(withText(uiCityClock.name)))

            val expectedLocation = if (uiCityClock.state.isNotEmpty()) {
                "${uiCityClock.country},${uiCityClock.state}"
            } else{
                uiCityClock.country
            }

            onView(
                withRecyclerView(R.id.clocks)
                    .atPositionOnView(index, R.id.city_location)
            )
                .check(matches(withText(expectedLocation)))

            onView(
                withRecyclerView(R.id.clocks)
                    .atPositionOnView(index, R.id.textClockTime)
            )
                .check(matches(withTimeZone(uiCityClock.timeZone)))
            onView(
                withRecyclerView(R.id.clocks)
                    .atPositionOnView(index, R.id.textClockTime)
            )
                .check(matches(withTimeFormat24(context.getString(com.diskin.alon.coolclock.worldclocks.presentation.R.string.clock_time_format_24))))

            onView(
                withRecyclerView(R.id.clocks)
                    .atPositionOnView(index, R.id.textClockDate)
            )
                .check(matches(withTimeZone(uiCityClock.timeZone)))
            onView(
                withRecyclerView(R.id.clocks)
                    .atPositionOnView(index, R.id.textClockDate)
            )
                .check(matches(withTimeFormat24(context.getString(com.diskin.alon.coolclock.worldclocks.presentation.R.string.clock_date_format_24))))
        }
    }
}