package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/**
 * Currency-agnostic amount handling.
 *
 * The API returns amounts as display strings ("₹1,100.00", "$1,100.00", "1.100,00 €",
 * "SAR 1,100.00", "١٬١٠٠٫٠٠ د.إ" ...). Nothing is hardcoded: the symbol/letters (prefix or suffix),
 * grouping separator, decimal separator, decimal count and Arabic digits are all detected from the
 * response itself, so sums are calculated with BigDecimal and shown in the SAME format the API uses.
 */
class AmountFormat(
    private val prefix: String = "",
    private val suffix: String = "",
    private val groupSep: Char = ',',
    private val decimalSep: Char = '.',
    private val decimals: Int = 2,
    private val arabicDigits: Boolean = false
) {

    /** "SAR 1,100.00" -> 1100.00 (BigDecimal, exact) */
    fun parse(raw: String?): BigDecimal {
        if (raw.isNullOrBlank()) return BigDecimal.ZERO
        val match = numberRegex.find(normalizeDigits(raw)) ?: return BigDecimal.ZERO
        val clean = buildString {
            for (c in match.value) {
                when {
                    c in '0'..'9' -> append(c)
                    c == decimalSep && decimals > 0 -> append('.')
                }
            }
        }
        return clean.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    /** 1100 -> "SAR 1,100.00" (same symbol / separators / digits as the API) */
    fun format(value: BigDecimal): String {
        val scaled = value.setScale(decimals, RoundingMode.HALF_UP)
        val plain = scaled.abs().toPlainString()
        val intPart = plain.substringBefore('.')
        val frac = plain.substringAfter('.', "")
        val grouped = intPart.reversed().chunked(3).joinToString(groupSep.toString()).reversed()
        var number = if (decimals > 0) grouped + decimalSep + frac else grouped
        if (arabicDigits) {
            number = number.map { if (it in '0'..'9') '\u0660' + (it - '0') else it }
                .joinToString("")
        }
        val sign = if (scaled.signum() < 0) "-" else ""
        return sign + prefix + number + suffix
    }

    companion object {
        // digits with optional separators inside: . , ٫ ٬ ' space nbsp
        private val numberRegex =
            Regex("[0-9](?:[0-9.,\u066B\u066C'\u00A0\u202F ]*[0-9])?")

        private fun isArabicDigit(c: Char) = c in '\u0660'..'\u0669' || c in '\u06F0'..'\u06F9'

        private fun normalizeDigits(s: String): String = buildString {
            for (c in s) {
                append(
                    when (c) {
                        in '\u0660'..'\u0669' -> '0' + (c - '\u0660')
                        in '\u06F0'..'\u06F9' -> '0' + (c - '\u06F0')
                        else -> c
                    }
                )
            }
        }

        /** Returns the decimal separator only when the number makes it unambiguous. */
        private fun unambiguousDecimal(num: String): Char? {
            if ('\u066B' in num) return '\u066B'
            val li = num.lastIndexOfAny(charArrayOf('.', ','))
            if (li < 0) return null
            val ch = num[li]
            val other = if (ch == '.') ',' else '.'
            val after = num.length - li - 1
            return when {
                num.count { it == ch } > 1 -> null   // repeated -> grouping
                num.contains(other) -> ch            // both present -> last one is decimal
                after != 3 -> ch                     // e.g. "100.00"
                else -> null                         // "1,100" -> grouping
            }
        }

        /**
         * Looks at every amount string in the response (first one gives symbol/prefix/suffix)
         * and works out the format used by the API.
         */
        fun detect(samples: List<String?>): AmountFormat {
            var prefix = ""
            var suffix = ""
            var haveTemplate = false
            var arabic = false
            var decimalSep: Char? = null
            val seps = LinkedHashSet<Char>()
            val nums = mutableListOf<String>()

            for (raw in samples) {
                if (raw.isNullOrBlank()) continue
                if (raw.any { isArabicDigit(it) }) arabic = true
                val s = normalizeDigits(raw)
                val m = numberRegex.find(s) ?: continue
                if (!haveTemplate) {
                    prefix = s.substring(0, m.range.first)
                    suffix = s.substring(m.range.last + 1)
                    haveTemplate = true
                }
                nums.add(m.value)
                m.value.filter { it !in '0'..'9' }.forEach { seps.add(it) }
                if (decimalSep == null) decimalSep = unambiguousDecimal(m.value)
            }
            if (!haveTemplate) return AmountFormat()

            val dec = decimalSep
            var decimals = 0
            if (dec != null) {
                nums.forEach { n ->
                    val i = n.lastIndexOf(dec)
                    if (i >= 0) decimals = max(decimals, n.length - i - 1)
                }
            }
            val group = seps.firstOrNull { it != dec } ?: when (dec) {
                '.' -> ','
                '\u066B' -> '\u066C'
                null -> ','
                else -> '.'
            }
            return AmountFormat(prefix, suffix, group, dec ?: '.', decimals, arabic)
        }
    }
}