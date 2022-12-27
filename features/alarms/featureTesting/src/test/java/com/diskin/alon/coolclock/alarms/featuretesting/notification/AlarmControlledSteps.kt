package com.diskin.alon.coolclock.alarms.featuretesting.notification

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
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.alarms.data.local.NO_REPEAT_DAYS
import com.diskin.alon.coolclock.alarms.device.ACTION_ALARM
import com.diskin.alon.coolclock.alarms.device.AlarmReceiver
import com.diskin.alon.coolclock.alarms.device.AlarmService
import com.diskin.alon.coolclock.alarms.device.KEY_ALARM_ID
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
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

class AlarmControlledSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    private val audioManager: AudioManager,
    private val ringtoneManager: RingtoneManager,
    private val vibrator: Vibrator
) : GreenCoffeeSteps() {

    private val app: Application = ApplicationProvider.getApplicationContext<Context>() as Application
    private val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)
    private val alarmService = Robolectric.setupService(AlarmService::class.java)
    private var isSnoozed = false

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
    }

    @Given("^\"([^\"]*)\" alarm is launched by app$")
    fun something_alarm_is_launched_by_app(alarmType: String) {
        // Set alarm in test db and scheduler
        val alarmId = 1
        var repeatDays = ""
        val nextAlarm = DateTime(2022,9,13,12,10)
        val alarmPendingIntent = Intent(app, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM
            intent.putExtra(KEY_ALARM_ID,alarmId)

            PendingIntent.getBroadcast(app, 1, intent, 0)
        }
        val scheduledAlarm = when(alarmType) {
            "one off" -> {
                repeatDays = NO_REPEAT_DAYS
                ShadowAlarmManager.ScheduledAlarm(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm.millis,
                    alarmPendingIntent,
                    null
                )
            }

            "repeated" -> {
                repeatDays = "SUN"
                val weekMillisInterval = 1000L * 60 * 60 * 24 * 7
                ShadowAlarmManager.ScheduledAlarm(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm.millis,
                    weekMillisInterval,
                    alarmPendingIntent,
                    null
                )
            }

            else -> throw UnknownScenarioArgumentException(alarmType)
        }

        val dbAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('alar_name',23,45,'$repeatDays',1,'ringtone1_uri',0,5,5,1,0,$alarmId)"

        db.compileStatement(dbAlarm).executeInsert()
        shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Start alarm
        shadowAlarmManager.nextScheduledAlarm.operation.send()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        alarmService.onStartCommand(Shadows.shadowOf(alarmService).nextStartedService,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user perform \"([^\"]*)\" on alarm$")
    fun user_perform_something_on_alarm(userAction: String) {
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val actionIntent = when(userAction) {
            "cancel" -> notificationManager.activeNotifications[0].notification.actions[0]
                .actionIntent
            "snooze" -> {
                isSnoozed = true
                notificationManager.activeNotifications[0].notification.actions[1]
                    .actionIntent
            }
            else -> throw UnknownScenarioArgumentException(userAction)
        }

        actionIntent.send()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        if (Shadows.shadowOf(app).nextStoppedService.component!!.shortClassName
            == AlarmService::class.qualifiedName) {
            alarmService.onDestroy()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Then("^app should disable alarm$")
    fun app_should_disable_alarm() {
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Verify alarm ringtone is not playing
        assertThat(Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
            .isPlaying()).isFalse()

        // Verify alarm vibration is off
        assertThat(Shadows.shadowOf(vibrator).isVibrating).isFalse()

        // Verify alarm notification is not shown
        assertThat(notificationManager.activeNotifications.size).isEqualTo(0)
    }

    @And("^reschedule it if is snoozed$")
    fun reschedule_it_if_is_snoozed() {
        // If alarm was snooze, verify alarm was rescheduled
        if (isSnoozed) {
            assertThat(shadowAlarmManager.scheduledAlarms.size)
                .isEqualTo(1)
        }
    }
}