package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View

class SpotlightOverlayView(
    context: Context,
    private val target: View
) : View(context) {

    private val dimPaint = Paint().apply {
        color = 0xB3000000.toInt()
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.parseColor("#2F80FF")
        maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.OUTER)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

        val loc = IntArray(2)
        target.getLocationOnScreen(loc)

        val rect = RectF(
            loc[0] - 12f,
            loc[1] - 12f,
            loc[0] + target.width + 12f,
            loc[1] + target.height + 12f
        )

        canvas.drawRoundRect(rect, 24f, 24f, glowPaint)
        canvas.drawRoundRect(rect, 24f, 24f, clearPaint)
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
}

