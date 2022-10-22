package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.application.model.ScheduleAlarmRequest
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarmEdit
import javax.inject.Inject

class ScheduleAlarmRequestMapper @Inject constructor() {

    fun mapNew(edit: UiAlarmEdit): ScheduleAlarmRequest {
        return ScheduleAlarmRequest.NewAlarm(
            edit.hour,
            edit.minute,
            edit.repeatDays,
            edit.getName(),
            when {
                edit.ringtone.isNotEmpty() -> AlarmSound.Ringtone(
                    edit.ringtone,
                    edit.getRingtoneName()
                )
                else -> AlarmSound.Silent
            },
            edit.getVibration(),
            edit.snoozeValue,
            edit.durationValue,
            edit.volume
        )
    }
}