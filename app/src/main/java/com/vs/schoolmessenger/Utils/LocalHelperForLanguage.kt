package com.vs.schoolmessenger.Utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocalHelperForLanguage {

    fun wrapContext(context: Context, language: String): Context {
        var newContext = context
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        newContext = context.createConfigurationContext(config)
        return newContext
    }
}