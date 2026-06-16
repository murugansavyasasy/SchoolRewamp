package com.vs.schoolmessenger.Utils

import android.view.View

data class TourStep(
    val targetView: View,
    val iconRes: Int,
    val title: String,
    val description: String,
    val hint: String
)
