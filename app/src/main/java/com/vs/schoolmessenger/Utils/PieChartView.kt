package com.vs.schoolmessenger.Utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class PieChartView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rect = RectF()
    private var animatedSweepAngle = 0f // Current animated angle

    // Properties for data input
    private var data: List<Pair<Float, Int>> = listOf()

    init {
        startAnimation()
    }

    // Setter for data
    fun setData(newData: List<Pair<Float, Int>>) {
        data = newData
        startAnimation()
    }

    // Start the pie chart loading animation
    private fun startAnimation() {
        val animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2000 // Animation duration in milliseconds
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animatedSweepAngle = animation.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Set up dimensions for the pie chart
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2 * 0.8f
        rect.set(
            width / 2 - radius,
            height / 2 - radius,
            width / 2 + radius,
            height / 2 + radius
        )

        var startAngle = 0f
        var currentSweepAngle = 0f

        for ((percentage, color) in data) {
            paint.color = color
            val sliceSweepAngle = (percentage / 100) * 360

            // Draw each segment up to the animated angle
            if (currentSweepAngle + sliceSweepAngle > animatedSweepAngle) {
                val remainingSweep = animatedSweepAngle - currentSweepAngle
                canvas.drawArc(rect, startAngle, remainingSweep, true, paint)
                break
            } else {
                canvas.drawArc(rect, startAngle, sliceSweepAngle, true, paint)
                startAngle += sliceSweepAngle
                currentSweepAngle += sliceSweepAngle
            }
        }
    }
}