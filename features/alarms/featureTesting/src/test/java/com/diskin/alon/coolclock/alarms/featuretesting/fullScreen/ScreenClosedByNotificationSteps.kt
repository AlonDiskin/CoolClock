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
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.device.AlarmService
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.presentation.ui.FullScreenAlarmActivity
import com.diskin.alon.coolclock.common.uitesting.UnknownScenarioArgumentException
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import org.joda.time.DateTime
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowAlarmManager

class ScreenClosedByNotificationSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    private val audioManager: AudioManager,
    private val ringtoneManager: RingtoneManager,
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

    @Given("^full screen alarm launched by app$")
    fun full_screen_alarm_launched_by_app() {
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

    @When("^user perform \"([^\"]*)\" from alarm notification$")
    fun user_perform_something_from_alarm_notification(
        notificationAction: String
    ) {
        // Perform alarm notification action
        val actionIntent = when(notificationAction) {
            "dismiss" -> notificationManager.activeNotifications[0].notification.actions[0]
                .actionIntent
            "snooze" -> notificationManager.activeNotifications[0].notification.actions[1]
                .actionIntent
            else -> throw UnknownScenarioArgumentException(notificationAction)
        }

        actionIntent.send()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should close alarm screen$")
    fun app_should_close_alarm_screen() {
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }
}