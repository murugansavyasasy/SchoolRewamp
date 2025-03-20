package com.vs.schoolmessenger.Utils

import android.content.Context

object SharedPreference {

    private const val SH_PREF = "SH_PREF"
    private const val SH_LANGUAGE = "isLanguage"
    private const val SH_AGREE = "isAgreeTerms"
    private const val SH_MOBILE_NUMBER = "isMobileNumber"
    private const val SH_PASSWORD = "isPassWord"
    private const val SH_COUNTRY_ID = "isCountryId"

    fun putLanguage(activity: Context, isAppLanguage: String?) {
        val isSharePref = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        val ed = isSharePref.edit()
        ed.putString(SH_LANGUAGE, isAppLanguage)
        ed.apply()
        ed.commit()
        return
    }

    fun getLanguage(activity: Context): String? {
        return activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
            .getString(SH_LANGUAGE,"")
    }

    fun putAgreeTerms(activity: Context, isAgree: Boolean?) {
        val isSharePref = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        val ed = isSharePref.edit()
        ed.putBoolean(SH_AGREE, isAgree!!)
        ed.apply()
        ed.commit()
        return
    }

    fun getAgreeTerms(activity: Context): Boolean? {
        return activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
            .getBoolean(SH_AGREE,false)
    }


    fun putMobileNumber(activity: Context, isMobileNumber: String?) {
        val isSharePref = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        val ed = isSharePref.edit()
        ed.putString(SH_MOBILE_NUMBER, isMobileNumber)
        ed.apply()
        ed.commit()
        return
    }

    fun getMobileNumber(activity: Context): String? {
        return activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
            .getString(SH_MOBILE_NUMBER,"")
    }

    fun putPassWord(activity: Context, isPassWord: String?) {
        val isSharePref = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        val ed = isSharePref.edit()
        ed.putString(SH_PASSWORD, isPassWord)
        ed.apply()
        ed.commit()
        return
    }

    fun getPassWord(activity: Context): String? {
        return activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
            .getString(SH_PASSWORD,"")
    }

    fun putCountryId(activity: Context, isCountryId: String?) {
        val isSharePref = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        val ed = isSharePref.edit()
        ed.putString(SH_COUNTRY_ID, isCountryId)
        ed.apply()
        ed.commit()
        return
    }

    fun getCountryId(activity: Context): String? {
        return activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
            .getString(SH_COUNTRY_ID,"")
    }
}