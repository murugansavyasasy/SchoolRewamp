package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.RectF
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SpinningProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dotCount = 12 // Total dots in circle
    private val visibleDots = 4 // Only show a few at a time
    private var angle = 0f
    private var animator: ValueAnimator? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        style = Paint.Style.FILL
    }

    init {
        startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = width.coerceAtMost(height).toFloat()
        val dotRadius = size * 0.07f   // Dot size
        val circleRadius = size * 0.4f // Distance from center

        val step = (2 * PI / dotCount).toFloat() // Angle step

        for (i in 0 until visibleDots) { // Show only a few dots
            val currentAngle = angle + i * step
            val x = (width / 2 + circleRadius * cos(currentAngle)).toFloat()
            val y = (height / 2 + circleRadius * sin(currentAngle)).toFloat()

            val alpha = (0.2f + 0.8f * (i.toFloat() / visibleDots)).coerceIn(0f, 1f) // Fading effect
            paint.alpha = (alpha * 255).toInt()

            canvas.drawCircle(x, y, dotRadius, paint)
        }
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, (2 * PI).toFloat()).apply {
            duration = 1000 // Speed of rotation
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                angle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimation() {
        animator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}