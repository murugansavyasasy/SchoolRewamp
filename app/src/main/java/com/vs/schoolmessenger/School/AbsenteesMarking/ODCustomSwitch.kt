package com.vs.schoolmessenger.School.AbsenteesMarking

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

class ODCustomSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isChecked = false
    private var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var switchRadius = 0f
    private var switchWidth = 0f

    private val activeColor = ContextCompat.getColor(context, R.color.light_yellow_5)
    private val inactiveColor = ContextCompat.getColor(context, R.color.grey_mild)

    private var listener: ((Boolean) -> Unit)? = null
    private var suppressListener = false // ✅ prevent recursion

    init {
        circlePaint.color = ContextCompat.getColor(context, R.color.white)
        backgroundPaint.color = inactiveColor
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
        canvas.drawRoundRect(
            0f, 0f, switchWidth, switchRadius * 2,
            switchRadius, switchRadius, backgroundPaint
        )
        val circleX = if (isChecked) switchWidth - switchRadius else switchRadius
        canvas.drawCircle(circleX, switchRadius, switchRadius - 10, circlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && isEnabled) {
            toggle()
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setChecked(checked: Boolean) {
        if (isChecked == checked) return // no redundant redraws

        suppressListener = true
        isChecked = checked
        backgroundPaint.color = if (isChecked) activeColor else inactiveColor
        invalidate()
        suppressListener = false
    }

    fun isChecked(): Boolean = isChecked

    fun setOnCheckedChangeListener(l: (Boolean) -> Unit) {
        listener = l
    }

    private fun toggle() {
        isChecked = !isChecked
        backgroundPaint.color = if (isChecked) activeColor else inactiveColor
        invalidate()
        if (!suppressListener) listener?.invoke(isChecked)
    }
}
