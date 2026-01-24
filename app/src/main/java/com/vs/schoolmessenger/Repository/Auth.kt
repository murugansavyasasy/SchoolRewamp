package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.CountryResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordCreationResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordResetResponse
import com.vs.schoolmessenger.Auth.Logout.LogoutResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationResponse
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpSendResponse
import com.vs.schoolmessenger.Auth.OTP.OtpResponse
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse
import com.vs.schoolmessenger.CommonScreens.DeviceToken
import com.vs.schoolmessenger.Utils.SharedPreference

class Auth(application: Application) : AndroidViewModel(application) {

    private var apiRepositories: AuthServices? = null

    var isCountryList: LiveData<CountryResponse?>? = null
        private set

    var isVersionCheck: LiveData<VersionCheckResponse?>? = null
        private set

    var isUpdateNotificationCallLog: LiveData<StatusMessageModel?>? = null
        private set

    var isUserValidation: LiveData<UserValidationResponse?>? = null
        private set

    var isOtpResponse: LiveData<OtpResponse?>? = null
        private set

//    var isUserDetails: LiveData<UserDetailsResponse?>? = null
//        private set

    var isPasswordChange: LiveData<PasswordCreationResponse?>? = null
        private set

    var isForgetPassword: LiveData<ForgetOtpSendResponse?>? = null
        private set

    var isPasswordReset: LiveData<PasswordResetResponse?>? = null
        private set

    var isCreateNewPassword: LiveData<PasswordResetResponse?>? = null
        private set

    var isDeviceToken: LiveData<DeviceToken?>? = null
        private set

    var isLogout: LiveData<LogoutResponse?>? = null
        private set


    fun init() {
        apiRepositories = AuthServices()
        isCountryList = apiRepositories!!.isCountryListLiveData
        isVersionCheck = apiRepositories!!.isVersionCheckLiveData
        isUpdateNotificationCallLog = apiRepositories!!.isUpdateNotificationCallLogLiveData
        isUserValidation = apiRepositories!!.isUserValidationLiveData
        isOtpResponse = apiRepositories!!.isOtpResponseLiveData
//        isUserDetails = apiRepositories!!.isUserDetailsLiveData
        isPasswordChange = apiRepositories!!.isPasswordChangeLiveData
        isForgetPassword = apiRepositories!!.isForgetPasswordLiveData
        isPasswordReset = apiRepositories!!.isPasswordResetLiveData
        isCreateNewPassword = apiRepositories!!.isCreatePassWordLiveData
        isDeviceToken = apiRepositories!!.isUpdateDeviceTokenLiveData
        isLogout = apiRepositories!!.isLogoutLiveData
    }

    fun isCountryList() {
        apiRepositories!!.isCountryList()
    }

    fun isVersionCheck(jsonObject: JsonObject, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiRepositories!!.isVersionCheck(jsonObject, activity)
    }

    fun isUpdateNotificationCalllog(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isUpdateNotificationCallLog(jsonObject, activity)
    }


    fun isValidateUser(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isValidateUser(jsonObject, activity)
    }

    fun isOtpResponse(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isValidateOtp(jsonObject, activity)
    }


    fun isPasswordChange(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isPasswordChange(jsonObject, activity)
    }

    fun isForgetPassword(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isForgetPassword(jsonObject, activity)
    }

    fun isPasswordReset(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isResetPassword(jsonObject, activity)
    }

    fun isCreatePassword(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isCreateNewPassword(jsonObject, activity)
    }

    fun isDeviceToken(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isDeviceToken(jsonObject, activity)
    }

    fun isLogout(jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiRepositories!!.isLogout(jsonObject, activity)
    }
}