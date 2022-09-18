package com.diskin.alon.coolclock.common.application

interface Mapper<S : Any,T : Any> {

    fun map(source: S): T
}