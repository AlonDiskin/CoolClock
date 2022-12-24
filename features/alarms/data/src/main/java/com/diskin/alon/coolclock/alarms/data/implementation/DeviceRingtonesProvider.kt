package com.diskin.alon.coolclock.alarms.data.implementation

import android.database.Cursor
import android.database.MatrixCursor
import android.media.RingtoneManager
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRingtonesProvider @Inject constructor(
    private val ringtoneManager: RingtoneManager
) {

    @WorkerThread
    @Synchronized fun get(): Cursor {
        val cursor = ringtoneManager.cursor!!
        val ringtonesCursor = MatrixCursor(
            arrayOf(
                "ID_COLUMN",
                "TITLE_COLUMN",
                "URI_COLUMN"
            ),
            cursor.count
        )

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)

            ringtonesCursor.addRow(arrayOf(id,title,uri))
        }

        return ringtonesCursor
    }
}