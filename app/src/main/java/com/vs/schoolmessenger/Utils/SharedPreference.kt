package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SharedPreference {

    private const val SH_PREF = "SH_PREF"
    private const val SH_LANGUAGE = "isLanguage"
    private const val SH_AGREE = "isAgreeTerms"
    private const val SH_MOBILE_NUMBER = "isMobileNumber"
    private const val SH_PASSWORD = "isPassWord"
    private const val SH_COUNTRY_ID = "isCountryId"

    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun putMobileNumberPassWord(activity: Activity, isMobileNumber: String?, isPassWord: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putString(SH_MOBILE_NUMBER, isMobileNumber).apply()
        sharedPreferences.edit().putString(SH_PASSWORD, isPassWord).apply()
    }

    fun getMobileNumber(activity: Context): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(SH_MOBILE_NUMBER, "")
    }

    fun getPassWord(activity: Context): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(SH_PASSWORD, "")
    }

    fun putCountryId(activity: Activity, isCountryId: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putString(SH_COUNTRY_ID, isCountryId).apply()
    }

    fun getCountryId(activity: Context): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(SH_COUNTRY_ID, "")
    }




    fun putLanguage(activity: Context, isAppLanguage: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putString(SH_AGREE, isAppLanguage).apply()
    }

    fun getLanguage(activity: Context): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(SH_AGREE, "")
    }

}