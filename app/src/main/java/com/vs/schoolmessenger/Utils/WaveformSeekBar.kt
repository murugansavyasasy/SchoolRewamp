package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

class WaveformSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /* ===================== SEEK LISTENER ===================== */

    private var onSeekChange: ((Float) -> Unit)? = null

    fun setOnSeekChangeListener(listener: (Float) -> Unit) {
        onSeekChange = listener
    }

    /* ===================== PAINTS ===================== */

    private val playedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.PrimaryColor)
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 6f
    }


    private val unPlayedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D6D6D6")
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 6f
    }

    /* ===================== CONFIG ===================== */

    private val barCount = 42
    private val spacing = 14f
    private var progressLevel = 0f

    // Static WhatsApp-like waveform
    private val pattern = floatArrayOf(
        8f, 20f, 14f, 28f, 10f, 22f, 16f, 32f,
        12f, 26f, 18f, 30f, 10f, 24f, 14f,
        34f, 16f, 22f, 12f, 28f, 18f,
        32f, 10f, 26f, 14f, 30f, 16f,
        22f, 12f, 28f, 18f, 32f,
        10f, 24f, 14f, 30f, 16f,
        22f, 12f, 28f, 18f, 10f
    )

    /* ===================== REQUIRED FUNCTION ===================== */
    // ⚠️ DO NOT RENAME – used by your Activity
    fun updateWithLevel(level: Float) {
        progressLevel = level.coerceIn(0f, 1f)
        invalidate()
    }

    /* ===================== DRAW ===================== */

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val startX = (width - barCount * spacing) / 2f
        val playedBars = (barCount * progressLevel).toInt()

        for (i in 0 until barCount) {
            val x = startX + i * spacing
            val barHeight = pattern[i]
            val paint = if (i <= playedBars) playedPaint else unPlayedPaint

            if (barHeight <= 10f) {
                canvas.drawCircle(x, centerY, 4f, paint)
            } else {
                canvas.drawLine(
                    x,
                    centerY - barHeight / 2,
                    x,
                    centerY + barHeight / 2,
                    paint
                )
            }
        }
    }

    /* ===================== TOUCH SEEK ===================== */

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {

                val x = event.x.coerceIn(0f, width.toFloat())
                val progress = x / width.toFloat()

                updateWithLevel(progress)
                onSeekChange?.invoke(progress)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
