package com.vs.schoolmessenger.Parent.EventsHolidays

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CustomDateItem (
    val day: Int?,
    val isSelectable: Boolean,
    val isHoliday: Boolean = false
) {
    fun getFormattedDate(): String? {
        if (day == null) return null
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}

