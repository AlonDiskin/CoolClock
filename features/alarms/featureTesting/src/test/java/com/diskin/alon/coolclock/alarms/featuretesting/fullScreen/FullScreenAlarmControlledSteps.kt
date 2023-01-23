package com.diskin.alon.coolclock.alarms.featuretesting.fullScreen

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Looper
import android.os.Vibrator
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.device.AlarmService
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.ui.FullScreenAlarmActivity
import com.diskin.alon.coolclock.common.uitesting.UnknownScenarioArgumentException
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import org.joda.time.DateTime
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowAlarmManager

class FullScreenAlarmControlledSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    private val audioManager: AudioManager,
    private val ringtoneManager: RingtoneManager,
    private val vibrator: Vibrator
) : GreenCoffeeSteps() {

    private val app: Application = ApplicationProvider.getApplicationContext<Context>() as Application
    private lateinit var scenario: ActivityScenario<FullScreenAlarmActivity>
    private val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)
    private val alarmService = Robolectric.setupService(AlarmService::class.java)
    private val notificationManager: NotificationManager = app
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Set alarm receiver
        app.registerReceiver(AlarmReceiver(), IntentFilter(ACTION_ALARM))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set fake ringtone manager
        val ringtoneValues = listOf(
            arrayOf("1","ringtone1_title", "ringtone1_uri"),
            arrayOf("2","ringtone2_title","ringtone2_uri"),
            arrayOf("3","ringtone3_title","ringtone3_uri")
        )

        Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .setCursorValues(ringtoneValues)

        // Stub mocked audio manager
        every { audioManager.setStreamVolume(any(),any(),any()) } returns Unit

        // Set scheduled alarm
        val alarmPendingIntent = Intent(app, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM
            intent.putExtra(KEY_ALARM_ID,1)

            PendingIntent.getBroadcast(app, 1, intent, 0)
        }
        val scheduledAlarm = ShadowAlarmManager.ScheduledAlarm(
            AlarmManager.RTC_WAKEUP,
            DateTime(2022,9,13,12,10).millis,
            alarmPendingIntent,
            null
        )
        shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set alarm data in test db
        val dbAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('alarm_name',23,45,'empty',1,'ringtone1_uri',0,5,5,1,0,1)"

        db.compileStatement(dbAlarm).executeInsert()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Given("^full screen alarm was launched by app$")
    fun full_screen_alarm_was_launched_by_app() {
        // Start alarm
        shadowAlarmManager.peekNextScheduledAlarm().operation.send()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Start alarm service
        alarmService.onStartCommand(Shadows.shadowOf(alarmService).nextStartedService,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Launch full screen alarm activity
        notificationManager.activeNotifications[0].notification.fullScreenIntent.send()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        scenario = ActivityScenario.launch(Shadows.shadowOf(app).nextStartedActivity)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user perform \"([^\"]*)\" on alarm$")
    fun user_perform_something_on_alarm(userAction: String) {
        // Perform alarm screen action
        when(userAction) {
            "dismiss" -> {
                onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.buttonDismiss))
                    .perform(swipeRight())
            }
            "snooze" -> onView(withId(com.diskin.alon.coolclock.alarms.presentation.R.id.buttonSnooze))
                .perform(swipeRight())
            else -> throw UnknownScenarioArgumentException(userAction)
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        if (Shadows.shadowOf(app).nextStoppedService.component!!.shortClassName
            == AlarmService::class.qualifiedName) {
            alarmService.onDestroy()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Then("^app should disable alarm$")
    fun app_should_disable_alarm() {
        // Verify alarm ringtone is not playing
        assertThat(Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .isPlaying()).isFalse()

        // Verify alarm vibration is off
        assertThat(Shadows.shadowOf(vibrator).isVibrating).isFalse()

        // Verify alarm notification is not shown
        assertThat(notificationManager.activeNotifications.size).isEqualTo(0)
    }

    @And("^close alarm screen$")
    fun close_alarm_screen() {
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }
}