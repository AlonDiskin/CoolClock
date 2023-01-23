package com.diskin.alon.coolclock.alarms.featuretesting.editor

import android.app.AlarmManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Looper
import android.widget.EditText
import android.widget.SeekBar
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.ui.AlarmEditorFragment
import com.diskin.alon.coolclock.common.uitesting.HiltTestActivity
import com.diskin.alon.coolclock.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow

class NewAlarmScheduledSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    ringtoneManager: RingtoneManager
) : GreenCoffeeSteps() {

    private val scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private lateinit var expectedScheduledAlarm: AlarmEntity
    private val currentTime = DateTime(2022,11,20,12,0,0,0)
    private lateinit var expectedNextAlarm: DateTime

    init {
        // Set fixed current time
        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)

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

    @Given("^user edited new alarm with selected \"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\",\"([^\"]" +
            "*)\",\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\"$")
    fun user_edited_new_alarm_with_selected_values(
        sound: String,
        vibration: String,
        snooze: String,
        duration: String,
        repeat: String,
        hour: String,
        minute: String,
        name: String,
        volume: String
    ) {
        // Init values set as default
        var selectedHour = 6
        var selectedMinute = 0
        var selectedRepeatDays = emptySet<WeekDay>()
        var selectedName = "my alarm"
        var selectedSound = "ringtone1_uri/1"
        var selectedVibration = false
        var selectedSnooze = 1
        var selectedVolume = 5
        var selectedDuration = 5

        // Select alarm time when not default
        if (hour != "default" && minute != "default") {
            selectedHour = hour.toInt()
            selectedMinute = minute.toInt()

            onView(withId(R.id.alarmTime))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withContentDescription("$hour o'clock"))
                .inRoot(isDialog())
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withContentDescription("$minute minutes"))
                .inRoot(isDialog())
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Select alarm repeat days
        if (repeat == "all week") {
            selectedRepeatDays = mutableSetOf(WeekDay.SUN,WeekDay.MON,WeekDay.TUE,
                WeekDay.WED,WeekDay.THU, WeekDay.FRI,WeekDay.SAT)

            onView(withId(R.id.chipSun))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withId(R.id.chipMon))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withId(R.id.chipTue))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withId(R.id.chipWed))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withId(R.id.chipThu))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withId(R.id.chipFri))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withId(R.id.chipSat))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Select alarm name
        if (name != "default") {
            selectedName = name
            scenario.onActivity {
                val et = it.findViewById<EditText>(R.id.editTextAlarmName)

                et.setText(name)
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Select sound
        if (sound != "default") {
            onView(withId(R.id.layoutSound))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            when(sound) {
                "sound 1" -> {
                    selectedSound = "ringtone1_uri/1"
                    onView(withText("ringtone1_title"))
                        .inRoot(isDialog())
                        .perform(click())
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }

                "sound 2" -> {
                    selectedSound = "ringtone2_uri/2"
                    onView(withText("ringtone2_title"))
                        .inRoot(isDialog())
                        .perform(click())
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }

                else -> throw IllegalArgumentException("Unknown sound arg:$sound")
            }

            onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Select vibration
        if (vibration != "default") {
            when(vibration) {
                "on" -> {
                    selectedVibration = true
                    onView(withId(R.id.switchVibration))
                        .perform(click())
                }
                "off" -> {
                    selectedVibration = false
                }
                else -> throw IllegalArgumentException("Unknown scenario vibration arg:$vibration")
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Select snooze
        if (snooze != "default") {
            onView(withId(R.id.snooze))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            when{
                snooze.contains("minutes") -> {
                    selectedSnooze = snooze.split(" ")[0].toInt()
                    onView(withText(snooze))
                        .inRoot(isDialog())
                        .perform(click())
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }
                snooze == "off" -> {
                    selectedSnooze = 0
                    onView(withText("None"))
                        .inRoot(isDialog())
                        .perform(click())
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }
            }

            onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Select volume
        if (volume != "default") {
            when(volume) {
                "min" -> {
                    selectedVolume = 1
                    scenario.onActivity {
                        val volumeSeekBar = it.findViewById<SeekBar>(R.id.volumeSeekBar)

                        volumeSeekBar.tag = true
                        volumeSeekBar.progress = volumeSeekBar.min
                    }
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }
                "max" -> {
                    selectedVolume = 15
                    scenario.onActivity {
                        val volumeSeekBar = it.findViewById<SeekBar>(R.id.volumeSeekBar)

                        volumeSeekBar.tag = true
                        volumeSeekBar.progress = volumeSeekBar.max
                    }
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }
                else -> throw IllegalArgumentException("Unknown scenario volume arg:$volume")
            }
        }

        // Select duration
        if (duration != "default") {
            onView(withId(R.id.duration))
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            when{
                duration.contains("minutes") -> {
                    selectedDuration = duration.split(" ")[0].toInt()
                    onView(withText(duration))
                        .inRoot(isDialog())
                        .perform(click())
                    Shadows.shadowOf(Looper.getMainLooper()).idle()
                }
            }

            onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click())
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
        expectedScheduledAlarm = AlarmEntity(
            selectedName,
            selectedHour,
            selectedMinute,
            selectedRepeatDays,
            true,
            Sound.AlarmSound(selectedSound),
            selectedVibration,
            selectedDuration,
            selectedVolume,
            selectedSnooze,
            false,
            1
        )

        // Set expected next alarm
        val alarm = DateTime()
            .withHourOfDay(selectedHour)
            .withMinuteOfHour(selectedMinute)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)

        expectedNextAlarm = if (alarm.isBeforeNow) {
            alarm.plusDays(1)
        } else {
            alarm
        }
    }

    @When("^he confirms edit selection$")
    fun he_confirms_edit_selection() {
        // Confirm schedule
        onView(withId(R.id.fab))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should schedule an alarm according to selected alarms values$")
    fun app_should_schedule_an_alarm_according_to_selected_alarms_values() {
        // Verify db contains only one record
        val actualScheduledAlarm = db.alarmDao().get(1).blockingGet()
        assertThat(actualScheduledAlarm).isEqualTo(expectedScheduledAlarm)

        // Verify alarm manager scheduled alarm
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)

        when {
            expectedScheduledAlarm.repeatDays.isEmpty() -> {
                assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(1)
            }

            expectedScheduledAlarm.repeatDays.isNotEmpty() -> {
                assertThat(shadowAlarmManager.scheduledAlarms.size).isEqualTo(7)
            }
        }

        assertThat(shadowAlarmManager.nextScheduledAlarm.triggerAtTime).isEqualTo(expectedNextAlarm.millis)
    }
}