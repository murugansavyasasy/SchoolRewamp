package com.vs.schoolmessenger.Utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Global helper for "filter by date" screens.
 * Default range = last 1 year  ->  from = today - 1 year, to = today.
 * Date format is dd-MM-yyyy (same as DailyCollection / calendar dialog).
 */
object DateFilterHelper {

    const val API_FORMAT = "dd-MM-yyyy"

    private fun formatter() = SimpleDateFormat(API_FORMAT, Locale.ENGLISH)

    fun today(): String = formatter().format(Calendar.getInstance().time)

    fun oneYearBackFromToday(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        return formatter().format(cal.time)
    }

    /** Pair(from, to) */
    fun defaultRange(): Pair<String, String> = Pair(oneYearBackFromToday(), today())

    fun toMillis(date: String?): Long {
        if (date.isNullOrEmpty()) return 0L
        return try {
            formatter().parse(date)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}