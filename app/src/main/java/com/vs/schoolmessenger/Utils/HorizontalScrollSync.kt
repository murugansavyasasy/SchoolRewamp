package com.vs.schoolmessenger.Utils

import android.widget.HorizontalScrollView

object HorizontalScrollSync {

    private val views = mutableListOf<HorizontalScrollView>()
    private var isSyncing = false
    var scrollX = 0

    fun bind(scrollView: HorizontalScrollView) {
        if (!views.contains(scrollView)) {
            views.add(scrollView)
        }

        scrollView.post { scrollView.scrollTo(scrollX, 0) }

        scrollView.setOnScrollChangeListener { _, x, _, _, _ ->
            if (isSyncing) return@setOnScrollChangeListener
            isSyncing = true
            scrollX = x
            views.forEach {
                if (it != scrollView) it.scrollTo(x, 0)
            }
            isSyncing = false
        }
    }
}

