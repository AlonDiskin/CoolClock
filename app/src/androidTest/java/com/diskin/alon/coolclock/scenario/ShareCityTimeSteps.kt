package com.diskin.alon.coolclock.scenario

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.coolclock.common.uitesting.RecyclerViewMatcher
import com.diskin.alon.coolclock.di.AppTestDatabase
import com.diskin.alon.coolclock.util.DeviceUtil
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.google.common.truth.Truth
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import java.text.SimpleDateFormat
import java.util.*

class ShareCityTimeSteps(
    db: AppTestDatabase
) : GreenCoffeeSteps() {

    init {
        // Populate test db with test data
        val insert1 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',1,1234)"

        db.compileStatement(insert1).executeInsert()
    }

    @Given("^user selected to share the time of his first selected world city$")
    fun user_selected_to_share_the_time_of_his_first_selected_world_city() {
        // Launch app from home
        DeviceUtil.launchAppFromHome()

        // Open world clocks screen
        onView(withContentDescription("World Clock"))
            .perform(click())

        Thread.sleep(2000)
        onView(
            RecyclerViewMatcher.withRecyclerView(R.id.clocks)
                .atPositionOnView(0, R.id.options_button)
        )
            .perform(click())

        Intents.init()

        onView(withText(R.string.title_action_share))
            .perform(click())
    }

    @Then("^app should share city time via device sharing menu$")
    fun app_should_share_city_time_via_device_sharing_menu() {
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        Intents.intended(IntentMatchers.hasExtraWithKey(Intent.EXTRA_INTENT))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        Truth.assertThat(intent.type).isEqualTo(context.getString(R.string.mime_type_text))
        Truth.assertThat(intent.getStringExtra(Intent.EXTRA_TEXT)).isEqualTo(getExpectedSharedMessage())

        Intents.release()
    }

    private fun getExpectedSharedMessage(): String {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tz = TimeZone.getTimeZone("Europe/London")
        val calendar = Calendar.getInstance(tz)
        val date = calendar.time
        val format = if(DateFormat.is24HourFormat(context)) {
            context.getString(R.string.clock_time_format_24)
        } else {
            context.getString(R.string.clock_time_format_12)
        }
        val df = SimpleDateFormat(format)
        df.timeZone = tz
        return "London current time:${df.format(date)}"
    }
}