package com.diskin.alon.coolclock.scenario

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.R
import com.diskin.alon.coolclock.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class NavigateAppSteps : GreenCoffeeSteps() {

    @Given("^User launched app fro device home screen$")
    fun user_launched_app_fro_device_home_screen() {
        DeviceUtil.launchAppFromHome()
    }

    @Then("^app should open alarms screen$")
    fun app_should_open_alarms_screen() {
        onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.alarms_root))
            .check(matches(isDisplayed()))
    }

    @When("^he navigates to world clocks feature$")
    fun he_navigates_to_world_clocks_feature() {
        onView(withId(com.diskin.alon.coolclock.home.presentation.R.id.world_clocks))
            .perform(click())
    }

    @Then("^app should open world clocks screen$")
    fun app_should_open_world_clocks_screen() {
        onView(withId(com.diskin.alon.coolclock.worldclocks.presentation.R.id.clocks))
            .check(matches(isDisplayed()))
    }

    @When("^he navigates to timer feature$")
    fun he_navigates_to_timer_feature() {
        onView(withId(com.diskin.alon.coolclock.home.presentation.R.id.timer))
            .perform(click())
    }

    @Then("^app should open timer screen$")
    fun app_should_open_timer_screen() {
        onView(withId(com.diskin.alon.coolclock.timer.presentation.R.id.motionLayout))
            .check(matches(isDisplayed()))
    }

    @When("^he navigates to settings feature$")
    fun he_navigates_to_settings_feature() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText("Settings"))
            .perform(click())
    }

    @Then("^app should open settings screen$")
    fun app_should_open_settings_screen() {
        onView(withId(com.diskin.alon.coolclock.settings.presentation.R.id.settings_root))
            .check(matches(isDisplayed()))
    }
}