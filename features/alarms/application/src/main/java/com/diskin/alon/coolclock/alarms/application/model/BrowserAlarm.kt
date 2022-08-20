package com.diskin.alon.coolclock.alarms.application.model

data class BrowserAlarm(val id: Int,
                        val name: String,
                        val hour: Int,
                        val minute: Int,
                        val repeatDays: Set<RepeatDay>,
                        val isActive: Boolean,
                        val nextAlarm: NextAlarm)