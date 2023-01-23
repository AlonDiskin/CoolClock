package com.diskin.alon.coolclock.di

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDeviceModule {

    @Singleton
    @Provides
    fun provideAlarmManager(app: Application): AlarmManager {
        return app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Singleton
    @Provides
    fun provideAudioManager(app: Application): AudioManager {
        return app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Singleton
    @Provides
    fun provideRingtoneManagerManager(app: Application): RingtoneManager {
        return RingtoneManager(app)
    }

    @Singleton
    @Provides
    fun provideVibrator(app: Application): Vibrator {
        return app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @Singleton
    @Provides
    fun provideNotificationManagerCompat(app: Application): NotificationManagerCompat {
        return NotificationManagerCompat.from(app)
    }

    @Singleton
    @Provides
    fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }
}