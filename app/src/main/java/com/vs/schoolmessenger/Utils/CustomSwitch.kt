package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

class CustomSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isChecked = false
    private var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var switchRadius = 0f
    private var switchWidth = 0f

    // Colors from resources
    private val activeColor = ContextCompat.getColor(context, R.color.light_dark_blue)
    private val inactiveColor = ContextCompat.getColor(context, R.color.grey_mild)

    init {
        circlePaint.color = ContextCompat.getColor(context, R.color.white)
        backgroundPaint.color = inactiveColor // Initial color when the switch is off
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = width.coerceAtMost(height)
        setMeasuredDimension(size * 2, size)
        switchRadius = size / 2f
        switchWidth = (size * 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawRoundRect(
            0f, 0f, switchWidth, switchRadius * 2,
            switchRadius, switchRadius, backgroundPaint
        )

        // Draw circle
        val circleX = if (isChecked) switchWidth - switchRadius else switchRadius
        canvas.drawCircle(circleX, switchRadius, switchRadius - 10, circlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            isChecked = !isChecked

            // Toggle between active and inactive colors
            backgroundPaint.color = if (isChecked) activeColor else inactiveColor

            invalidate() // Redraw the view
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun setChecked(checked: Boolean) {
        isChecked = checked
        backgroundPaint.color = if (isChecked) activeColor else inactiveColor
        invalidate()
    }

    fun isChecked(): Boolean = isChecked
}
