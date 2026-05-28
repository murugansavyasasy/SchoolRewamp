package com.vs.schoolmessenger.School.StudentDetails.Charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class YAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val padding = 100f
    private val maxValue = 100

    private val axisPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 32f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val chartHeight = height - padding * 2
        val axisX = width.toFloat() - 10f

        canvas.drawLine(
            axisX,
            padding,
            axisX,
            height - padding,
            axisPaint
        )

        // Draw marks
        for (i in 0..4) {
            val value = i * 25
            val y = height - padding - (value / maxValue.toFloat()) * chartHeight

            canvas.drawLine(
                axisX - 25f,   // start little left
                y,
                axisX,         // end exactly at vertical axis
                y,
                axisPaint
            )

            // Draw mark number
            canvas.drawText(
                value.toString(),
                10f,
                y + 10f,
                textPaint
            )
        }
    }
}