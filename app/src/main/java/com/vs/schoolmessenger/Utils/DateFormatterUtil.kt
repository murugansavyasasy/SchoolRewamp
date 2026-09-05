package com.vs.schoolmessenger.Utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatterUtil {
    private const val PATTERN = "dd-MM-yyyy"


    fun normalizeToApiFormat(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val normalized = when (ch) {
                in '٠'..'٩' -> '0' + (ch - '٠')
                in '۰'..'۹' -> '0' + (ch - '۰')
                else -> ch
            }
            sb.append(normalized)
        }
        return sb.toString()
    }
}