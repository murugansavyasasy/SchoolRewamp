package com.vs.schoolmessenger.Utils

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class ThreeDotsLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var dotRadius = 20f
    private var dotSpacing = 40f
    private val scaleFactors = floatArrayOf(1f, 1f, 1f)

    // Allow different colors for each dot
    private val dotPaints = arrayOf(
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE91E63.toInt() }, // pink
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF3F51B5.toInt() }, // indigo
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFC107.toInt() }  // amber
    )

    private val animators = mutableListOf<ObjectAnimator>()

    init {
        for (i in 0..2) {
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat("scale$i", 0.5f, 1.2f, 0.5f)
            )
            animator.duration = 600
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.interpolator = LinearInterpolator()
            animator.startDelay = (i * 200).toLong()
            animators.add(animator)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = ((dotRadius * 2) * 3 + dotSpacing * 2).toInt()
        val height = (dotRadius * 2).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        var startX = dotRadius

        for (i in 0..2) {
            val scale = scaleFactors[i]
            canvas.drawCircle(startX, centerY, dotRadius * scale, dotPaints[i])
            startX += (dotRadius * 2 + dotSpacing)
        }
    }

    // animator setters
    @Suppress("unused")
    fun setScale0(value: Float) { scaleFactors[0] = value; invalidate() }
    @Suppress("unused")
    fun setScale1(value: Float) { scaleFactors[1] = value; invalidate() }
    @Suppress("unused")
    fun setScale2(value: Float) { scaleFactors[2] = value; invalidate() }

    fun start() {
        animators.forEach { if (!it.isRunning) it.start() }
    }

    fun stop() {
        animators.forEach { it.cancel() }
    }

    fun setDotRadius(radius: Float) {
        dotRadius = radius
        requestLayout()
    }

    fun setDotSpacing(spacing: Float) {
        dotSpacing = spacing
        requestLayout()
    }

    /** Change dot colors dynamically */
    fun setDotColors(colors: IntArray) {
        for (i in 0..2) {
            dotPaints[i].color = colors.getOrNull(i) ?: dotPaints[i].color
        }
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }
}

