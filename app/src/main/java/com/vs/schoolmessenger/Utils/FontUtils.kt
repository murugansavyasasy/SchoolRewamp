package com.vs.schoolmessenger.Utils

import android.graphics.Typeface
import android.widget.TextView

object FontUtils {

    /**
     * Returns true if the given text contains characters outside Poppins' glyph coverage
     * (Arabic block: U+0600–U+06FF, plus Arabic Presentation Forms).
     */
    private fun containsNonLatinScript(text: String): Boolean {
        return text.any { ch ->
            val code = ch.code
            code in 0x0600..0x06FF ||   // Arabic
                    code in 0x0750..0x077F ||   // Arabic Supplement
                    code in 0xFB50..0xFDFF ||   // Arabic Presentation Forms-A
                    code in 0xFE70..0xFEFF      // Arabic Presentation Forms-B
        }
    }

    /**
     * Applies a script-safe typeface to a TextView. Falls back to the system
     * default font (which has full Arabic coverage) if the text can't be
     * rendered by the custom font; otherwise keeps the existing custom font.
     */
    fun applyScriptSafeFont(textView: TextView, customTypeface: Typeface?) {
        val text = textView.text?.toString().orEmpty()
        textView.typeface = if (containsNonLatinScript(text)) {
            Typeface.DEFAULT // system font — has Arabic glyphs
        } else {
            customTypeface ?: textView.typeface
        }
    }
}