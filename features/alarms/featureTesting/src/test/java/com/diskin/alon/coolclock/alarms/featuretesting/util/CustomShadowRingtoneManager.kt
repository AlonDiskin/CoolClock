package com.diskin.alon.coolclock.alarms.featuretesting.util

import android.database.Cursor
import android.database.MatrixCursor
import android.media.RingtoneManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(RingtoneManager::class)
class CustomShadowRingtoneManager {

    private lateinit var cursorValues: List<Array<String>>

    fun setCursorValues(values: List<Array<String>>) {
        cursorValues = values
    }

    @Implementation
    fun getCursor(): Cursor? {
        val ringtonesCursor = MatrixCursor(
            arrayOf(
                "ID_COLUMN",
                "TITLE_COLUMN",
                "URI_COLUMN"
            ),
            3
        )

        cursorValues.forEach { ringtonesCursor.addRow(it) }
        return ringtonesCursor
    }
}