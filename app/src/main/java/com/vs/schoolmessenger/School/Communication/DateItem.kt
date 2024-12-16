package com.vs.schoolmessenger.School.Communication

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DateItem(
    val day: Int?, // Day of the month (e.g., 12)
    val isSelectable: Boolean // Whether the date is selectable
) {
    // Method to get the formatted date (dd-MM-yyyy)
    fun getFormattedDate(): String? {
        if (day == null) return null
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        // Format date as dd-MM-yyyy
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}

