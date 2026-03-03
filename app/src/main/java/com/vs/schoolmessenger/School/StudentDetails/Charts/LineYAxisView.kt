package com.vs.schoolmessenger.School.StudentDetails.Charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LineYAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paddingTop = 100f
    private val paddingBottom = 140f
    private val maxValue = 100

    private val axisPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val chartHeight = height - paddingTop - paddingBottom
        val xAxisY = height - paddingBottom
        val axisX = width.toFloat()   // Right edge

        // Vertical Y-axis line
        canvas.drawLine(
            axisX,
            paddingTop,
            axisX,
            xAxisY,
            axisPaint
        )

        val marks = listOf(25, 50, 75, 100)

        marks.forEach { value ->
            val y = xAxisY - (value / maxValue.toFloat()) * chartHeight

            // Tick line
            canvas.drawLine(
                axisX - 20f,
                y,
                axisX,
                y,
                axisPaint
            )

            canvas.drawText(
                value.toString(),
                10f,
                y + 10f,
                textPaint
            )
        }
    }
}