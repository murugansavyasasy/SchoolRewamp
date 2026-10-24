package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.ModelConvert

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Works for any country because it copies the style of the API's own amount strings:
 *   "₹1,10,887.00"  "$1,250.50"  "AED 1,000.00"  "د.إ 1,000.00"  "Rs.1,000"  "R$ 1.234,56"
 *   "CHF 1'234.50"  "1 234,56 zł"  "€1.234,56"  "¥1,000"  "-₹100"  "(₹100)"  "١٢٣٫٥٠ ر.س"
 *
 * Rule: the NUMBER is everything from the first digit to the last digit.
 *       Whatever is before it / after it is the currency symbol (any characters at all).
 *
 * - parse()  : any formatted amount -> BigDecimal (exact math)
 * - detect() : learns symbol, position, separators, decimals, digit style from the API strings
 * - format() : prints a calculated value (like a section total) in that same style
 *
 * Amounts that come from the API are shown exactly as sent (see FeeUiMapper.fmt),
 * so this formatter is only used for values WE calculate.
 */
class MoneyFormatter(
    private val symbol: String = "",
    private val symbolBefore: Boolean = true,
    private val space: Boolean = false,
    private val decimalSep: Char = '.',
    private val groupSep: Char = ',',
    private val decimals: Int = 2,
    private val indian: Boolean = false,
    private val zeroDigit: Char = '0'          // '٠' for Arabic-Indic digits, '0' for normal digits
) {

    fun format(value: BigDecimal): String {
        val plain = value.setScale(decimals, RoundingMode.HALF_UP).abs().toPlainString()
        val whole = group(plain.substringBefore('.'))
        val fraction = plain.substringAfter('.', "")
        val number = localDigits(whole) + if (decimals > 0) "$decimalSep${localDigits(fraction)}" else ""

        val gap = if (space) " " else ""
        val text = when {
            symbol.isEmpty() -> number
            symbolBefore -> symbol + gap + number
            else -> number + gap + symbol
        }
        return if (value.signum() < 0) "-$text" else text
    }

    private fun group(digits: String): String {
        if (digits.length <= 3) return digits
        var rest = digits.dropLast(3)
        var out = digits.takeLast(3)
        val size = if (indian) 2 else 3
        while (rest.isNotEmpty()) {
            out = rest.takeLast(size) + groupSep + out
            rest = rest.dropLast(size)
        }
        return out
    }

    private fun localDigits(s: String): String =
        if (zeroDigit == '0') s else s.map { if (it in '0'..'9') zeroDigit + (it - '0') else it }.joinToString("")

    companion object {
        private val INDIAN_SYMBOLS = setOf("₹", "INR", "RS")
        private val LAKH_PATTERN = Regex("^\\d{1,2}(?:[,.]\\d{2})+[,.]\\d{3}")   // 1,10,887

        /** text before the first digit / after the last digit / the number itself */
        private class Parts(val prefix: String, val body: String, val suffix: String)

        private fun isDigit(c: Char) = Character.digit(c, 10) >= 0          // also Arabic, Persian, Devanagari digits
        private fun isSpace(c: Char) = c.isWhitespace() || c == '\u00A0' || c == '\u202F'

        private fun split(raw: String): Parts? {
            val first = raw.indexOfFirst { isDigit(it) }
            if (first < 0) return null
            val last = raw.indexOfLast { isDigit(it) }
            return Parts(raw.substring(0, first), raw.substring(first, last + 1), raw.substring(last + 1))
        }

        /** Same length as [body]: digits -> 0-9, Arabic separators -> . and , (keeps indexes aligned) */
        private fun ascii(body: String): String = body.map { c ->
            when {
                isDigit(c) -> '0' + Character.digit(c, 10)
                c == '٫' -> '.'
                c == '٬' -> ','
                else -> c
            }
        }.joinToString("")

        /** Symbol text = prefix/suffix without signs, brackets and spaces. */
        private fun symbolOf(s: String): String =
            s.filterNot { it == '-' || it == '−' || it == '(' || it == ')' }.trim { isSpace(it) }

        /**
         * Index of the decimal separator inside [m] (output of ascii()), or -1.
         *  - both "." and "," present      -> the LAST one is the decimal ("1.234,56", "1,10,887.00")
         *  - one kind, used more than once -> grouping ("1,10,887")
         *  - one kind, used once           -> decimal if 1-2 digits follow ("1,50"), grouping if 3 ("1,000")
         */
        private fun decimalIndex(m: String): Int {
            val dot = m.lastIndexOf('.')
            val comma = m.lastIndexOf(',')
            if (dot < 0 && comma < 0) return -1
            if (dot >= 0 && comma >= 0) return maxOf(dot, comma)
            val i = maxOf(dot, comma)
            if (m.count { it == m[i] } > 1) return -1
            return if (m.length - i - 1 in 1..2) i else -1
        }

        /** Any formatted amount -> BigDecimal. null / invalid -> 0 */
        fun parse(raw: String?): BigDecimal {
            if (raw.isNullOrBlank()) return BigDecimal.ZERO
            val parts = split(raw) ?: return BigDecimal.ZERO
            val m = ascii(parts.body)
            val i = decimalIndex(m)
            val whole = (if (i >= 0) m.substring(0, i) else m).filter { it in '0'..'9' }
            val fraction = if (i >= 0) m.substring(i + 1).filter { it in '0'..'9' } else ""
            val value = "${whole.ifEmpty { "0" }}.${fraction.ifEmpty { "0" }}".toBigDecimalOrNull()
                ?: return BigDecimal.ZERO
            val negative = raw.any { it == '-' || it == '−' } || raw.trim().startsWith("(")
            return if (negative) value.negate() else value
        }

        /** Learns the style from the API's amount strings. */
        fun detect(samples: List<String?>): MoneyFormatter {
            val parts = samples.mapNotNull { s -> s?.trim()?.let { split(it) } }
            if (parts.isEmpty()) return MoneyFormatter()

            // ---- symbol, position, space ----
            var symbol = ""
            var before = true
            var space = false
            val withSymbol = parts.firstOrNull { symbolOf(it.prefix).isNotEmpty() || symbolOf(it.suffix).isNotEmpty() }
            if (withSymbol != null) {
                before = symbolOf(withSymbol.prefix).isNotEmpty()
                if (before) {
                    symbol = symbolOf(withSymbol.prefix)
                    space = withSymbol.prefix.lastOrNull()?.let { isSpace(it) } == true
                } else {
                    symbol = symbolOf(withSymbol.suffix)
                    space = withSymbol.suffix.firstOrNull()?.let { isSpace(it) } == true
                }
            }

            // ---- decimal / group separators, decimals, digit style ----
            var decimalSep = '.'
            var decimals = -1
            var groupSep: Char? = null
            var zero = '0'
            var lakh = false
            for (p in parts) {
                val m = ascii(p.body)
                val i = decimalIndex(m)
                if (i >= 0) {
                    if (decimals < 0) decimalSep = p.body[i]
                    decimals = maxOf(decimals, m.length - i - 1)
                }
                if (LAKH_PATTERN.containsMatchIn(m)) lakh = true
                p.body.forEachIndexed { idx, c ->
                    val digit = Character.digit(c, 10)
                    if (digit >= 0) {
                        if (c !in '0'..'9' && zero == '0') zero = c - digit
                    } else if (idx != i && groupSep == null) {
                        groupSep = c
                    }
                }
            }
            if (decimals < 0) decimals = 0     // API never shows decimals (e.g. ¥1,000)

            val defaultGroup = when (decimalSep) { '.' -> ','; '٫' -> '٬'; else -> '.' }

            return MoneyFormatter(
                symbol = symbol,
                symbolBefore = before,
                space = space,
                decimalSep = decimalSep,
                groupSep = groupSep ?: defaultGroup,
                decimals = decimals,
                indian = lakh || symbol.trim('.').uppercase() in INDIAN_SYMBOLS,
                zeroDigit = zero
            )
        }
    }
}