package com.vs.schoolmessenger.Utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatterUtil {
    private const val PATTERN = "dd-MM-yyyy"


    fun normalizeToApiFormat(dateStr: String): String {
        return dateStr.map { ch ->
            when (ch) {
                in '٠'..'٩' -> ('0' + (ch - '٠'))
                in '۰'..'۹' -> ('0' + (ch - '۰'))
                else -> ch
            }
        }.joinToString("")
    }
}