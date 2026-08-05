package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.GlobalVariableData

object SharedPreference {

    private const val TAG = "SharedPreference"
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
    const val KEY_SCHOOL_DASHBOARD_TOUR = "school_dashboard_tour"
    const val KEY_PARENT_DASHBOARD_TOUR = "parent_dashboard_tour"

    // Lazy master key — only created when first accessed, with try-catch
    private val masterKeyAlias: String? by lazy {
        try {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        } catch (e: Exception) {
            Log.e(TAG, "Keystore operation failed, encryption unavailable", e)
            null
        }
    }

    // Singleton EncryptedSharedPreferences instance
    private var encryptedPrefsInstance: SharedPreferences? = null

    // Thread-safe getter for encrypted prefs with fallback to plain prefs
    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        // Return cached instance if available
        encryptedPrefsInstance?.let { return it }

        val masterKey = masterKeyAlias
        return if (masterKey != null) {
            try {
                val prefs = EncryptedSharedPreferences.create(
                    SH_PREF,
                    masterKey,
                    context.applicationContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                encryptedPrefsInstance = prefs
                prefs
            } catch (e: Exception) {
                Log.e(TAG, "EncryptedSharedPreferences creation failed, using plain prefs", e)
                getPlainPrefs(context)
            }
        } else {
            getPlainPrefs(context)
        }
    }

