package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class DashBoardWaveDesign @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 90
        style = Paint.Style.FILL
    }

    private val wavePaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 140
        style = Paint.Style.FILL
    }

    private val wavePaint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 255
        style = Paint.Style.FILL
    }

    private var phaseShift1 = 0f
    private var phaseShift2 = 0f
    private var phaseShift3 = 0f

    private val waveLength = 800f
    private val waveHeight1 = 35f
    private val waveHeight2 = 45f
    private val waveHeight3 = 55f


    private val waveSpeed1 = 0.3f
    private val waveSpeed2 = 0.2f
    private val waveSpeed3 = 0.1f

    private val animator = object : Runnable {
        override fun run() {
            phaseShift1 += waveSpeed1
            phaseShift2 += waveSpeed2
            phaseShift3 += waveSpeed3
            invalidate()
            postDelayed(this, 16)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(animator)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(animator)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWave(canvas, wavePaint1, waveHeight1, phaseShift1)
        drawWave(canvas, wavePaint2, waveHeight2, phaseShift2)
        drawWave(canvas, wavePaint3, waveHeight3, phaseShift3)
    }

    private fun drawWave(canvas: Canvas, paint: Paint, waveHeight: Float, phaseShift: Float) {
        val path = Path()
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height - waveHeight

        path.moveTo(0f, height)
        for (x in 0..width.toInt()) {
            val y = (waveHeight * sin((x + phaseShift) * Math.PI * 2 / waveLength)).toFloat()
            path.lineTo(x.toFloat(), centerY + y)
        }
        path.lineTo(width, height)
        path.close()
        canvas.drawPath(path, paint)
    }
}