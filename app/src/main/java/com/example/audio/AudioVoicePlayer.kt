package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AudioVoicePlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _currentlyPlayingId = MutableStateFlow<Long?>(null)
    val currentlyPlayingId: StateFlow<Long?> = _currentlyPlayingId.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    fun playAudio(messageId: Long, audioDataUri: String, onComplete: () -> Unit = {}) {
        if (_currentlyPlayingId.value == messageId) {
            pause()
            return
        }

        stop()

        try {
            val mp = MediaPlayer()
            if (audioDataUri.startsWith("data:audio/")) {
                val commaIndex = audioDataUri.indexOf(",")
                if (commaIndex == -1) return
                val base64 = audioDataUri.substring(commaIndex + 1)
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val tempFile = File(context.cacheDir, "play_voice_${System.currentTimeMillis()}.tmp")
                FileOutputStream(tempFile).use { it.write(bytes) }
                mp.setDataSource(tempFile.absolutePath)
                mp.setOnCompletionListener {
                    tempFile.delete()
                    _currentlyPlayingId.value = null
                    _playbackProgress.value = 0f
                    progressJob?.cancel()
                    onComplete()
                }
            } else if (audioDataUri.startsWith("http://") || audioDataUri.startsWith("https://")) {
                mp.setDataSource(audioDataUri)
                mp.setOnCompletionListener {
                    _currentlyPlayingId.value = null
                    _playbackProgress.value = 0f
                    progressJob?.cancel()
                    onComplete()
                }
            } else {
                return
            }

            mp.prepare()
            mp.start()
            mediaPlayer = mp
            _currentlyPlayingId.value = messageId

            progressJob?.cancel()
            progressJob = coroutineScope.launch {
                while (isActive && mp.isPlaying) {
                    val duration = mp.duration
                    if (duration > 0) {
                        _playbackProgress.value = mp.currentPosition.toFloat() / duration
                    }
                    delay(50)
                }
            }
        } catch (e: Exception) {
            _currentlyPlayingId.value = null
            _playbackProgress.value = 0f
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
        _currentlyPlayingId.value = null
        progressJob?.cancel()
    }

    fun stop() {
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _currentlyPlayingId.value = null
        _playbackProgress.value = 0f
    }
}
