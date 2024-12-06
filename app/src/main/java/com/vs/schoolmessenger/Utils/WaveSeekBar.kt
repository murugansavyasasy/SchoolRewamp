package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class WaveSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private var progress: Float = 0.5f // Default progress (50%)

    var maxProgress: Int = 100
        set(value) {
            field = value
            invalidate()
        }

    var onProgressChangeListener: ((progress: Int) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val waveHeight = height / 4

        // Draw wave background
        for (i in 0 until width.toInt() step 30) {
            val waveY = height / 2 + waveHeight * Math.sin((i + progress * width) * 0.1).toFloat()
            canvas.drawCircle(i.toFloat(), waveY, 15f, wavePaint)
        }

        // Draw progress
        val progressWidth = progress * width
        canvas.drawRect(0f, 0f, progressWidth, height, progressPaint)

        // Draw thumb
        val thumbX = progressWidth
        val thumbY = height / 2
        canvas.drawCircle(thumbX, thumbY, 20f, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                progress = min(1f, maxOf(0f, x / width))
                onProgressChangeListener?.invoke((progress * maxProgress).toInt())
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}