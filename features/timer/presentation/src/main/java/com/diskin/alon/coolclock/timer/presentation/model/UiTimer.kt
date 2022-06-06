package com.diskin.alon.coolclock.timer.presentation.model

data class UiTimer(val remainSeconds: Int,
                   val remainMinutes: Int,
                   val remainHours: Int,
                   val remainTime: Long,
                   val state: UiTimerState
)