package com.diskin.alon.coolclock.alarms.presentation

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsMapper
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmsMapperTest {

    @Test
    fun name() {
        val mapper = AlarmsMapper(
            ApplicationProvider.getApplicationContext()
        )
        val alarmDate = DateTime(2022,8,15,10,50)


    }
}