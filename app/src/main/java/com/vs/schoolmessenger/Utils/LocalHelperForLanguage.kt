package com.vs.schoolmessenger.Utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocalHelperForLanguage {

    fun wrapContext(context: Context, language: String): Context {
        var newContext = context
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
            // Works fine on Android 7+ except Android 10 (Q)
            newContext = context.createConfigurationContext(config)
        } else {
            // For Android 10 (Q) and below → update resources directly
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            newContext = context
        }
        return newContext
    }
}