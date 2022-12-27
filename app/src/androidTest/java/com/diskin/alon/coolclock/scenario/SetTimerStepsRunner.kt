package com.diskin.alon.coolclock.scenario

import androidx.test.filters.LargeTest
import com.diskin.alon.coolclock.di.AppDataModule
import com.diskin.alon.coolclock.timer.presentation.device.TimerAlarmManager
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppDataModule::class)
@RunWith(Parameterized::class)
@LargeTest
class SetTimerStepsRunner(scenario: ScenarioConfig?) : GreenCoffeeTest(scenario)  {

    companion object {
        @Parameterized.Parameters(name = "{0}")
        @Throws(IOException::class)
        @JvmStatic
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/set_timer.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var alarmManager: TimerAlarmManager

    @Test
    fun test() {
        hiltRule.inject()
        start(SetTimerSteps(alarmManager))
    }
}