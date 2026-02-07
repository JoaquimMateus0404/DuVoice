package com.cleansoft.duvoice.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.cleansoft.duvoice.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.abs
import kotlin.math.min

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val amplitudes = mutableListOf<Int>()
    private val maxAmplitudes = 100 // Número máximo de barras

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val playedPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        alpha = 255
    }

    private val unplayedPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_background)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_background)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private var maxAmplitude = 32767 // Máximo para 16-bit PCM
    private var barWidth = 6f
    private var barGap = 3f
    private var minBarHeight = 4f
    private var progress: Float = 0f // 0 a 1
    private var isStaticWaveform = false

    fun addAmplitude(amplitude: Int) {
        if (isStaticWaveform) return
        amplitudes.add(amplitude)
        if (amplitudes.size > maxAmplitudes) {
            amplitudes.removeAt(0)
        }
        invalidate()
    }

    fun setAmplitudes(amplitudeList: List<Int>) {
        amplitudes.clear()
        val step = if (amplitudeList.size > maxAmplitudes) {
            amplitudeList.size / maxAmplitudes
        } else {
            1
        }
        for (i in amplitudeList.indices step step) {
            amplitudes.add(amplitudeList[i])
            if (amplitudes.size >= maxAmplitudes) break
        }
        invalidate()
    }

    fun clear() {
        amplitudes.clear()
        isStaticWaveform = false
        progress = 0f
        invalidate()
    }

    fun setWaveColor(color: Int) {
        paint.color = color
        playedPaint.color = color
        invalidate()
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun loadFromFile(filePath: String) {
        isStaticWaveform = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(filePath)
                if (!file.exists()) return@launch

                val extractedAmplitudes = extractWaveformData(file)

                withContext(Dispatchers.Main) {
                    setAmplitudes(extractedAmplitudes)
                }
            } catch (e: Exception) {
                // Gerar waveform aleatório se falhar
                withContext(Dispatchers.Main) {
                    generateRandomWaveform()
                }
            }
        }
    }

    private fun extractWaveformData(file: File): List<Int> {
        val result = mutableListOf<Int>()

        try {
            RandomAccessFile(file, "r").use { raf ->
                // Verificar se é WAV (procurar header)
                val header = ByteArray(44)
                raf.read(header)

                // Saltar header WAV (44 bytes)
                val dataSize = file.length() - 44
                if (dataSize <= 0) {
                    return generateRandomAmplitudes()
                }

                // Calcular samples a ler
                val samplesCount = maxAmplitudes
                val bytesPerSample = 2 // 16-bit
                val totalSamples = dataSize / bytesPerSample
                val samplesPerBar = (totalSamples / samplesCount).coerceAtLeast(1)

                raf.seek(44) // Ir para dados

                for (i in 0 until samplesCount) {
                    var maxAmp = 0
                    for (j in 0 until samplesPerBar.toInt()) {
                        if (raf.filePointer >= file.length()) break

                        val low = raf.read()
                        val high = raf.read()
                        if (low == -1 || high == -1) break

                        val sample = (high shl 8) or low
                        val signedSample = if (sample > 32767) sample - 65536 else sample
                        maxAmp = maxOf(maxAmp, abs(signedSample))
                    }
                    result.add(maxAmp)
                }
            }
        } catch (e: Exception) {
            return generateRandomAmplitudes()
        }

        return if (result.isEmpty()) generateRandomAmplitudes() else result
    }

    private fun generateRandomAmplitudes(): List<Int> {
        return List(maxAmplitudes) { (Math.random() * maxAmplitude * 0.7).toInt() }
    }

    private fun generateRandomWaveform() {
        setAmplitudes(generateRandomAmplitudes())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewHeight = height.toFloat()
        val viewWidth = width.toFloat()
        val centerY = viewHeight / 2

        val totalBarWidth = barWidth + barGap
        val maxBars = (viewWidth / totalBarWidth).toInt()

        if (amplitudes.isEmpty()) {
            canvas.drawLine(0f, centerY, viewWidth, centerY, backgroundPaint)
            return
        }

        val barCount = min(amplitudes.size, maxBars)
        val startX = (viewWidth - (barCount * totalBarWidth)) / 2 + barWidth / 2
        val progressBarIndex = (barCount * progress).toInt()

        for (i in 0 until barCount) {
            val index = if (amplitudes.size > barCount) {
                (i.toFloat() / barCount * amplitudes.size).toInt().coerceIn(0, amplitudes.size - 1)
            } else {
                i
            }

            val amplitude = amplitudes.getOrElse(index) { 0 }
            val normalizedAmplitude = (amplitude.toFloat() / maxAmplitude).coerceIn(0f, 1f)
            val barHeight = minBarHeight + (viewHeight / 2 - minBarHeight) * normalizedAmplitude

            val x = startX + i * totalBarWidth

            // Usar cor diferente para parte já reproduzida
            val barPaint = if (isStaticWaveform && i <= progressBarIndex) {
                playedPaint
            } else if (isStaticWaveform) {
                unplayedPaint
            } else {
                paint
            }

            canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, barPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 200
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }
}
