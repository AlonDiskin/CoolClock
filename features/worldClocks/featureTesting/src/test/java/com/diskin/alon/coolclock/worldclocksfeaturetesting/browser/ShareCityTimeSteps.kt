package com.diskin.alon.coolclock.worldclocksfeaturetesting.browser

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.text.format.DateFormat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.controller.CityClocksFragment
import com.diskin.alon.coolclock.worldclocksfeaturetesting.di.TestDatabase
import com.google.common.truth.Truth
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import org.robolectric.Shadows
import java.text.SimpleDateFormat
import java.util.*

class ShareCityTimeSteps(
    private val db: TestDatabase
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val cityTimeZone = "Europe/London"
    private val cityName = "London"

    init {
        // Populate test db with test data
        val insert1 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',1,1234)"

        db.compileStatement(insert1).executeInsert()
    }

    @Given("^user selected to share the time of first listed world city clock$")
    fun user_selected_to_share_the_time_of_first_listed_world_city_clock() {
        Intents.init()

        // Launch clocks fragment
        scenario = launchFragmentInHiltContainer<CityClocksFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)

        // Share first city clock time
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.clocks)
                .atPositionOnView(0, R.id.options_button)
        )
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(ViewMatchers.withText(R.string.title_action_share))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should share city time via device sharing menu$")
    fun app_should_share_city_time_via_device_sharing_menu() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tz = TimeZone.getTimeZone(cityTimeZone)
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
            cityName,
            df.format(date)
        )

        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        Intents.intended(IntentMatchers.hasExtraWithKey(Intent.EXTRA_INTENT))

        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        Truth.assertThat(intent.type).isEqualTo(context.getString(R.string.mime_type_text))
        Truth.assertThat(intent.getStringExtra(Intent.EXTRA_TEXT)).isEqualTo(expectedCityTimeString)

        Intents.release()
    }
}