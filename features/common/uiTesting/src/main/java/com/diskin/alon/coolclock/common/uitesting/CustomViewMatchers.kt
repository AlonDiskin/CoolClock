package com.diskin.alon.coolclock.common.uitesting

import android.view.View
import android.widget.ProgressBar
import android.widget.TextClock
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

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