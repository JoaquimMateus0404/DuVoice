package com.cleansoft.duvoice.util.audio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

/**
 * Encoder para converter arquivos WAV para AAC/M4A
 */
class AudioEncoder {

    companion object {
        private const val TAG = "AudioEncoder"
        private const val TIMEOUT_US = 10000L
    }

    /**
     * Converte um arquivo WAV para AAC (M4A container)
     */
    suspend fun encodeWavToAac(
        inputWavFile: File,
        outputAacFile: File,
        bitRate: Int = 128000
    ): Boolean = withContext(Dispatchers.IO) {
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputStream: FileInputStream? = null

        try {
            // Ler header do WAV para obter parâmetros
            inputStream = FileInputStream(inputWavFile)
            val header = ByteArray(44)
            inputStream.read(header)

            val sampleRate = (header[24].toInt() and 0xFF) or
                    ((header[25].toInt() and 0xFF) shl 8) or
                    ((header[26].toInt() and 0xFF) shl 16) or
                    ((header[27].toInt() and 0xFF) shl 24)

            val channels = header[22].toInt() and 0xFF

            // Configurar formato de saída AAC
            val format = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate,
                channels
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }

            // Criar encoder
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            // Criar muxer
            muxer = MediaMuxer(outputAacFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val inputBuffer = ByteArray(16384)
            var isEos = false

            while (!isEos) {
                // Feed input
                val inputBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inputBufferIndex >= 0) {
                    val buffer = codec.getInputBuffer(inputBufferIndex)!!
                    val bytesRead = inputStream.read(inputBuffer)

                    if (bytesRead < 0) {
                        codec.queueInputBuffer(
                            inputBufferIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEos = true
                    } else {
                        // Converter PCM 16-bit para o formato esperado
                        buffer.clear()
                        buffer.put(inputBuffer, 0, bytesRead)
                        codec.queueInputBuffer(inputBufferIndex, 0, bytesRead, 0, 0)
                    }
                }

                // Get output
                var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                while (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size > 0 && muxerStarted) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        break
                    }

                    outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                }

                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = codec.outputFormat
                    trackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                }
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Error encoding to AAC", e)
            false
        } finally {
            try {
                inputStream?.close()
                codec?.stop()
                codec?.release()
                muxer?.stop()
                muxer?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing resources", e)
            }
        }
    }
}

