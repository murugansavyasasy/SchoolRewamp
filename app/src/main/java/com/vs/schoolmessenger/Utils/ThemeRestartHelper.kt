package com.vs.schoolmessenger.Utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate

object ThemeRestartHelper {

    fun restartApp(context: Context, isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        intent?.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        )

        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
