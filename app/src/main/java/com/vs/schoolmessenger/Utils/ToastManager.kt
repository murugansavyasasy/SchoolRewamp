package com.vs.schoolmessenger.Utils

import android.content.Context
import android.widget.Toast

object ToastManager {
    private var currentToast: Toast? = null

    fun showToast(context: Context, resId: Int) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, context.getResources().getText(resId), Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    fun cancelToast() {
        currentToast?.cancel()
    }
}