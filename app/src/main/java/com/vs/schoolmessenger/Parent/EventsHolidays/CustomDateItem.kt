package com.vs.schoolmessenger.Parent.EventsHolidays

import com.vs.schoolmessenger.Utils.Constant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CustomDateItem(
    val day: Int?,
    val month: Int,
    val year: Int,
    val isSelectable: Boolean,
    val isHoliday: Boolean = false,
    val isSunday: Boolean = false,
) {
    fun getFormattedDate(): String? {
        if (day == null) return null
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dateFormat = SimpleDateFormat(Constant.yyyy_MM_dd, Locale.ENGLISH)
        return dateFormat.format(calendar.time)
    }
}
