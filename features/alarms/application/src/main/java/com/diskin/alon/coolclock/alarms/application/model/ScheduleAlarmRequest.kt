package com.diskin.alon.coolclock.alarms.application.model

sealed class ScheduleAlarmRequest(val hour: Int,
                                  val minute: Int,
                                  val repeatDays: Set<RepeatDay>,
                                  val name: String,
                                  val ringtone: AlarmSound,
                                  val vibration: Boolean,
                                  val snooze: Int,
                                  val duration: Int,
                                  val volume: Int) {

    class NewAlarm(hour: Int,
                   minute: Int,
                   repeatDays: Set<RepeatDay>,
                   name: String,
                   ringtone: AlarmSound,
                   vibration: Boolean,
                   snooze: Int,
                   duration: Int, volume: Int)
    : ScheduleAlarmRequest(hour,
        minute,
        repeatDays,
        name,
        ringtone,
        vibration,
        snooze,
        duration,
        volume) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NewAlarm) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}