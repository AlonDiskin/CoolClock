package com.diskin.alon.coolclock.alarms.featuretesting.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Looper
import android.os.Vibrator
import androidx.test.core.app.ApplicationProvider
import com.diskin.alon.coolclock.alarms.data.local.NO_REPEAT_DAYS
import com.diskin.alon.coolclock.alarms.data.local.NO_SOUND
import com.diskin.alon.coolclock.alarms.device.*
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.common.uitesting.UnknownScenarioArgumentException
import com.google.common.truth.Truth.*
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
import java.text.SimpleDateFormat
import java.util.*

class AlarmLaunchedSteps(
    private val db: TestDatabase,
    private val alarmManager: AlarmManager,
    private val audioManager: AudioManager,
    private val ringtoneManager: RingtoneManager,
    private val vibrator: Vibrator
) : GreenCoffeeSteps() {

    private val app: Application = ApplicationProvider.getApplicationContext<Context>() as Application
    private val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)
    private val alarmService = Robolectric.setupService(AlarmService::class.java)
    private lateinit var expectedAlarmName: String
    private lateinit var expectedAlarmRingtonePath: String
    private var expectedAlarmVibration = false
    private var expectedAlarmSnooze = false


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

    @Given("^user has scheduled alarm with \"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\", and \"([^\"]*)\" configurations$")
    fun user_has_scheduled_alarm_with_configurations(
        sound: String,
        vibration: String,
        name: String,
        repeated: String,
        snooze: String
    ) {
        // Set scheduled alarm
        expectedAlarmName = name
        val alarmId = 1
        var repeatDays = ""
        val alarmRingtonePath = when(sound) {
            "active" -> {
                expectedAlarmRingtonePath = "ringtone1_uri"
                "ringtone1_uri"
            }
            "non active" -> {
                expectedAlarmRingtonePath = ""
                NO_SOUND
            }
            else -> throw UnknownScenarioArgumentException(sound)
        }
        val alarmVibration = when(vibration) {
            "active" -> {
                expectedAlarmVibration = true
                "1"
            }
            "non active" -> {
                expectedAlarmVibration = false
                "0"
            }
            else -> throw UnknownScenarioArgumentException(vibration)
        }
        val nextAlarm = DateTime(2022,9,13,12,10)
        val alarmPendingIntent = Intent(app, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_ALARM
            intent.putExtra(KEY_ALARM_ID,alarmId)

            PendingIntent.getBroadcast(app, 1, intent, 0)
        }
        val scheduledAlarm = when(repeated) {
            "true" -> {
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

            "false" -> {
                repeatDays = NO_REPEAT_DAYS
                ShadowAlarmManager.ScheduledAlarm(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm.millis,
                    alarmPendingIntent,
                    null
                )
            }

            else -> throw UnknownScenarioArgumentException(repeated)
        }
        val snoozeDuration = when(snooze) {
            "enabled" -> {
                expectedAlarmSnooze = true
                5
            }

            "disabled" -> {
                expectedAlarmSnooze = false
                0
            }

            else -> throw UnknownScenarioArgumentException(snooze)
        }

        shadowAlarmManager.scheduledAlarms.add(scheduledAlarm)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set alarm data in test db
        val dbAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('$name',23,45,'$repeatDays',1,'$alarmRingtonePath',$alarmVibration,5,5,$snoozeDuration,0,$alarmId)"

        db.compileStatement(dbAlarm).executeInsert()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^alarm launched by app$")
    fun alarm_launched_by_app() {
        shadowAlarmManager.peekNextScheduledAlarm().operation.send()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        alarmService.onStartCommand(Shadows.shadowOf(alarmService).nextStartedService,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should set off alarm, according to user configurations$")
    fun app_should_set_off_alarm_according_to_user_configurations() {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val currentTimeFormat = SimpleDateFormat(app.getString(R.string.format_alarm_titme))
        val currentTimeDate = Calendar.getInstance().time
        val notificationTitle = currentTimeFormat.format(currentTimeDate)
        val notificationManager: NotificationManager = app
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        assertThat(notificationManager.activeNotifications.size).isEqualTo(1)

        // Verify alarm name and title
        assertThat(
            notificationManager.activeNotifications[0].notification.extras.getString(
                Notification.EXTRA_TITLE
            )
        ).isEqualTo(notificationTitle)
        assertThat(
            notificationManager.activeNotifications[0].notification
                .extras.getString("android.text")
        ).isEqualTo(expectedAlarmName)

        // Verify alarm actions are shown
        assertThat(notificationManager.activeNotifications[0].notification.actions[0].title)
            .isEqualTo(app.getString(R.string.button_notification_dismiss))

        when(expectedAlarmSnooze) {
            true -> {
                assertThat(notificationManager.activeNotifications[0].notification.actions.size)
                    .isEqualTo(2)
                assertThat(notificationManager.activeNotifications[0].notification.actions[1].title)
                    .isEqualTo(app.getString(R.string.button_notification_snooze))
            }

            false -> {
                assertThat(notificationManager.activeNotifications[0].notification.actions.size)
                    .isEqualTo(1)
            }
        }

        // Verify alarm ringtone is active/non active according to alarm configuration
        assertThat(
            Shadow.extract<CustomShadowRingtoneManager>(ringtoneManager)
                .getLastPlayedRingtonePath()
        ).isEqualTo(expectedAlarmRingtonePath)

        // Verify alarm vibration is active/non active according to alarm configuration
        val shadowVibrator = Shadows.shadowOf(vibrator)
        assertThat(shadowVibrator.isVibrating).isEqualTo(expectedAlarmVibration)
    }
}