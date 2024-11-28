package com.vs.schoolmessenger.Utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.Locale

object ChangeLanguage {

    // Method to set the language at runtime
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    // Save the selected language using your custom SharedPreference utility
    private fun persist(context: Context, language: String) {
        SharedPreference.putLanguage(context, language)
    }

    // Retrieve the saved language
    fun getPersistedLanguage(context: Context): String {
        return SharedPreference.getLanguage(context) ?: "en"
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    @Suppress("deprecation")
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}
