package com.diskin.alon.coolclock

import com.diskin.alon.coolclock.home.presentation.AppGraphProvider
import javax.inject.Inject

class AppGraphProviderImpl @Inject constructor() : AppGraphProvider {

    override fun getAppGraph(): Int {
        return R.navigation.app_graph
    }

    override fun getClocksDest(): Int {
        return com.diskin.alon.coolclock.worldclocks.presentation.R.id.cityClocksFragment
    }

    override fun getTimerDest(): Int {
        return com.diskin.alon.coolclock.timer.presentation.R.id.timerFragment
    }

    override fun getAlarmsDest(): Int {
        return com.diskin.alon.coolclock.alarms.presentation.R.id.alarmsFragment
    }
}