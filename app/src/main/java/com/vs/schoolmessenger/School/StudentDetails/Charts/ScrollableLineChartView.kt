package com.vs.schoolmessenger.School.StudentDetails.Charts

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.vs.schoolmessenger.School.StudentDetails.InterFace.OnPointClickListener
import kotlin.math.hypot

class ScrollableLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var pointClickListener: OnPointClickListener? = null
    private var data: List<Pair<String, Int>> = emptyList()

    private val spaceX = 250f
    private val paddingTop = 100f
    private val paddingBottom = 140f
    private val startX = 80f
    private val maxValue = 100

    private val points = mutableListOf<PointF>()

    private val linePaint = Paint().apply {
        color = Color.parseColor("#3F51B5")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = Color.parseColor("#3F51B5")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun setData(chartData: List<Pair<String, Int>>) {
        data = chartData
        requestLayout()
        invalidate()
    }

    fun setOnPointClickListener(listener: OnPointClickListener) {
        pointClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth =
            if (data.isNotEmpty())
                (startX + (data.size - 1) * spaceX + 150f).toInt()
            else 500

        val width = resolveSize(totalWidth, widthMeasureSpec)
        val height = resolveSize(700, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {

            for (i in points.indices) {

                val point = points[i]

                val distance = hypot(
                    (event.x - point.x),
                    (event.y - point.y)
                )

                if (distance < 60) {   // touch radius

                    val item = data[i]

                    pointClickListener?.onPointClicked(
                        item.first,
                        item.second,
                        point.x,
                        point.y
                    )
                    return true
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val chartHeight = height - paddingTop - paddingBottom
        val xAxisY = height - paddingBottom
        val chartEndX = startX + (data.size - 1) * spaceX

        canvas.drawRect(
            startX,
            paddingTop,
            chartEndX,
            xAxisY,
            gridPaint
        )

        // Horizontal grid lines
        val marks = listOf(25, 50, 75, 100)

        marks.forEach { value ->
            val y = xAxisY - (value / maxValue.toFloat()) * chartHeight
            canvas.drawLine(startX, y, chartEndX, y, gridPaint)
        }

        // Vertical grid lines
        for (i in data.indices) {
            val x = startX + i * spaceX
            canvas.drawLine(x, paddingTop, x, xAxisY, gridPaint)
        }
        points.clear()

        data.forEachIndexed { index, pair ->

            val x = startX + index * spaceX
            val y = xAxisY - (pair.second / maxValue.toFloat()) * chartHeight

            points.add(PointF(x, y))

            canvas.drawCircle(x, y, 10f, pointPaint)

            canvas.drawText(pair.first, x, xAxisY + 50f, textPaint)

            canvas.drawText(pair.second.toString(), x, y - 20f, textPaint)
        }

        if (points.size > 1) {

            val linePath = Path()
            val fillPath = Path()

            linePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {

                val prev = points[i - 1]
                val current = points[i]

                val midX = (prev.x + current.x) / 2

                linePath.cubicTo(
                    midX, prev.y,
                    midX, current.y,
                    current.x, current.y
                )

                fillPath.cubicTo(
                    midX, prev.y,
                    midX, current.y,
                    current.x, current.y
                )
            }

            fillPath.lineTo(points.last().x, xAxisY)
            fillPath.lineTo(points.first().x, xAxisY)
            fillPath.close()

            val gradient = LinearGradient(
                0f,
                paddingTop,
                0f,
                height.toFloat(),
                Color.parseColor("#553F51B5"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )

            val fillPaint = Paint().apply {
                shader = gradient
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            canvas.drawPath(fillPath, fillPaint)
            canvas.drawPath(linePath, linePaint)
        }
    }
}