package com.diskin.alon.coolclock.scenario

import androidx.test.filters.LargeTest
import com.diskin.alon.coolclock.di.AppDataModule
import com.diskin.alon.coolclock.di.AppTestDatabase
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
class AlarmLaunchedStepsRunner(scenario: ScenarioConfig?) : GreenCoffeeTest(scenario)  {

    companion object {
        @Parameterized.Parameters(name = "{0}")
        @Throws(IOException::class)
        @JvmStatic
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/alarm_launched.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppTestDatabase

    @Test
    fun test() {
        hiltRule.inject()
        start(AlarmLaunchedSteps())
    }
}