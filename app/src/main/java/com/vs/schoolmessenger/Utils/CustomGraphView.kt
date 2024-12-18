package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View



class CustomGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 36f
    }

    // Public properties for data and labels
    var data: List<Int> = listOf()
    var xLabels: List<String> = listOf()

    // Graph configuration
    private val maxYValue = 100 // Updated for your requirement
    private val yStep = 10 // Step for Y-axis labels
    private val yLabels = (0..maxYValue step yStep).toList() // Y values from 0 to 100

    // Bar and axis dimensions
    private val barWidth = 150f // Width of each bar in pixels

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Dynamically set the width and height of the view
        val desiredWidth = (data.size * barWidth).toInt() + paddingStart + paddingEnd
        val desiredHeight = 500 // Fixed height to show the full Y-axis
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // View dimensions
        val graphHeight = height - 200f // Deduct padding for better alignment
        val padding = 100f

        // Max bar height
        val maxBarHeight = graphHeight - 2 * padding

        // Draw Y-axis and labels
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawLine(padding, graphHeight - padding, padding, padding, paint)

        // Y-axis labels
        for ((index, yValue) in yLabels.withIndex()) {
            val yPosition = graphHeight - padding - (yValue / maxYValue.toFloat()) * maxBarHeight

            // Draw labels and ticks
            canvas.drawText(yValue.toString(), padding - 20, yPosition + 10, paint)
            canvas.drawLine(padding, yPosition, padding + 10, yPosition, paint)
        }

        // Draw X-axis and bars
        paint.textAlign = Paint.Align.CENTER
        for (i in data.indices) {
            val barHeight = (data[i] / maxYValue.toFloat()) * maxBarHeight
            paint.color = Color.BLUE

            val left = padding + i * barWidth
            val top = graphHeight - padding - barHeight
            val right = left + barWidth * 0.8f
            val bottom = graphHeight - padding

            // Draw bar
            canvas.drawRect(left, top, right, bottom, paint)

            // Draw bar values
            paint.color = Color.BLACK
            canvas.drawText(data[i].toString(), left + barWidth * 0.4f, top - 10, paint)

            // Draw X-axis labels
            if (i < xLabels.size) {
                canvas.drawText(xLabels[i], left + barWidth * 0.4f, graphHeight - padding + 40, paint)
            }
        }
    }
}


//class CustomGraphView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : View(context, attrs, defStyleAttr) {
//
//    private val paint = Paint().apply {
//        isAntiAlias = true
//        textSize = 36f
//    }
//
//    // Public properties for data and labels
//    var data: List<Int> = listOf()
//    var xLabels: List<String> = listOf()
//
//    // Graph configuration
//    private val maxYValue = 500
//    private val barWidth = 150f // Width of each bar in pixels
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val desiredWidth = (data.size * barWidth).toInt() + paddingStart + paddingEnd
//        val desiredHeight = 600 // Adjust as needed for your UI
//        setMeasuredDimension(desiredWidth, desiredHeight)
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        // View dimensions
//        val graphHeight = height - 100f // Adjust for padding
//        val padding = 100f
//
//        // Draw X-axis and bars
//        paint.textAlign = Paint.Align.CENTER
//        for (i in data.indices) {
//            val barHeight = (data[i] / maxYValue.toFloat()) * (graphHeight - padding)
//
//            val left = padding + i * barWidth
//            val top = graphHeight - barHeight
//            val right = left + barWidth * 0.8f
//            val bottom = graphHeight
//
//            // Draw bar
//            paint.color = Color.BLUE
//            canvas.drawRect(left, top, right, bottom, paint)
//
//            // Draw bar values
//            paint.color = Color.BLACK
//            canvas.drawText(data[i].toString(), left + barWidth * 0.4f, top - 10, paint)
//
//            // Draw X-axis labels
//            if (i < xLabels.size) {
//                canvas.drawText(xLabels[i], left + barWidth * 0.4f, graphHeight + 40, paint)
//            }
//        }
//    }
//}
