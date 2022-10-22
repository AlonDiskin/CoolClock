package com.diskin.alon.coolclock.alarms.di

import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmVolumeRangeProvider
import com.diskin.alon.coolclock.alarms.application.interfaces.AlarmsScheduler
import com.diskin.alon.coolclock.alarms.application.interfaces.RingtoneSamplePlayer
import com.diskin.alon.coolclock.alarms.device.AlarmVolumeRangeProviderImpl
import com.diskin.alon.coolclock.alarms.device.AlarmsSchedulerImpl
import com.diskin.alon.coolclock.alarms.device.RingtoneSamplePlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceModule {

    @Singleton
    @Binds
    abstract fun bindAlarmsScheduler(scheduler: AlarmsSchedulerImpl): AlarmsScheduler

    @Singleton
    @Binds
    abstract fun bindDeviceRingtonePlayer(player: RingtoneSamplePlayerImpl): RingtoneSamplePlayer

    @Singleton
    @Binds
    abstract fun bindAlarmVolumeRangeProvider(provider: AlarmVolumeRangeProviderImpl): AlarmVolumeRangeProvider
}