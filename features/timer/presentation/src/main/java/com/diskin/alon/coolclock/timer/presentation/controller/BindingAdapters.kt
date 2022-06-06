package com.diskin.alon.coolclock.timer.presentation.controller

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setRemainTime")
fun setRemainTime(tv: TextView,value: Int?) {
    value?.let { tv.text = if (it >= 10) it.toString() else "0".plus(it.toString()) }
}
