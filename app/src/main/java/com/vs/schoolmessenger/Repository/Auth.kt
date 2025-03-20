package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.CountryResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordCreationResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordResetResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PasswordUpdateResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetailsResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationResponse
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpSendResponse
import com.vs.schoolmessenger.Auth.OTP.OtpResponse
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse

class Auth(application: Application) : AndroidViewModel(application) {

    private var apiRepositories: AuthServices? = null

    var isCountryList: LiveData<CountryResponse?>? = null
        private set

    var isVersionCheck: LiveData<VersionCheckResponse?>? = null
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


    fun init() {
        apiRepositories = AuthServices()
        isCountryList = apiRepositories!!.isCountryListLiveData
        isVersionCheck = apiRepositories!!.isVersionCheckLiveData
        isUserValidation = apiRepositories!!.isUserValidationLiveData
        isOtpResponse = apiRepositories!!.isOtpResponseLiveData
//        isUserDetails = apiRepositories!!.isUserDetailsLiveData
        isPasswordChange = apiRepositories!!.isPasswordChangeLiveData
        isForgetPassword = apiRepositories!!.isForgetPasswordLiveData
        isPasswordReset = apiRepositories!!.isPasswordResetLiveData
        isCreateNewPassword = apiRepositories!!.isCreatePassWordLiveData
    }

    fun isCountryList() {
        apiRepositories!!.isCountryList()
    }

    fun isVersionCheck(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isVersionCheck(jsonObject, activity)
    }

    fun isValidateUser(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isValidateUser(jsonObject, activity)
    }

    fun isOtpResponse(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isValidateOtp(jsonObject, activity)
    }

//    fun isUserDetails(
//        isMobileNumber: String,
//        isPassword: String,
//        isDeviceType: String,
//        isSecureId: String,
//        activity: Activity
//    ) {
//        apiRepositories!!.isUserDetails(
//            isMobileNumber,
//            isPassword,
//            isDeviceType,
//            isSecureId,
//            activity
//        )
//    }

    fun isPasswordChange(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isPasswordChange(jsonObject, activity)
    }

    fun isForgetPassword(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isForgetPassword(jsonObject, activity)
    }

    fun isPasswordReset(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isResetPassword(jsonObject, activity)
    }


    fun isCreatePassword(jsonObject: JsonObject, activity: Activity) {
        apiRepositories!!.isCreateNewPassword(jsonObject, activity)
    }

}