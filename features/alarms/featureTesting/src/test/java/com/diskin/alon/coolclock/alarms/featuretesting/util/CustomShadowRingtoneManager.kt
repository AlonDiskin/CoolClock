package com.diskin.alon.coolclock.alarms.featuretesting.util

import android.database.Cursor
import android.database.MatrixCursor
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(RingtoneManager::class)
class CustomShadowRingtoneManager {

    private lateinit var cursorValues: List<Array<String>>
    private var lastPlayedPath: String = ""
    private var isPlaying = false

    fun setCursorValues(values: List<Array<String>>) {
        cursorValues = values
    }

    fun getLastPlayedRingtonePath() = lastPlayedPath

    fun isPlaying() = isPlaying

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

    @Implementation
    fun getRingtonePosition(ringtoneUri: Uri): Int {
        cursorValues.forEachIndexed { index, value ->
            if (value[2] == ringtoneUri.toString()) {
                return index
            }
        }

        return -1
    }

    @Implementation
    fun getRingtone(position: Int): Ringtone {
        return if (position >=0 && position < cursorValues.size) {
            val ringtone = mockk<Ringtone>()

            every { ringtone.play() } answers {
                lastPlayedPath = cursorValues[position][2]
                isPlaying = true
            }

            ringtone
        } else {
            throw IllegalArgumentException("Shadow alarm manager: has no ringtone for given position $position")
        }
    }

    @Implementation
    fun stopPreviousRingtone() {
        isPlaying = false
    }
}