package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult

@BindingAdapter("setCityLocation")
fun setCityLocation(tv: TextView,result: UiCitySearchResult?) {
    result?.let {
        tv.text = if (result.state.isNotEmpty()) {
            "${result.country},${result.state}"
        } else {
            result.country
        }
    }
}