package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.vs.schoolmessenger.R
import kotlin.lazy

class GuidedTourManager(
    private val activity: Activity,
    private val steps: List<TourStep>
) {

    private var index = 0
    private lateinit var overlay: SpotlightOverlayView
    private lateinit var tourView: View
    private val root by lazy { activity.findViewById<FrameLayout>(R.id.rootContainer) }

    fun start() {
//        if (TourPref.isShown(activity)) return

        tourView = activity.layoutInflater.inflate(R.layout.layout_guided_tour, root, false)
        root.addView(tourView)

        tourView.findViewById<View>(R.id.btnNext).setOnClickListener {
            index++
            showStep()
        }

        tourView.findViewById<View>(R.id.btnSkip).setOnClickListener {
            end()
        }

        showStep()
    }

    private fun showStep() {
        if (::overlay.isInitialized) root.removeView(overlay)

        if (index >= steps.size) {
            end()
            return
        }

        val step = steps[index]

        overlay = SpotlightOverlayView(activity, step.targetView)
        root.addView(overlay, 0)

        tourView.findViewById<ImageView>(R.id.imgIcon).setImageResource(step.iconRes)
        tourView.findViewById<TextView>(R.id.txtTitle).text = step.title
        tourView.findViewById<TextView>(R.id.txtDesc).text = step.description
        tourView.findViewById<TextView>(R.id.txtHint).text = step.hint
    }

    private fun end() {
        root.removeView(tourView)
        if (::overlay.isInitialized) root.removeView(overlay)
      //  TourPref.markShown(activity)
    }
}
