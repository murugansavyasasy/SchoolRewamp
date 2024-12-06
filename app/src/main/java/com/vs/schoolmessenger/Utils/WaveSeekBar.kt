package com.vs.schoolmessenger.Utils
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.R

class WaveSeekBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var waveHeights: FloatArray = FloatArray(0)
    private val playedPaint = Paint() // Paint for the played wave
    private val unplayedPaint = Paint() // Paint for the unplayed wave
    private val circlePaint = Paint() // Paint for the circular indicator
    private var progress: Float = 0f // Seek bar progress (0.0 to 1.0)
    private var circleRadius = 15f // Radius of the circle indicator


    init {
        // Set played wave color from resources
        playedPaint.isAntiAlias = true
        playedPaint.color = ContextCompat.getColor(context, R.color.light_green_bg1)
        playedPaint.style = Paint.Style.FILL // Fill style for played wave

        // Set unplayed wave color from resources
        unplayedPaint.isAntiAlias = true
        unplayedPaint.color = ContextCompat.getColor(context, R.color.grey)
        unplayedPaint.style = Paint.Style.FILL // Fill style for unplayed wave

        // Set circle indicator color from resources
        circlePaint.isAntiAlias = true
        circlePaint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        circlePaint.style = Paint.Style.FILL // Fill style for the circle
    }


    // Set wave heights for drawing
    fun setWaveHeights(heights: FloatArray) {
        waveHeights = heights
        invalidate() // Redraw the view
    }

    // Get current progress
    fun getProgress(): Float {
        return progress
    }

    fun setProgress(newProgress: Float) {
        progress = newProgress.coerceIn(0f, 1f) // Clamp progress between 0 and 1
        invalidate() // Redraw the view to reflect the new progress
        Log.d("WaveSeekBar", "Progress set to $progress")
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (waveHeights.isEmpty()) {
            Log.d("WaveSeekBar", "No wave heights to draw.")
            return
        }

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2
        val step = width / (waveHeights.size - 1)
        val progressIndex = (progress * (waveHeights.size - 1)).toInt()

        for (i in waveHeights.indices) {
            val x = i * step
            val waveHeight = waveHeights[i]
            val top = centerY - waveHeight / 2
            val bottom = centerY + waveHeight / 2
            val paint = if (i <= progressIndex) playedPaint else unplayedPaint
            canvas.drawRect(x, top, x + step, bottom, paint)
        }

        // Draw progress indicator as a circle
        val progressX = progress * width
        canvas.drawCircle(progressX, centerY, circleRadius, circlePaint) // Draw the circle indicator
        Log.d("WaveSeekBar", "Waveform drawn with progress: $progress")
    }

}