package com.vs.schoolmessenger.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

object ShimmerUtil {

    fun wrapWithShimmer(parent: ViewGroup, layoutResId: Int): View {
        // Inflate shimmer container
        val shimmerLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.shimmer_wrapper, parent, false) as ShimmerFrameLayout

        // Inflate the actual layout inside shimmer
        val contentView = LayoutInflater.from(parent.context)
            .inflate(layoutResId, shimmerLayout, true)

        return shimmerLayout
    }

    fun startShimmer(view: View) {
        if (view is ShimmerFrameLayout) {
            view.startShimmer()
        }
    }

    fun stopShimmer(view: View) {
        if (view is ShimmerFrameLayout) {
            view.stopShimmer()
            view.visibility = View.GONE
        }
    }
}
