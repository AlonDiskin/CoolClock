package com.diskin.alon.coolclock.alarms.presentation

import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm

fun createUiAlarms(): List<UiAlarm> {
    return listOf(
        UiAlarm(
            1,
            "23:08",
            "name_1",
            false,
            "Today",
            setOf(RepeatDay.SAT)
        ),
        UiAlarm(
            2,
            "12:10",
            "name_2",
            true,
            "Tomorrow",
            emptySet()
        ),
        UiAlarm(
            3,
            "13:15",
            "name_3",
            true,
            "Tue,Jul 23",
            setOf(RepeatDay.SUN,RepeatDay.WED)
        )
    )
}

fun createUnActiveAlarm() = UiAlarm(
    3,
    "13:15",
    "name_3",
    false,
    "Tue,Jul 23",
    setOf(RepeatDay.SUN,RepeatDay.WED)
)

fun createActiveAlarm() = UiAlarm(
    3,
    "13:15",
    "name_3",
    true,
    "Tue,Jul 23",
    setOf(RepeatDay.SUN,RepeatDay.WED)
)