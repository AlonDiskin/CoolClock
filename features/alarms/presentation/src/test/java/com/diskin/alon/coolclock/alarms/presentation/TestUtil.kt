package com.diskin.alon.coolclock.alarms.presentation

import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarmEdit

fun createUiAlarms(): List<UiAlarm> {
    return listOf(
        UiAlarm(
            1,
            "23:08",
            "name_1",
            false,
            "Today",
            setOf(RepeatDay.SAT),
            false
        ),
        UiAlarm(
            2,
            "12:10",
            "name_2",
            true,
            "Tomorrow",
            emptySet(),
            true
        ),
        UiAlarm(
            3,
            "13:15",
            "name_3",
            true,
            "Tue,Jul 23",
            setOf(RepeatDay.SUN,RepeatDay.WED),
            false
        )
    )
}

fun createUnActiveAlarm() = UiAlarm(
    3,
    "13:15",
    "name_3",
    false,
    "Tue,Jul 23",
    setOf(RepeatDay.SUN,RepeatDay.WED),
    false
)

fun createActiveAlarm() = UiAlarm(
    3,
    "13:15",
    "name_3",
    true,
    "Tue,Jul 23",
    setOf(RepeatDay.SUN,RepeatDay.WED),
    false
)

fun createAlarmEdit() = UiAlarmEdit(
    12,
    15,
    "alarm name",
    mutableSetOf(RepeatDay.SUN,RepeatDay.FRI),
    false,
    6,
    1,
    15,
    "path_1",
    arrayOf("path_1","path_2","path_3"),
    arrayOf("ring bell","space tune","loud fuzz"),
    5,
    arrayOf(5,10,15),
    arrayOf("5 minutes","10 minutes","15 minutes"),
    1,
    arrayOf(1,5,10),
    arrayOf("1 minute","5 minutes","10 minutes")
)