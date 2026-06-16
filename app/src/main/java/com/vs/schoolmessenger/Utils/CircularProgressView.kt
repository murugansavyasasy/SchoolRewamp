package com.vs.schoolmessenger.Utils


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.vs.schoolmessenger.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var progressBar: ProgressBar
    private var textView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_circular_progress, this, true)
        progressBar = view.findViewById(R.id.circularProgressbar)
        textView = view.findViewById(R.id.lblPresentStatus)
    }

    fun setProgress(progress: Int) {
        progressBar.progress = progress
        textView.text = "$progress%"
    }
}
