package com.diskin.alon.coolclock.alarms.device

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RingtonePlayerTest {

    // Test subject
    private lateinit var player: RingtonePlayer

    // Collaborators
    private val appContext: Context = mockk()
    private val mediaPlayer: MediaPlayer = mockk()

    @Before
    fun setUp() {
        every { mediaPlayer.setAudioAttributes(any()) } returns Unit
        every { mediaPlayer.setOnErrorListener(any()) } returns Unit
        every { mediaPlayer.setOnPreparedListener(any()) } returns Unit

        player = RingtonePlayer(appContext, mediaPlayer)
    }

    @Test
    fun setPlayerAudioAttributes_WhenCreated() {
        // Given
        val attr = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        // Then
        verify(exactly = 1) { mediaPlayer.setAudioAttributes(attr) }
    }

    @Test
    fun playRingtoneOnDevice_WhenPlayed() {
        // Given
        val ringtoneUri = mockk<Uri>()

        every { mediaPlayer.reset() } returns Unit
        every { mediaPlayer.setDataSource(any(),any()) } returns Unit
        every { mediaPlayer.prepareAsync() } answers { player.onPrepared(mediaPlayer) }
        every { mediaPlayer.start() } returns Unit

        // When
        player.play(ringtoneUri)

        // Then
        verify(exactly = 1) { mediaPlayer.reset() }
        verify(exactly = 1) { mediaPlayer.setDataSource(appContext,ringtoneUri) }
        verify(exactly = 1) { mediaPlayer.prepareAsync() }
        verify(exactly = 1) { mediaPlayer.start() }
    }

    @Test
    fun releasePlayer_WhenReleased() {
        // Given

        every { mediaPlayer.release() } returns Unit

        // When
        player.release()

        // Then
        verify(exactly = 1) { mediaPlayer.release() }
    }

    @Test
    fun stopRingtonePlayOnDevice_WhenStopped() {
        // Given

        every { mediaPlayer.stop() } returns Unit

        // When
        player.stop()

        // Then
        verify(exactly = 1) { mediaPlayer.stop() }
    }
}