package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class FlowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 0
        var height = 0
        var lineWidth = 0
        var lineHeight = 0
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            if (lineWidth + child.measuredWidth > maxWidth) {
                width = maxOf(width, lineWidth)
                height += lineHeight
                lineWidth = child.measuredWidth
                lineHeight = child.measuredHeight
            } else {
                lineWidth += child.measuredWidth
                lineHeight = maxOf(lineHeight, child.measuredHeight)
            }
        }

        width = maxOf(width, lineWidth)
        height += lineHeight
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var x = 0
        var y = 0
        var lineHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (x + child.measuredWidth > measuredWidth) {
                x = 0
                y += lineHeight
                lineHeight = child.measuredHeight
            }

            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            x += child.measuredWidth
            lineHeight = maxOf(lineHeight, child.measuredHeight)
        }
    }
}
