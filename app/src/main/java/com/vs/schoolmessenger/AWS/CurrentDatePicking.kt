package com.vs.schoolmessenger.AWS

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrentDatePicking {
    val currentDate: String
        get() {
            // Define the date format you need
            val sdf =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            // Get the current date
            val currentDate = Date()
            // Format and return the current date
            return sdf.format(currentDate)
        }
}
