package com.diskin.alon.coolclock.common.uitesting

import android.view.View
import android.widget.ProgressBar
import android.widget.TextClock
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withSwitchChecked(checked: Boolean): Matcher<View> {
    return object : BoundedMatcher<View, SwitchCompat>(SwitchCompat::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with switch checked:$checked")
        }

        override fun matchesSafely(item: SwitchCompat): Boolean {
            return item.isChecked == checked
        }

    }
}

fun isRecyclerViewItemsCount(size: Int): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with items count:${size}")
        }

        override fun matchesSafely(item: RecyclerView): Boolean {
            return item.adapter!!.itemCount == size
        }

    }
}

fun isWithProgress(progress: Int): Matcher<View> {
    return object : BoundedMatcher<View,ProgressBar>(ProgressBar::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with progress:$progress")
        }

        override fun matchesSafely(item: ProgressBar): Boolean {
            return item.progress == progress
        }

    }
}

fun withTimeZone(timeZone: String): Matcher<View> {
    return object : BoundedMatcher<View, TextClock>(TextClock::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with time zone :${timeZone}")
        }

        override fun matchesSafely(item: TextClock): Boolean {
            return item.timeZone == timeZone
        }
    }
}

fun withTimeFormat24(format: String?): Matcher<View> {
    return object : BoundedMatcher<View, TextClock>(TextClock::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with 24 time format:${format}")
        }

        override fun matchesSafely(item: TextClock): Boolean {
            return when(format) {
                null -> item.format24Hour == null
                else -> item.format24Hour.toString() == format
            }
        }
    }
}

fun withTimeFormat12(format: String?): Matcher<View> {
    return object : BoundedMatcher<View,TextClock>(TextClock::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with 12 time format:${format}")
        }

        override fun matchesSafely(item: TextClock): Boolean {
            return when(format) {
                null -> item.format12Hour == null
                else -> item.format12Hour.toString() == format
            }
        }
    }
}

fun withTextViewTextColor(@ColorRes colorId: Int): Matcher<View> {
    return object : BoundedMatcher<View, TextView>(TextView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with text view text color :$colorId")
        }

        override fun matchesSafely(item: TextView): Boolean {
            val color = ContextCompat.getColor(
                item.context,
                colorId
            )
            return item.currentTextColor == color
        }
    }
}