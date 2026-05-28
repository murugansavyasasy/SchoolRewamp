package com.vs.schoolmessenger.FCM

import android.content.Context

object CallStateManager {

    private const val PREF_NAME = "call_state"
    private const val KEY_PREFIX = "handled_"

    fun markHandled(context: Context, id: Int) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putBoolean(KEY_PREFIX + id, true).apply()
    }

    fun isHandled(context: Context, id: Int): Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getBoolean(KEY_PREFIX + id, false)
    }

    fun clear(context: Context, id: Int) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().remove(KEY_PREFIX + id).apply()
    }
}