package com.diskin.alon.coolclock.home.presentation

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes

interface AppGraphProvider {

    @NavigationRes
    fun getAppGraph(): Int

    @IdRes
    fun getClocksDest(): Int

    @IdRes
    fun getTimerDest(): Int
}