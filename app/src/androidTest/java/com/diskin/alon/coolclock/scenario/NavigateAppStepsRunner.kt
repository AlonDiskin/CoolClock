package com.diskin.alon.coolclock.scenario

import androidx.test.filters.LargeTest
import com.diskin.alon.coolclock.AppDataModule
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

@HiltAndroidTest
@UninstallModules(AppDataModule::class)
@RunWith(Parameterized::class)
@LargeTest
class NavigateAppStepsRunner(scenario: ScenarioConfig?) : GreenCoffeeTest(scenario)  {

    companion object {
        @Parameterized.Parameters(name = "{0}")
        @Throws(IOException::class)
        @JvmStatic
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/navigate_app.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun test() {
        start(NavigateAppSteps())
    }
}