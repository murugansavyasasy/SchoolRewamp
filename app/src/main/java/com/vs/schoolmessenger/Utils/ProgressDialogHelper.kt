package com.vs.schoolmessenger.Utils


import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.vs.schoolmessenger.R

object ProgressDialogHelper {
    private var dialog: Dialog? = null
    private var progressView: CircularProgressView? = null

    fun show(context: Context) {
        if (dialog?.isShowing == true) return

        dialog = Dialog(context)
        dialog?.apply {
            setContentView(R.layout.progressbar_loading)
            setCancelable(false)

            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.attributes?.dimAmount = 0.5f

            progressView = findViewById(R.id.circularProgressView)
            progressView?.setProgress(0)

            show()
        }
    }

    fun updateProgress(percent: Int) {
        progressView?.setProgress(percent)
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
        progressView = null
    }
}
