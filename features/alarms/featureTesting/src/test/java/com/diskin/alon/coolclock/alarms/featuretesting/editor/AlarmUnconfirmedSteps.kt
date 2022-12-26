package com.diskin.alon.coolclock.alarms.featuretesting.editor

import android.app.AlarmManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Looper
import android.widget.EditText
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.controller.AlarmEditorFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import gherkin.ast.TableRow
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow

class AlarmUnconfirmedSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private val scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    init {
        // Mock static ringtone manager
        mockkStatic(RingtoneManager::class)
        val defaultRingtone = mockk<Ringtone>()
        val defaultRingtonePath = "ringtone1_uri/1"
        val defaultRingtoneTitle = "ringtone1_title"

        every { RingtoneManager.getActualDefaultRingtoneUri(any(),any()) } returns Uri.parse(defaultRingtonePath)
        every { RingtoneManager.getRingtone(any(),any()) } returns defaultRingtone
        every { defaultRingtone.getTitle(any()) } returns defaultRingtoneTitle

        // Config fake ringtone manager
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title","ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursorValues(ringtoneValues)

        // Setup test nav controller
        navController.setGraph(R.navigation.alarms_graph)
        navController.setCurrentDestination(R.id.alarmEditorFragment)

        // Launch editor fragment
        scenario = launchFragmentInHiltContainer<AlarmEditorFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set the NavController property on the fragment with test controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Given("^user edited a new alarm wth selected values$")
    fun user_edited_a_new_alarm_wth_selected_values(data: List<TableRow>) {
        var hour: Int? = null
        var minute: Int? = null
        var name: String? = null
        var vibration: Boolean? = null

        data.forEach { row ->
            when(row.cells[0].value) {
                "Hour" -> hour = row.cells[1].value.toInt()
                "Minute" -> minute = row.cells[1].value.toInt()
                "Name" -> name = row.cells[1].value
                "Vibration" -> vibration = row.cells[1].value.toBoolean()
            }
        }

        // Set time
        onView(withId(R.id.alarmTime))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("$hour o'clock"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withContentDescription("$minute minutes"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText("OK"))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set name
        scenario.onActivity {
            val et = it.findViewById<EditText>(R.id.editTextAlarmName)

            et.setText(name)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Select vibration
        if (vibration!!) {
            onView(withId(R.id.switchVibration))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @When("^he leave editor without confirming edit selection$")
    fun he_leave_editor_without_confirming_edit_selection() {
        scenario.onActivity { it.finish() }
    }

    @Then("^app should not schedule selected edited alarm$")
    fun app_should_not_schedule_selected_edited_alarm() {
        val actualSize = db.compileStatement("SELECT COUNT(*) FROM user_alarms")
            .simpleQueryForLong()

        assertThat(actualSize).isEqualTo(0)
        assertThat(Shadows.shadowOf(alarmManager).scheduledAlarms.size).isEqualTo(0)
    }
}