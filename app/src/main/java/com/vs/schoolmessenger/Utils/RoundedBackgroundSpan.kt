package com.vs.schoolmessenger.Utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

class RoundedBackgroundSpan(
    private val backgroundColor: Int,
    private val textColor: Int,
    private val cornerRadius: Float = 20f,
    private val padding: Float = 20f
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return (padding + paint.measureText(
            text.subSequence(start, end).toString()
        ) + padding).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val textToDraw = text.subSequence(start, end).toString()

        val width = paint.measureText(textToDraw)
        val rect = RectF(
            x,
            top.toFloat(),
            x + width + 2 * padding,
            bottom.toFloat()
        )

        // Background
        val oldColor = paint.color
        paint.color = backgroundColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        // Text
        paint.color = textColor
        canvas.drawText(textToDraw, x + padding, y.toFloat(), paint)
        paint.color = oldColor
    }
}