    // Plain fallback prefs (used when keystore/encryption fails)
    private fun getPlainPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
    }

    // For tour prefs (non-encrypted, kept as-is since it was already plain)
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setFingerPrintSetupSkip(activity: Activity, enabled: Boolean) {
        getEncryptedPrefs(activity).edit { putBoolean(KEY_FINGER_PRINT_SETUP_SKIP, enabled) }
    }

    fun isFingerprintSetupSkip(activity: Activity): Boolean {
        return getEncryptedPrefs(activity).getBoolean(KEY_FINGER_PRINT_SETUP_SKIP, false)
    }

    fun setFingerprintEnabled(activity: Activity, enabled: Boolean) {
        getEncryptedPrefs(activity).edit { putBoolean(KEY_FINGERPRINT_ENABLED, enabled) }
    }

    fun isFingerprintEnabled(activity: Activity): Boolean {
        return getEncryptedPrefs(activity).getBoolean(KEY_FINGERPRINT_ENABLED, false)
    }

    fun setLoggedIn(activity: Activity, loggedIn: Boolean) {
        getEncryptedPrefs(activity).edit { putBoolean(KEY_LOGGED_IN, loggedIn) }
    }

    fun isLoggedIn(activity: Activity): Boolean {
        return getEncryptedPrefs(activity).getBoolean(KEY_LOGGED_IN, false)
    }

    fun setFingerPrintSkipped(activity: Activity, loggedIn: Boolean) {
        getEncryptedPrefs(activity).edit { putBoolean(KEY_FINGER_PRINT_SKIPPED, loggedIn) }
    }

    fun isFingerPrintSkipped(activity: Activity): Boolean {
        return getEncryptedPrefs(activity).getBoolean(KEY_FINGER_PRINT_SKIPPED, false)
    }

    fun putMobileNumberPassWord(activity: Activity, isMobileNumber: String?, isPassWord: String?) {
        getEncryptedPrefs(activity).edit {
            putString(SH_MOBILE_NUMBER, isMobileNumber)
            putString(SH_PASSWORD, isPassWord)
        }
    }

    fun getMobileNumber(activity: Context): String? {
        return getEncryptedPrefs(activity).getString(SH_MOBILE_NUMBER, "")
    }

    fun getPassWord(activity: Context): String? {
        return getEncryptedPrefs(activity).getString(SH_PASSWORD, "")
    }

    fun putCountryId(activity: Activity, isCountryId: Int?) {
        getEncryptedPrefs(activity).edit { putInt(SH_COUNTRY_ID, isCountryId ?: 0) }
    }

    fun getCountryId(activity: Context): Int {
        return getEncryptedPrefs(activity).getInt(SH_COUNTRY_ID, 0)
    }

    fun putIntroductionSkip(activity: Activity, isCountryId: Boolean?) {
        getEncryptedPrefs(activity).edit { putBoolean(SH_INTRODUCTION_SKIP, isCountryId ?: false) }
    }

    fun getIntroductionSkip(activity: Context): Boolean {
        return getEncryptedPrefs(activity).getBoolean(SH_INTRODUCTION_SKIP, false)
    }

    fun putLogout(activity: Activity, isLogout: Boolean?) {
        getEncryptedPrefs(activity).edit { putBoolean(SH_LOGOUT, isLogout ?: false) }
    }

    fun getLogout(activity: Context): Boolean {
        return getEncryptedPrefs(activity).getBoolean(SH_LOGOUT, false)
    }

    fun putLanguage(activity: Context, isAppLanguage: String?) {
        val prefs = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        prefs.edit().putString(SH_LANGUAGE, isAppLanguage).apply()
    }

    fun getLanguage(activity: Context): String? {
        val prefs = activity.getSharedPreferences(SH_PREF, Context.MODE_PRIVATE)
        return prefs.getString(SH_LANGUAGE, "en")
    }

    fun putUserDetails(activity: Context, userDetails: UserDetails) {
        val gson = Gson()
        val userJson = gson.toJson(userDetails)
        getEncryptedPrefs(activity).edit { putString(SH_USER_DETAILS, userJson) }
    }

    fun getUserDetails(activity: Context): UserDetails? {
        val userJson = getEncryptedPrefs(activity).getString(SH_USER_DETAILS, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, UserDetails::class.java)
        } else null
    }

    fun putChildDetails(activity: Context, isChildDetails: ChildDetails) {
        val gson = Gson()
        val userJson = gson.toJson(isChildDetails)
        getEncryptedPrefs(activity).edit { putString(SH_CHILD_DETAILS, userJson) }
    }

    fun getChildDetails(activity: Context): ChildDetails? {
        val userJson = getEncryptedPrefs(activity).getString(SH_CHILD_DETAILS, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, ChildDetails::class.java)
        } else null
    }

    fun putStaffDetails(activity: Context, isChildDetails: StaffDetails) {
        val gson = Gson()
        val userJson = gson.toJson(isChildDetails)
        getEncryptedPrefs(activity).edit { putString(SH_STAFF_DETAILS, userJson) }
    }

    fun getStaffDetails(activity: Context): StaffDetails? {
        val userJson = getEncryptedPrefs(activity).getString(SH_STAFF_DETAILS, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, StaffDetails::class.java)
        } else null
    }

    fun putBaseUrl(activity: Context, isBaseUrl: String?) {
        getEncryptedPrefs(activity).edit { putString(SH_BASEURL, isBaseUrl) }
    }

    fun putReportingUrl(activity: Context, isReportUrl: String?) {
        getEncryptedPrefs(activity).edit { putString(SH_REPORTING_URL, isReportUrl) }
    }

    fun getBaseUrl(activity: Context): String? {
        return getEncryptedPrefs(activity).getString(SH_BASEURL, "")
    }

    fun getReportingUrl(activity: Context): String? {
        return getEncryptedPrefs(activity).getString(SH_REPORTING_URL, "")
    }

    fun putBiometricEnabled(activity: Activity, isEnable: Boolean) {
        getEncryptedPrefs(activity).edit { putBoolean(SH_BIOMETRIC_ENABLED, isEnable) }
    }

    fun getBiometricEnabled(activity: Context): Boolean {
        return getEncryptedPrefs(activity).getBoolean(SH_BIOMETRIC_ENABLED, false)
    }

    fun putBiometricSkip(activity: Activity, isEnable: Boolean) {
        getEncryptedPrefs(activity).edit { putBoolean(SH_BIOMETRIC_SKIP, isEnable) }
    }

    fun getBiometricSkip(activity: Context): Boolean {
        return getEncryptedPrefs(activity).getBoolean(SH_BIOMETRIC_SKIP, false)
    }

    fun putGlobalvariables(activity: Context, globalVariables: GlobalVariableData) {
        val gson = Gson()
        val userJson = gson.toJson(globalVariables)
        getEncryptedPrefs(activity).edit { putString(SH_GLOBAL_VARIABLES, userJson) }
    }

    fun getGlobalVariables(activity: Context): GlobalVariableData? {
        val userJson = getEncryptedPrefs(activity).getString(SH_GLOBAL_VARIABLES, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, GlobalVariableData::class.java)
        } else null
    }

    fun isTourShown(context: Context, key: String): Boolean {
        return prefs(context).getBoolean(key, false)
    }

    fun setTourShown(context: Context, key: String) {
        prefs(context).edit().putBoolean(key, true).apply()
    }
}