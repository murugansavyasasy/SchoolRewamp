package com.vs.schoolmessenger.Utils


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class CustomPieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFA726") // Orange Color
        style = Paint.Style.STROKE
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 60f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val rect = RectF()
    private var progress = 0f // Current progress percentage

    fun setProgress(value: Float) {
        val animator = ValueAnimator.ofFloat(0f, value).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) * 0.8f
        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Draw gray background ring
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // Draw progress ring
        val sweepAngle = (progress / 100) * 360
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Draw percentage text in center
        canvas.drawText("${progress.toInt()}%", centerX, centerY + textPaint.textSize / 3, textPaint)
    }
}
