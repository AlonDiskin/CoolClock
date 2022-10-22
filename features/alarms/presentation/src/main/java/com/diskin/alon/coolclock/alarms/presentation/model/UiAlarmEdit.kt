package com.diskin.alon.coolclock.alarms.presentation.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.diskin.alon.coolclock.alarms.application.model.RepeatDay
import java.io.Serializable
import com.diskin.alon.coolclock.alarms.presentation.BR

data class UiAlarmEdit(var hour: Int,
                       var minute: Int,
                       private var name: String,
                       val repeatDays: MutableSet<RepeatDay>,
                       private var vibration: Boolean,
                       var volume: Int,
                       val minVolume: Int,
                       val maxVolume: Int,
                       var ringtone: String,
                       val ringtoneValues: Array<String>,
                       val ringtoneEntries: Array<String>,
                       var durationValue: Int,
                       val durationValues: Array<Int>,
                       val durationEntries: Array<String>,
                       var snoozeValue: Int,
                       val snoozeValues: Array<Int>,
                       val snoozeEntries: Array<String>,
                       val id: Int? = null
) : BaseObservable(), Serializable {

    fun getRingtoneName(): String {
        val index = ringtoneValues.toList().indexOf(ringtone)
        return ringtoneEntries[index]
    }

    fun getSnooze(): String {
        return if (snoozeValue != 0 ) {
            "$snoozeValue minutes"
        } else {
            "none"
        }
    }

    fun getDuration(): String {
        return "$durationValue minutes"
    }

    fun getTime(): String {
        val hourStr = if(hour < 10) "0".plus(hour) else hour.toString()
        val minuteStr = if(minute < 10) "0".plus(minute) else minute.toString()

        return "$hourStr:$minuteStr"
    }

    @Bindable
    fun getName(): String {
        return name
    }

    fun setName(value: String) {
        if (value != name) {
            name = value

            notifyChange()
            notifyPropertyChanged(BR.name)
        }
    }

    @Bindable
    fun getSunRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.SUN)
    }

    fun setSunRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.SUN)) {
                    repeatDays.add(RepeatDay.SUN)
                    notifyPropertyChanged(BR.sunRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.SUN)) {
                    repeatDays.remove(RepeatDay.SUN)
                    notifyPropertyChanged(BR.sunRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getMonRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.MON)
    }

    fun setMonRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.MON)) {
                    repeatDays.add(RepeatDay.MON)
                    notifyPropertyChanged(BR.monRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.MON)) {
                    repeatDays.remove(RepeatDay.MON)
                    notifyPropertyChanged(BR.monRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getTueRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.TUE)
    }

    fun setTueRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.TUE)) {
                    repeatDays.add(RepeatDay.TUE)
                    notifyPropertyChanged(BR.tueRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.TUE)) {
                    repeatDays.remove(RepeatDay.TUE)
                    notifyPropertyChanged(BR.tueRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getWedRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.WED)
    }

    fun setWedRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.WED)) {
                    repeatDays.add(RepeatDay.WED)
                    notifyPropertyChanged(BR.wedRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.WED)) {
                    repeatDays.remove(RepeatDay.WED)
                    notifyPropertyChanged(BR.wedRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getThuRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.THU)
    }

    fun setThuRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.THU)) {
                    repeatDays.add(RepeatDay.THU)
                    notifyPropertyChanged(BR.thuRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.THU)) {
                    repeatDays.remove(RepeatDay.THU)
                    notifyPropertyChanged(BR.thuRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getFriRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.FRI)
    }

    fun setFriRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.FRI)) {
                    repeatDays.add(RepeatDay.FRI)
                    notifyPropertyChanged(BR.friRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.FRI)) {
                    repeatDays.remove(RepeatDay.FRI)
                    notifyPropertyChanged(BR.friRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getSatRepeatDay(): Boolean {
        return repeatDays.contains(RepeatDay.SAT)
    }

    fun setSatRepeatDay(value: Boolean) {
        when(value) {
            true -> {
                if (!repeatDays.contains(RepeatDay.SAT)) {
                    repeatDays.add(RepeatDay.SAT)
                    notifyPropertyChanged(BR.satRepeatDay)
                }
            }

            false -> {
                if (repeatDays.contains(RepeatDay.SAT)) {
                    repeatDays.remove(RepeatDay.SAT)
                    notifyPropertyChanged(BR.satRepeatDay)
                }
            }
        }
    }

    @Bindable
    fun getVibration(): Boolean {
        return vibration
    }

    fun setVibration(value: Boolean) {
        if (value != vibration) {
            vibration = value
            notifyPropertyChanged(BR.vibration)
        }
    }
}