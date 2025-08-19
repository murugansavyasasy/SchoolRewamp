package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class DashBoardWaveDesign @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 60
        style = Paint.Style.FILL
    }
    private val wavePaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 80
        style = Paint.Style.FILL
    }

    private val wavePaint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 200
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalHeight = height.toFloat()
        val waveLength = width.toFloat() * 1.99f
        val waveHeight = 35f

        drawWave(
            canvas,
            wavePaint1,
            waveLength,
            waveHeight,
            offsetY = totalHeight - 90,
            phaseShift = 150f
        )
        drawWave(
            canvas,
            wavePaint2,
            waveLength,
            waveHeight,
            offsetY = totalHeight - 65,
            phaseShift = 450f
        )
        drawWave(
            canvas,
            wavePaint3,
            waveLength,
            waveHeight,
            offsetY = totalHeight - 45,
            phaseShift = 700f
        )
    }

    private fun drawWave(
        canvas: Canvas,
        paint: Paint,
        waveLength: Float,
        waveHeight: Float,
        offsetY: Float,
        phaseShift: Float
    ) {
        val path = Path()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        path.moveTo(0f, viewHeight)
        for (x in 0..viewWidth.toInt()) {
            val y = (waveHeight * sin((x + phaseShift) * Math.PI * 10 / waveLength)).toFloat()
            path.lineTo(x.toFloat(), offsetY + y)
        }
        path.lineTo(viewWidth, viewHeight)
        path.close()

        canvas.drawPath(path, paint)
    }
}
