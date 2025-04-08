package com.vs.schoolmessenger.Utils

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import androidx.core.graphics.toColorInt

object ShimmerUtil {

    private val originalText = mutableMapOf<TextView, CharSequence>()
    private val originalTextColors = mutableMapOf<TextView, Int>()

    fun wrapWithShimmer(parent: ViewGroup, layoutResId: Int): View {
        val shimmerLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.shimmer_wrapper, parent, false) as ShimmerFrameLayout
        val contentView = LayoutInflater.from(parent.context)
            .inflate(layoutResId, shimmerLayout, true)
        hideViewsDuringShimmer(contentView)
        return contentView
    }

    fun startShimmer(view: View) {
        if (view is ShimmerFrameLayout) {
            hideViewsDuringShimmer(view)
            view.startShimmer()
        }
    }

    fun stopShimmer(view: View) {
        if (view is ShimmerFrameLayout) {
            view.stopShimmer()
            view.visibility = View.GONE
            restoreViewsAfterShimmer(view)
        }
    }

    private fun hideViewsDuringShimmer(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                when (child) {
                    is TextView -> {
                        if (!originalText.containsKey(child)) {
                            originalText[child] = child.text
                            originalTextColors[child] = child.currentTextColor
                        }

                        child.setBackgroundColor("#DDDDDD".toColorInt())
                    }

                    is ViewGroup -> hideViewsDuringShimmer(child)
                }
            }
        }
    }

    private fun restoreViewsAfterShimmer(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                when (child) {
                    is TextView -> {
                        if (originalText.containsKey(child)) {
                            child.text = originalText[child]
                            child.background = null
                            originalText.remove(child)
                            originalTextColors.remove(child)
                        }
                    }
                    is ViewGroup -> restoreViewsAfterShimmer(child)
                }
            }
        }
    }
}
