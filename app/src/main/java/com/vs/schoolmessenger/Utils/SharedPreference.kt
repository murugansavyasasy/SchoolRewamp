package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import androidx.core.content.edit
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails

object SharedPreference {

    private const val SH_PREF = "SH_PREF"
    private const val SH_LANGUAGE = "isLanguage"
    private const val SH_AGREE = "isAgreeTerms"
    private const val SH_MOBILE_NUMBER = "isMobileNumber"
    private const val SH_PASSWORD = "isPassWord"
    private const val SH_COUNTRY_ID = "isCountryId"
    private const val SH_USER_DETAILS = "UserDetails"
    private const val SH_CHILD_DETAILS = "ChildDetails"
    private const val SH_STAFF_DETAILS = "StaffDetails"
    private const val SH_LOGOUT = "isLogout"
    private const val SH_TOKEN = "isToken"

    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun putMobileNumberPassWord(activity: Activity, isMobileNumber: String?, isPassWord: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit() { putString(SH_MOBILE_NUMBER, isMobileNumber) }
        sharedPreferences.edit() { putString(SH_PASSWORD, isPassWord) }
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
        sharedPreferences.edit() { putString(SH_COUNTRY_ID, isCountryId) }
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

    fun putLogout(activity: Activity, isLogout: Boolean?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit() { putBoolean(SH_LOGOUT, isLogout!!) }
    }

    fun getLogout(activity: Context): Boolean? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(SH_LOGOUT, false)
    }


    fun putLanguage(activity: Context, isAppLanguage: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit() { putString(SH_AGREE, isAppLanguage) }
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

    fun putUserDetails(activity: Context, userDetails: UserDetails) {
        val gson = Gson()
        val userJson = gson.toJson(userDetails)
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit() { putString(SH_USER_DETAILS, userJson) }
    }


    fun getUserDetails(activity: Context): UserDetails? {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val userJson = sharedPreferences.getString(SH_USER_DETAILS, null)
        var userDetails: UserDetails? = null
        if (userJson != null) {
            val gson = Gson()
            userDetails = gson.fromJson(userJson, UserDetails::class.java)
        }
        return userDetails;
    }

    fun putChildDetails(activity: Context, isChildDetails: ChildDetails) {
        val gson = Gson()
        val userJson = gson.toJson(isChildDetails)
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit() { putString(SH_CHILD_DETAILS, userJson) }
    }

    fun getChildDetails(activity: Context): ChildDetails? {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val userJson = sharedPreferences.getString(SH_CHILD_DETAILS, null)
        var isChildDetails: ChildDetails? = null
        if (userJson != null) {
            val gson = Gson()
            isChildDetails = gson.fromJson(userJson, ChildDetails::class.java)
        }
        return isChildDetails;
    }

    fun putStaffDetails(activity: Context, isChildDetails: StaffDetails) {
        val gson = Gson()
        val userJson = gson.toJson(isChildDetails)
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit() { putString(SH_STAFF_DETAILS, userJson) }
    }

    fun getStaffDetails(activity: Context): StaffDetails? {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val userJson = sharedPreferences.getString(SH_STAFF_DETAILS, null)
        var isChildDetails: StaffDetails? = null
        if (userJson != null) {
            val gson = Gson()
            isChildDetails = gson.fromJson(userJson, StaffDetails::class.java)
        }
        return isChildDetails;
    }
}