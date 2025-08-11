package com.vs.schoolmessenger.Parent.EventsHolidays

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import androidx.core.content.ContextCompat

class DrawableBackgroundSpan(private val drawable: Drawable) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
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
        val textWidth = paint.measureText(text, start, end)
        val textHeight = paint.descent() - paint.ascent()
        val drawableTop = (y + paint.ascent()).toInt()
        val drawableBottom = (drawableTop + textHeight).toInt()

        drawable.setBounds(x.toInt(), drawableTop, (x + textWidth).toInt(), drawableBottom)
        drawable.draw(canvas)

        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}
