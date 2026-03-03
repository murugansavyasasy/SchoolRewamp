package com.vs.schoolmessenger.School.StudentDetails.Charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.vs.schoolmessenger.R

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<Pair<String, Int>> = emptyList()
    private var popupWindow: PopupWindow? = null
    private val padding = 100f
    private val barWidth = 140f
    private val space = 80f
    private val maxValue = 100

    private var selectedIndex = -1

    private val axisPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val popupPaint = Paint().apply {
        color = Color.BLACK
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val colors = listOf(
        Color.parseColor("#3F51B5"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#4CAF50"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#009688"),
        Color.parseColor("#F44336")
    )

    fun setData(chartData: List<Pair<String, Int>>) {
        data = chartData
        requestLayout()
        invalidate()
    }

    // Abbreviation logic
    private fun getShortName(subject: String): String {
        if (subject.length <= 10) return subject

        val words = subject.trim().split(" ")
        return if (words.size > 1) {
            words.joinToString("") { it.first().uppercase() }
        } else {
            subject.take(3).uppercase()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth =
            (data.size * (barWidth + space) + padding).toInt()

        val width = resolveSize(totalWidth, widthMeasureSpec)
        val height = resolveSize(600, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val chartHeight = height - padding * 2

        // X Axis
        canvas.drawLine(
            0f,
            height - padding,
            width.toFloat(),
            height - padding,
            axisPaint
        )

        data.forEachIndexed { index, pair ->

            val left = index * (barWidth + space)
            val right = left + barWidth

            val barHeight = (pair.second / maxValue.toFloat()) * chartHeight
            val top = height - padding - barHeight
            val bottom = height - padding

            val barPaint = Paint().apply {
                color = colors[index % colors.size]
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // Rounded top bar
            val radius = 25f
            val path = Path().apply {
                moveTo(left, bottom)
                lineTo(left, top + radius)
                quadTo(left, top, left + radius, top)
                lineTo(right - radius, top)
                quadTo(right, top, right, top + radius)
                lineTo(right, bottom)
                close()
            }

            canvas.drawPath(path, barPaint)

            // X-axis indicator line
            canvas.drawLine(
                (left + right) / 2,
                height - padding,
                (left + right) / 2,
                height - padding + 20,
                axisPaint
            )

            // Subject name (short)
            canvas.drawText(
                getShortName(pair.first),
                (left + right) / 2,
                height - padding + 50,
                textPaint
            )
        }

        // Popup when selected
        if (selectedIndex != -1) {
            val pair = data[selectedIndex]
            val left = selectedIndex * (barWidth + space)
            val right = left + barWidth
            val centerX = (left + right) / 2

            canvas.drawText(pair.first, centerX, padding - 20, popupPaint)
            canvas.drawText("${pair.second}%", centerX, padding + 20, popupPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {

            data.forEachIndexed { index, pair ->

                val left = index * (barWidth + space)
                val right = left + barWidth

                if (event.x in left..right) {

                    val barColor = colors[index % colors.size]

                    showPopup(
                        pair,
                        barColor,
                        event.rawX.toInt(),
                        event.rawY.toInt()
                    )

                    return true
                }
            }
        }
        return true
    }

    private fun showPopup(
        pair: Pair<String, Int>,
        barColor: Int,
        x: Int,
        y: Int
    ) {

        popupWindow?.dismiss()

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_chart_popup, null)

        val tvSubject = view.findViewById<TextView>(R.id.tvSubject)
        val tvMarks = view.findViewById<TextView>(R.id.tvMarks)

        tvSubject.text = pair.first
        tvMarks.text = "${pair.second}%"


        val bgDrawable = GradientDrawable().apply {
            setColor(barColor)
            cornerRadius = 20f
        }

        tvSubject.setTextColor(Color.WHITE)
        tvMarks.setTextColor(Color.WHITE)

        view.background = bgDrawable

        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow?.elevation = 12f
        popupWindow?.showAtLocation(this, Gravity.NO_GRAVITY, x - 150, y - 250)
    }
}