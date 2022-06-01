package com.diskin.alon.coolclock.common.uitesting

import android.view.View
import android.widget.ProgressBar
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