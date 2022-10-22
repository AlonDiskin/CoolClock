package com.diskin.alon.coolclock.alarms.data

import android.database.Cursor
import android.database.MatrixCursor
import android.media.RingtoneManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(RingtoneManager::class)
class CustomShadowRingtoneManager {

    private var cursor: Cursor? = null

    fun setCursor(values: List<Array<String>>) {
        val ringtonesCursor = MatrixCursor(
            arrayOf(
                "ID_COLUMN",
                "TITLE_COLUMN",
                "URI_COLUMN"
            ),
            3
        )

        values.forEach { ringtonesCursor.addRow(it) }
        cursor = ringtonesCursor
    }

    @Implementation
    fun getCursor(): Cursor? {
        return cursor
    }
}