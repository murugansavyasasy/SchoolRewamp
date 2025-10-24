package com.vs.schoolmessenger.Utils

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText

class UrlPasteOnlyEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    init {
        isFocusable = true
        isCursorVisible = true
        isFocusableInTouchMode = true
        showSoftInputOnFocus = false //  This prevents keyboard from opening
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        // No soft keyboard
        return null
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = clipboard.primaryClip

            if (clipData != null && clipData.itemCount > 0) {
                val pastedText = clipData.getItemAt(0).coerceToText(context).toString().trim()

                if (isValidUrl(pastedText)) {
                    setText(pastedText)
                    setSelection(text?.length ?: 0)
                } else {
                    Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                    setText("")
                }
            }
            return true
        }
        return super.onTextContextMenuItem(id)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Block typing from hardware keyboard
        return false
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            (uri.scheme == "http" || uri.scheme == "https") && uri.host != null
        } catch (e: Exception) {
            false
        }
    }
}
