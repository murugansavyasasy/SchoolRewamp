package com.vs.schoolmessenger.Utils

import com.vs.schoolmessenger.R
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class DimOverlayManager(private val activity: Activity) {

    private var dimView: View? = null

    init {
        addDimOverlay()
    }

    private fun addDimOverlay() {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        dimView = LayoutInflater.from(activity).inflate(R.layout.layout_dim_overlay, rootView, false)
        dimView?.visibility = View.GONE
        rootView.addView(dimView)
    }

    fun showDim() {
        dimView?.visibility = View.VISIBLE
    }

    fun hideDim() {
        dimView?.visibility = View.GONE
    }
}
