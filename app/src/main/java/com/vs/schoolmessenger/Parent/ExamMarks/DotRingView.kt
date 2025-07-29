package com.vs.schoolmessenger.Parent.ExamMarks

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DotRingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val outerDotCount = 20
    private val midDotCount = 28
    private val innerDotCount = 36

    private val outerDotRadius = 15f
    private val midDotRadius = 11f
    private val innerDotRadius = 7f

    private val outerDotColor = Color.parseColor("#d4e0f6")
    private val midDotColor = Color.parseColor("#79a7e3")
    private val innerDotColor = Color.parseColor("#5485e1")

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val midPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var alphaFactor = 255 // Fully opaque
    private var isFadedOut = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Start fade-out only after the view is attached
        post { startFadeOutAnimation() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = (min(width, height) / 2f) * 0.85f

        val outerRadius = maxRadius * 0.90f
        val midRadius = maxRadius * 0.75f
        val innerRadius = maxRadius * 0.60f

        outerPaint.color = outerDotColor
        midPaint.color = midDotColor
        innerPaint.color = innerDotColor

        outerPaint.alpha = alphaFactor
        midPaint.alpha = alphaFactor
        innerPaint.alpha = alphaFactor

        drawDots(canvas, centerX, centerY, outerRadius, outerDotCount, outerDotRadius, outerPaint)
        drawDots(canvas, centerX, centerY, midRadius, midDotCount, midDotRadius, midPaint)
        drawDots(canvas, centerX, centerY, innerRadius, innerDotCount, innerDotRadius, innerPaint)
    }

    private fun drawDots(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        count: Int,
        dotRadius: Float,
        paint: Paint
    ) {
        for (i in 0 until count) {
            val angle = Math.toRadians((360.0 / count) * i)
            val x = (cx + radius * cos(angle)).toFloat()
            val y = (cy + radius * sin(angle)).toFloat()
            canvas.drawCircle(x, y, dotRadius, paint)
        }
    }

    private fun startFadeOutAnimation() {
        val animator = ValueAnimator.ofInt(255, 0).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                alphaFactor = it.animatedValue as Int
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Restore alpha to 255 after fade out
                    alphaFactor = 255
                    isFadedOut = true
                    invalidate()
                }
            })
        }
        animator.start()
    }
}