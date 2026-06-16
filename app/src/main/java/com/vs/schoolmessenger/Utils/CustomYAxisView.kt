package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomYAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 36f
    }

    private val maxYValue = 50
    private val yStep = 10 // Step for Y-axis labels
    private val yLabels = (0..maxYValue step yStep).toList() // Y-axis labels

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val graphHeight = height - 100f // Deduct padding for better alignment
        val padding = 50f

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawLine(padding, graphHeight + padding, padding, padding, paint) // Y-axis line

        // Draw Y-axis labels
        for (yValue in yLabels) {
            val yPosition = graphHeight + padding - (yValue / maxYValue.toFloat()) * graphHeight
            canvas.drawText(yValue.toString(), padding - 20, yPosition + 10, paint) // Y-axis labels
            canvas.drawLine(padding, yPosition, padding + 10, yPosition, paint) // Y-axis ticks
        }
    }
}
