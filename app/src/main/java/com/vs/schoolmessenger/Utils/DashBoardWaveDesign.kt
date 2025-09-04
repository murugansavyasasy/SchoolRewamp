package com.vs.schoolmessenger.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R
import kotlin.math.sin

class DashBoardWaveDesign @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Use your custom bpWhite color
    private val waveColor = ContextCompat.getColor(context, R.color.bpWhite)

    private val wavePaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveColor
        alpha = 80
        style = Paint.Style.FILL
    }
    private val wavePaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveColor
        alpha = 80
        style = Paint.Style.FILL
    }
    private val wavePaint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveColor
        alpha = 250
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalHeight = height.toFloat()
        val waveLength = width.toFloat() * 2.35f
        val waveHeight = 30f

        drawWave(
            canvas,
            wavePaint1,
            waveLength,
            waveHeight,
            offsetY = totalHeight - 95,
            phaseShift = 85f
        )
        drawWave(
            canvas,
            wavePaint2,
            waveLength,
            waveHeight,
            offsetY = totalHeight - 95,
            phaseShift = 500f
        )
        drawWave(
            canvas,
            wavePaint3,
            waveLength,
            waveHeight,
            offsetY = totalHeight - 75,
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
            val y = (waveHeight * sin((x + phaseShift) * Math.PI * 9.85 / waveLength)).toFloat()
            path.lineTo(x.toFloat(), offsetY + y)
        }
        path.lineTo(viewWidth, viewHeight)
        path.close()
        canvas.drawPath(path, paint)
    }
}
