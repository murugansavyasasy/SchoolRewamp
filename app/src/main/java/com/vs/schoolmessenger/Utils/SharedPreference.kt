package com.vs.schoolmessenger.Utils

import android.content.Context

object SharedPreference {
    const val SH_PREF = "SH_PREF"
    const val isLanguage = "isLanguage"

    fun putLanguage(activity: Context, isAppLanguage: String?) {
        val sharepref = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        val ed = sharepref.edit()
        ed.putString(isLanguage, isAppLanguage)
        ed.apply()
        ed.commit()
        return
    }

    fun getLanguage(activity: Context): String? {
        return activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
            .getString(isLanguage,"")
    }
}