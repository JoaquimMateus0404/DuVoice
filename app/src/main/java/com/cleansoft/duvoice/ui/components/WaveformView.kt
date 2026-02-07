package com.cleansoft.duvoice.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.cleansoft.duvoice.R
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

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.waveform_background)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private var maxAmplitude = 32767 // Máximo para 16-bit PCM
    private var barWidth = 8f
    private var barGap = 4f
    private var minBarHeight = 4f

    fun addAmplitude(amplitude: Int) {
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
        invalidate()
    }

    fun setWaveColor(color: Int) {
        paint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewHeight = height.toFloat()
        val viewWidth = width.toFloat()
        val centerY = viewHeight / 2

        // Calcular largura das barras baseado no número de amplitudes
        val totalBarWidth = barWidth + barGap
        val availableWidth = viewWidth
        val maxBars = (availableWidth / totalBarWidth).toInt()

        if (amplitudes.isEmpty()) {
            // Desenhar linha central quando não há dados
            canvas.drawLine(0f, centerY, viewWidth, centerY, backgroundPaint)
            return
        }

        // Desenhar barras
        val barCount = min(amplitudes.size, maxBars)
        val startX = (viewWidth - (barCount * totalBarWidth)) / 2 + barWidth / 2

        for (i in 0 until barCount) {
            val index = if (amplitudes.size > barCount) {
                amplitudes.size - barCount + i
            } else {
                i
            }

            if (index < amplitudes.size) {
                val amplitude = amplitudes[index]
                val normalizedAmplitude = (amplitude.toFloat() / maxAmplitude).coerceIn(0f, 1f)
                val barHeight = minBarHeight + (viewHeight / 2 - minBarHeight) * normalizedAmplitude

                val x = startX + i * totalBarWidth
                canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
            }
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

