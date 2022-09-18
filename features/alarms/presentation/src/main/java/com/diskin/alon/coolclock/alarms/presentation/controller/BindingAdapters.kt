package com.diskin.alon.coolclock.alarms.presentation.controller

import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm

@BindingAdapter("setRepeatDayLabel")
fun setRepeatDayLabel(tv: TextView,alarm: UiAlarm?) {
    alarm?.let {
        val context = tv.context
        val dayMarkedAsRepeatedColor = ContextCompat.getColor(
            context,
            com.google.android.material.R.color.design_default_color_primary
        )
        val dayMarkedAsUnrepeatedColor = ContextCompat.getColor(
            context,
            com.google.android.material.R.color.material_on_background_disabled
        )
        val color = when(tv.id) {
            R.id.sunday_label -> {
                if (it.repeatDays.contains(RepeatDay.SUN)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            R.id.monday_label -> {
                if (it.repeatDays.contains(RepeatDay.MON)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            R.id.tuesday_label -> {
                if (it.repeatDays.contains(RepeatDay.TUE)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            R.id.wednesday_label -> {
                if (it.repeatDays.contains(RepeatDay.WED)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            R.id.thursday_label -> {
                if (it.repeatDays.contains(RepeatDay.THU)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            R.id.friday_label -> {
                if (it.repeatDays.contains(RepeatDay.FRI)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            R.id.saturday_label -> {
                if (it.repeatDays.contains(RepeatDay.SAT)) {
                    dayMarkedAsRepeatedColor
                } else {
                    dayMarkedAsUnrepeatedColor
                }
            }

            else -> throw IllegalArgumentException("Wrong repeat day label for alarm!")
        }

        tv.setTextColor(color)
    }
}
