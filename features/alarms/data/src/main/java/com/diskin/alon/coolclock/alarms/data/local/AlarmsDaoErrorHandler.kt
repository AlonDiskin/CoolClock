package com.diskin.alon.coolclock.alarms.data.local

import com.diskin.alon.coolclock.common.application.AppError
import javax.inject.Inject

class AlarmsDaoErrorHandler @Inject constructor() {

    fun handle(throwable: Throwable): AppError {
        return AppError.INTERNAL_ERROR
    }
}