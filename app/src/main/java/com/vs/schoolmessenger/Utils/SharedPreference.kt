package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.GlobalVariableData

object SharedPreference {

    private const val PREF_NAME = "communication_prefs"
    private const val SH_PREF = "SH_PREF"
    private const val SH_LANGUAGE = "isLanguage"
    private const val SH_AGREE = "isAgreeTerms"
    private const val SH_MOBILE_NUMBER = "isMobileNumber"
    private const val SH_PASSWORD = "isPassWord"
    private const val SH_COUNTRY_ID = "isCountryId"
    private const val SH_INTRODUCTION_SKIP = "isIntroductionSkip"
    private const val SH_USER_DETAILS = "UserDetails"
    private const val SH_GLOBAL_VARIABLES = "GlobalVariables"
    private const val SH_CHILD_DETAILS = "ChildDetails"
    private const val SH_STAFF_DETAILS = "StaffDetails"
    private const val SH_LOGOUT = "isLogout"
    private const val SH_TOKEN = "isToken"
    private const val SH_BASEURL = "isBaseUrl"
    private const val SH_REPORTING_URL = "isReportingUrl"
    private const val SH_BIOMETRIC_ENABLED = "isBiometricEnabled"
    private const val SH_BIOMETRIC_SKIP = "isBiometricSkip"
    private const val KEY_FINGERPRINT_ENABLED = "fingerprint_enabled"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_FINGER_PRINT_SKIPPED = "finger_print_skipped"
    private const val KEY_FINGER_PRINT_SETUP_SKIP = "finger_print_setup_skip"
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    const val KEY_SCHOOL_DASHBOARD_TOUR = "school_dashboard_tour"
    const val KEY_PARENT_DASHBOARD_TOUR = "parent_dashboard_tour"
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setFingerPrintSetupSkip(activity: Activity, enabled: Boolean) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(KEY_FINGER_PRINT_SETUP_SKIP, enabled) }
    }

    fun isFingerprintSetupSkip(activity: Activity): Boolean {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(KEY_FINGER_PRINT_SETUP_SKIP, false)
    }

    fun setFingerprintEnabled(activity: Activity, enabled: Boolean) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(KEY_FINGERPRINT_ENABLED, enabled) }
    }


    fun isFingerprintEnabled(activity: Activity): Boolean {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(KEY_FINGERPRINT_ENABLED, false)
    }

    fun setLoggedIn(activity: Activity, loggedIn: Boolean) {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(KEY_LOGGED_IN, loggedIn) }
    }

    fun isLoggedIn(activity: Activity): Boolean {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false)
    }


    fun setFingerPrintSkipped(activity: Activity, loggedIn: Boolean) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(KEY_FINGER_PRINT_SKIPPED, loggedIn) }
    }

    fun isFingerPrintSkipped(activity: Activity): Boolean {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(KEY_FINGER_PRINT_SKIPPED, false)
    }

    fun putMobileNumberPassWord(activity: Activity, isMobileNumber: String?, isPassWord: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putString(SH_MOBILE_NUMBER, isMobileNumber) }
        sharedPreferences.edit { putString(SH_PASSWORD, isPassWord) }
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

    fun putCountryId(activity: Activity, isCountryId: Int?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putInt(SH_COUNTRY_ID, isCountryId!!) }
    }

    fun getCountryId(activity: Context): Int {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt(SH_COUNTRY_ID, 0)

    }

    fun putIntroductionSkip(activity: Activity, isCountryId: Boolean?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(SH_INTRODUCTION_SKIP, isCountryId!!) }
    }

    fun getIntroductionSkip(activity: Context): Boolean {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(SH_INTRODUCTION_SKIP, false)

    }

    fun putLogout(activity: Activity, isLogout: Boolean?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(SH_LOGOUT, isLogout!!) }
    }

    fun getLogout(activity: Context): Boolean {

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
        val prefs = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        prefs.edit().putString(SH_LANGUAGE, isAppLanguage).apply()
    }

    fun getLanguage(activity: Context): String? {
        val prefs = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        return prefs.getString(SH_LANGUAGE, "en") // default is English
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
        sharedPreferences.edit { putString(SH_USER_DETAILS, userJson) }
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
        return userDetails
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
        sharedPreferences.edit { putString(SH_CHILD_DETAILS, userJson) }
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
        return isChildDetails
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
        sharedPreferences.edit { putString(SH_STAFF_DETAILS, userJson) }
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
        return isChildDetails
    }


    fun putBaseUrl(activity: Context, isBaseUrl: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putString(SH_BASEURL, isBaseUrl) }
    }

    fun putReportingUrl(activity: Context, isReportUrl: String?) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putString(SH_REPORTING_URL, isReportUrl) }
    }

    fun getBaseUrl(activity: Context): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(SH_BASEURL, "")
    }

    fun getReportingUrl(activity: Context): String? {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString(SH_REPORTING_URL, "")
    }

    fun putBiometricEnabled(activity: Activity, isEnable: Boolean) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(SH_BIOMETRIC_ENABLED, isEnable) }
    }

    fun getBiometricEnabled(activity: Context): Boolean {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(SH_BIOMETRIC_ENABLED, false)
    }

    fun putBiometricSkip(activity: Activity, isEnable: Boolean) {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putBoolean(SH_BIOMETRIC_SKIP, isEnable) }
    }

    fun getBiometricSkip(activity: Context): Boolean {

        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getBoolean(SH_BIOMETRIC_SKIP, false)
    }

    fun putGlobalvariables(activity: Context, globalVariables: GlobalVariableData) {
        val gson = Gson()
        val userJson = gson.toJson(globalVariables)
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit { putString(SH_GLOBAL_VARIABLES, userJson) }
    }


    fun getGlobalVariables(activity: Context): GlobalVariableData? {
        val sharedPreferences = EncryptedSharedPreferences.create(
            SH_PREF,
            masterKeyAlias,
            activity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val userJson = sharedPreferences.getString(SH_GLOBAL_VARIABLES, null)
        var globalVariables: GlobalVariableData? = null
        if (userJson != null) {
            val gson = Gson()
            globalVariables = gson.fromJson(userJson, GlobalVariableData::class.java)
        }
        return globalVariables
    }


    fun isTourShown(context: Context, key: String): Boolean {
        return prefs(context).getBoolean(key, false)
    }

    fun setTourShown(context: Context, key: String) {
        prefs(context)
            .edit()
            .putBoolean(key, true)
            .apply()
    }
}