package com.diskin.alon.coolclock.alarms.application.model

const val DEFAULT_ALARM_HOUR = 6
const val DEFAULT_ALARM_MINUTE = 0
const val DEFAULT_ALARM_NAME = "my alarm"
const val DEFAULT_ALARM_VOLUME = 5
const val DEFAULT_ALARM_DURATION = 5
const val DEFAULT_ALARM_SNOOZE = 1
const val DEFAULT_ALARM_VIBRATION = false

sealed class AlarmEdit(val name: String,
                       val hour: Int,
                       val minute: Int,
                       val repeatDays: Set<RepeatDay> = emptySet(),
                       val sound: AlarmSound,
                       val duration: Int,
                       val volume: Int,
                       val snooze: Int,
                       val vibration: Boolean,
                       val deviceRingtones: List<AlarmSound.Ringtone>,
                       val minVolume: Int,
                       val maxVolume: Int) {

    // Const edit fields range values
    val durationValues: List<Int> = listOf(1,2,3,4,5,10,15,20,25,30)
    val snoozeValues: List<Int> = listOf(0,1,2,3,4,5,10,15,20,30)

    class DefaultEdit(val ringtone: AlarmSound.Ringtone,
                      deviceRingtones: List<AlarmSound.Ringtone>,
                      volumeRange: AlarmVolumeRange)
        : AlarmEdit(name = DEFAULT_ALARM_NAME,
        hour = DEFAULT_ALARM_HOUR,
        minute = DEFAULT_ALARM_MINUTE,
        repeatDays = emptySet(),
        sound = ringtone,
        duration = DEFAULT_ALARM_DURATION,
        volume = DEFAULT_ALARM_VOLUME,
        snooze = DEFAULT_ALARM_SNOOZE,
        vibration = DEFAULT_ALARM_VIBRATION,
        deviceRingtones = deviceRingtones,
        minVolume = volumeRange.min,
        maxVolume = volumeRange.max) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DefaultEdit) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}