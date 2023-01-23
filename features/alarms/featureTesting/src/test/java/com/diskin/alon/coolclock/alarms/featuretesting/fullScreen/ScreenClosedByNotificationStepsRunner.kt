package com.diskin.alon.coolclock.alarms.featuretesting.fullScreen

import android.app.AlarmManager
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ViewDataBinding
import androidx.test.filters.MediumTest
import com.diskin.alon.coolclock.alarms.featuretesting.util.CustomShadowRingtoneManager
import com.diskin.alon.coolclock.alarms.featuretesting.util.TestDatabase
import com.diskin.alon.coolclock.alarms.featuretesting.util.setFinalStatic
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject

@HiltAndroidTest
@RunWith(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
    application = HiltTestApplication::class,
    instrumentedPackages = ["androidx.loader.content"],
    shadows = [CustomShadowRingtoneManager::class]
)
@MediumTest
class ScreenClosedByNotificationStepsRunner(scenario: ScenarioConfig) : GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> {
            val res = ArrayList<Array<Any>>()
            val scenarioConfigs = GreenCoffeeConfig()
                .withFeatureFromAssets("feature/full_screen_alarm.feature")
                .withTags("@alarm-dismissed")
                .scenarios()

            for (scenarioConfig in scenarioConfigs) {
                res.add(arrayOf(scenarioConfig))
            }

            return res
        }

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    // Currently when using this rule,test wont advance upon loading results
    @get:Rule(order = 2)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var db: TestDatabase
    @Inject
    lateinit var alarmManager: AlarmManager
    @Inject
    lateinit var audioManager: AudioManager
    @Inject
    lateinit var ringtoneManager: RingtoneManager

    @Test
    fun test() {
        // Disable data binding Choreographer
        setFinalStatic(ViewDataBinding::class.java.getDeclaredField("USE_CHOREOGRAPHER"),false)

        // Inject test dependencies
        hiltRule.inject()

        // Start test
        start(ScreenClosedByNotificationSteps(db,alarmManager,audioManager,ringtoneManager))
    }
}