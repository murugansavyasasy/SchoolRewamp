package com.vs.schoolmessenger.Parent.Hostel

import android.content.Context

object AttendanceUIConfig {

    // ================== CHANGE ONLY HERE ==================
    var COLUMN_WIDTH_DP = 100    // column width
    var IMAGE_SIZE_DP = 20       // image size
    var PADDING_H_DP = 10         // horizontal padding
    var PADDING_V_DP = 0         // vertical padding
    // ======================================================

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun columnWidth(context: Context): Int {
        val minWidth = IMAGE_SIZE_DP + (PADDING_H_DP * 2)
        val finalDp = maxOf(COLUMN_WIDTH_DP, minWidth)
        return dpToPx(context, finalDp)
    }

    fun imageSize(context: Context) = dpToPx(context, IMAGE_SIZE_DP)
    fun paddingH(context: Context) = dpToPx(context, PADDING_H_DP)
    fun paddingV(context: Context) = dpToPx(context, PADDING_V_DP)
}