package com.diskin.alon.coolclock.scenario

import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class AlarmLaunchedSteps : GreenCoffeeSteps(){

    @Given("^app has scheduled alarm$")
    fun app_has_scheduled_alarm() {
        TODO()
    }

    @When("^alarm trigger time arrive$")
    fun alarm_trigger_time_arrive() {
        TODO()
    }

    @When("^user snooze alarm$")
    fun user_snooze_alarm() {
        TODO()
    }

    @When("^snoozed time pass$")
    fun snoozed_time_pass() {
        TODO()
    }

    @Then("^app should launch alarm as urgent message in device according to its config$")
    fun app_should_launch_alarm_as_urgent_message_in_device_according_to_its_config() {
        TODO()
    }

    @Then("^app should launch alarm in full screen, according to its config$")
    fun app_should_launch_alarm_in_full_screen_according_to_its_config() {
        TODO()
    }

    @And("^lock device$")
    fun lock_device() {
        TODO()
    }
}