package com.diskin.alon.coolclock.alarms.presentation.viewmodel

import com.diskin.alon.coolclock.alarms.application.model.AlarmEdit
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarmEdit
import javax.inject.Inject

class UiAlarmEditMapper @Inject constructor() {

    fun map(edit: AlarmEdit): UiAlarmEdit {
        return UiAlarmEdit(
            edit.hour,
            edit.minute,
            edit.name,
            edit.repeatDays.toMutableSet(),
            edit.vibration,
            edit.volume,
            edit.minVolume,
            edit.maxVolume,
            when(val sound = edit.sound) {
                is AlarmSound.Ringtone -> sound.path
                is AlarmSound.Silent -> ""
            },
            createDeviceRingtonesValues(edit.deviceRingtones),
            createDeviceRingtonesEntries(edit.deviceRingtones),
            edit.duration,
            edit.durationValues.toTypedArray(),
            edit.durationValues.map { duration -> "$duration minutes" }.toTypedArray(),
            edit.snooze,
            edit.snoozeValues.toTypedArray(),
            edit.snoozeValues.map { duration ->
                if (duration == 0) "None" else "$duration minutes"
            }.toTypedArray(),
        )
    }

    private fun createDeviceRingtonesValues(ringtones: List<AlarmSound.Ringtone>): Array<String> {
        val values = ringtones.map { ringtone -> ringtone.path }.toMutableList()

        values.add(0,"")

        return values.toTypedArray()
    }

    private fun createDeviceRingtonesEntries(ringtones: List<AlarmSound.Ringtone>): Array<String> {
        val values = ringtones.map { ringtone -> ringtone.name }.toMutableList()

        values.add(0,"Silent(no sound)")

        return values.toTypedArray()
    }
}