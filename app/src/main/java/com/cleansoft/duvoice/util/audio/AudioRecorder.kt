package com.cleansoft.duvoice.util.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.cleansoft.duvoice.data.model.AudioQuality
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.abs

class AudioRecorder {

    companion object {
        private const val TAG = "AudioRecorder"
    }

    enum class State {
        IDLE, RECORDING, PAUSED, STOPPED
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var outputFile: File? = null
    private var fileOutputStream: FileOutputStream? = null

    private var sampleRate = 44100
    private var channelConfig = AudioFormat.CHANNEL_IN_MONO
    private var audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize = 0

    private val _state = MutableStateFlow(State.IDLE)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private var startTime = 0L
    private var pausedTime = 0L
    private var totalPausedDuration = 0L

    private val amplitudeHistory = mutableListOf<Int>()

    fun prepare(
        outputPath: File,
        quality: AudioQuality = AudioQuality.MEDIUM,
        isStereo: Boolean = false
    ): Boolean {
        try {
            sampleRate = quality.sampleRate
            channelConfig = if (isStereo) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_IN_MONO

            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
                Log.e(TAG, "Invalid buffer size")
                return false
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return false
            }

            outputFile = outputPath
            _state.value = State.IDLE
            return true

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing AudioRecord", e)
            return false
        }
    }

    fun start() {
        if (_state.value != State.IDLE && _state.value != State.PAUSED) {
            return
        }

        try {
            if (_state.value == State.IDLE) {
                // Novo início
                fileOutputStream = FileOutputStream(outputFile)
                // Escrever header WAV placeholder
                writeWavHeader(fileOutputStream!!, 0, sampleRate, if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) 2 else 1)
                startTime = System.currentTimeMillis()
                totalPausedDuration = 0L
                amplitudeHistory.clear()
            } else {
                // Retomar de pausa
                totalPausedDuration += System.currentTimeMillis() - pausedTime
            }

            audioRecord?.startRecording()
            _state.value = State.RECORDING

            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ShortArray(bufferSize)

                while (isActive && _state.value == State.RECORDING) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readCount > 0) {
                        // Calcular amplitude
                        val maxAmplitude = buffer.take(readCount).maxOfOrNull { abs(it.toInt()) } ?: 0
                        _amplitude.value = maxAmplitude
                        amplitudeHistory.add(maxAmplitude)

                        // Escrever dados no ficheiro
                        val byteBuffer = ByteArray(readCount * 2)
                        for (i in 0 until readCount) {
                            byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                        }
                        fileOutputStream?.write(byteBuffer)

                        // Atualizar tempo decorrido
                        _elapsedTime.value = System.currentTimeMillis() - startTime - totalPausedDuration
                    }

                    delay(10) // Pequeno delay para não sobrecarregar
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            _state.value = State.IDLE
        }
    }

    fun pause() {
        if (_state.value != State.RECORDING) return

        pausedTime = System.currentTimeMillis()
        audioRecord?.stop()
        recordingJob?.cancel()
        _state.value = State.PAUSED
    }

    fun stop(): File? {
        recordingJob?.cancel()

        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }

            fileOutputStream?.close()

            // Atualizar header WAV com tamanho correto
            outputFile?.let { file ->
                if (file.exists()) {
                    updateWavHeader(file)
                }
            }

            _state.value = State.STOPPED
            _amplitude.value = 0

            return outputFile

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            return null
        }
    }

    fun release() {
        recordingJob?.cancel()
        audioRecord?.release()
        audioRecord = null
        fileOutputStream?.close()
        fileOutputStream = null
        _state.value = State.IDLE
        _elapsedTime.value = 0
        _amplitude.value = 0
    }

    fun getAmplitudeHistory(): List<Int> = amplitudeHistory.toList()

    fun getDuration(): Long = _elapsedTime.value

    fun getSampleRate(): Int = sampleRate

    fun getChannels(): Int = if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) 2 else 1

    private fun writeWavHeader(
        outputStream: FileOutputStream,
        totalAudioLen: Long,
        sampleRate: Int,
        channels: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // File size (placeholder)
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()

        // WAVE header
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // Subchunk1 size (16 for PCM)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // Audio format (1 = PCM)
        header[20] = 1
        header[21] = 0

        // Channels
        header[22] = channels.toByte()
        header[23] = 0

        // Sample rate
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()

        // Byte rate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()

        // Block align
        header[32] = (channels * 2).toByte()
        header[33] = 0

        // Bits per sample
        header[34] = 16
        header[35] = 0

        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // Data size (placeholder)
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        outputStream.write(header)
    }

    private fun updateWavHeader(file: File) {
        val fileSize = file.length()
        val dataSize = fileSize - 44

        RandomAccessFile(file, "rw").use { raf ->
            // Update file size at position 4
            raf.seek(4)
            raf.write((dataSize + 36).toInt() and 0xff)
            raf.write(((dataSize + 36).toInt() shr 8) and 0xff)
            raf.write(((dataSize + 36).toInt() shr 16) and 0xff)
            raf.write(((dataSize + 36).toInt() shr 24) and 0xff)

            // Update data size at position 40
            raf.seek(40)
            raf.write(dataSize.toInt() and 0xff)
            raf.write((dataSize.toInt() shr 8) and 0xff)
            raf.write((dataSize.toInt() shr 16) and 0xff)
            raf.write((dataSize.toInt() shr 24) and 0xff)
        }
    }
}

