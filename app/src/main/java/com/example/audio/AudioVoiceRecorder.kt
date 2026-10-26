package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
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
import java.io.FileInputStream

class AudioVoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null
    private var coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _audioAmplitudes = MutableStateFlow(listOf(0.2f, 0.4f, 0.6f, 0.3f, 0.5f))
    val audioAmplitudes: StateFlow<List<Float>> = _audioAmplitudes.asStateFlow()

    fun startRecording(onFailed: (String) -> Unit = {}): Boolean {
        try {
            stopRecording(discard = true)
            val file = File(context.cacheDir, "voice_record_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioEncodingBitRate(64000)
            mr.setAudioSamplingRate(22050)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()

            recorder = mr
            _isRecording.value = true
            _recordingSeconds.value = 0

            recordingJob = coroutineScope.launch {
                var seconds = 0
                while (isActive && _isRecording.value) {
                    delay(100)
                    val maxAmp = try {
                        recorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val normalized = (maxAmp / 32767f).coerceIn(0.1f, 1.0f)
                    val old = _audioAmplitudes.value
                    _audioAmplitudes.value = listOf(old[1], old[2], old[3], old[4], normalized)

                    if (System.currentTimeMillis() % 1000 < 120) {
                        seconds++
                        _recordingSeconds.value = seconds
                        if (seconds >= 30) {
                            // Capped at 30 seconds
                            break
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            onFailed(e.localizedMessage ?: "Microphone error")
            _isRecording.value = false
            return false
        }
    }

    fun stopRecording(discard: Boolean = false): Pair<String, String>? {
        recordingJob?.cancel()
        recordingJob = null
        val durationSec = _recordingSeconds.value
        _isRecording.value = false

        val mr = recorder
        recorder = null
        val file = currentOutputFile

        try {
            mr?.stop()
            mr?.release()
        } catch (e: Exception) {
            mr?.release()
        }

        if (discard || file == null || !file.exists() || file.length() == 0L) {
            file?.delete()
            return null
        }

        val formattedDuration = String.format("%02d:%02d", durationSec / 60, durationSec % 60)
        val bytes = FileInputStream(file).use { it.readBytes() }
        file.delete()

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val dataUri = "data:audio/mp4;base64,$base64"
        return Pair(dataUri, formattedDuration)
    }
}
