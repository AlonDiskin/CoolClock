package com.diskin.alon.coolclock.alarms.data.implementation

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.diskin.alon.coolclock.alarms.application.interfaces.RingtonesDataStore
import com.diskin.alon.coolclock.alarms.application.model.AlarmSound
import com.diskin.alon.coolclock.common.application.AppError
import com.diskin.alon.coolclock.common.application.AppResult
import com.diskin.alon.coolclock.common.application.toSingleAppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RingtonesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ringtoneManager: RingtoneManager
) : RingtonesDataStore {

    override fun getDefault(): Single<AppResult<AlarmSound.Ringtone>> {
        return Single.create<AlarmSound.Ringtone> {
            var defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_ALARM
            )
            val defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri)

            if (defaultRingtoneUri.toString().contains("?")) {
                val uri = defaultRingtoneUri.toString().split("?")
                defaultRingtoneUri = Uri.parse(uri[0])
            }

            val resRingtone = AlarmSound.Ringtone(
                defaultRingtoneUri.toString(),
                defaultRingtone.getTitle(context)
            )

            it.onSuccess(resRingtone)
        }
            .subscribeOn(Schedulers.io())
            .toSingleAppResult{ AppError.INTERNAL_ERROR }
    }

    override fun getAll(): Single<AppResult<List<AlarmSound.Ringtone>>> {
        return Single.create<List<AlarmSound.Ringtone>> {
            val cursor = ringtoneManager.cursor!!
            val ringtones = mutableListOf<AlarmSound.Ringtone>()

            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = Uri.parse(
                    cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(
                        RingtoneManager.ID_COLUMN_INDEX
                    )
                )

                ringtones.add(AlarmSound.Ringtone(uri.toString(),title))
            }

            it.onSuccess(ringtones)
        }.subscribeOn(Schedulers.io())
            .toSingleAppResult{ AppError.INTERNAL_ERROR }
    }
}