package com.vs.schoolmessenger.Utils

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText

class UrlPasteOnlyEditText(context: Context, attrs: AttributeSet?) :
    AppCompatEditText(context, attrs) {

    init {
        isFocusable = true
        isCursorVisible = true
        isFocusableInTouchMode = true
        showSoftInputOnFocus = false // prevent soft keyboard
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        // Disable soft keyboard
        return null
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = clipboard.primaryClip

            if (clipData != null && clipData.itemCount > 0) {
                val pastedText = clipData.getItemAt(0).coerceToText(context).toString().trim()

                // ✅ Allow any text to be pasted
                if (pastedText.isNotEmpty()) {
                    setText(pastedText)
                    setSelection(text?.length ?: 0)
                } else {
                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Nothing to paste", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return super.onTextContextMenuItem(id)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Block typing from hardware keyboard
        return false
    }
}
