package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.widget.ImageButton
import android.widget.TextClock
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock
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

@BindingAdapter("setCityLocation")
fun setCityLocation(tv: TextView,cityClock: UiCityClock?) {
    cityClock?.let {
        tv.text = if (cityClock.state.isNotEmpty()) {
            "${cityClock.country},${cityClock.state}"
        } else {
            cityClock.country
        }
    }
}

@BindingAdapter("setAddCityButton")
fun setAddCityButton(button: ImageButton, result: UiCitySearchResult?) {
    result?.let {
        when(it.isSelected) {
            true -> {
                loadIconResIntoImageButton(button,R.drawable.ic_baseline_done_24)
                button.isEnabled = false
            }
            false -> {
                loadIconResIntoImageButton(button,R.drawable.ic_baseline_add_24)
                button.isEnabled = true
            }
        }
    }
}

fun loadIconResIntoImageButton(imageButton: ImageButton, @DrawableRes res: Int) {
    imageButton.setImageResource(res)
}

@BindingAdapter("setClockTimeZone")
fun setClockTimeZone(tc: TextClock, timeZone: String?) {
    timeZone?.let { tc.timeZone = it }
}