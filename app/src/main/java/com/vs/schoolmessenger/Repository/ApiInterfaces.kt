package com.vs.schoolmessenger.Repository

import android.app.Activity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.CountryResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordCreationResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordResetResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PasswordUpdateResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetailsResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationResponse
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpData
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpSendResponse
import com.vs.schoolmessenger.Auth.OTP.OtpResponse
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterfaces {

    @GET(APIMethods.isCountryList)
    fun isCountry(): Call<CountryResponse?>?

    @POST(APIMethods.isVersionCheck)
    fun isVersionCheck(
        @Body jsonObject: JsonObject
    ): Call<VersionCheckResponse?>?

    @POST(APIMethods.isValidateUser)
    fun isValidateUser(
        @Body jsonObject: JsonObject
    ): Call<UserValidationResponse?>?

    @POST(APIMethods.isValidateOtp)
    fun isValidateOtp(
        @Body jsonObject: JsonObject
    ): Call<OtpResponse?>?

//    @GET(APIMethods.isValidateUser)
//    fun isUserDetails(
//        @Query(RequestKeys.Req_mobile_number) mobile_number: String?,
//        @Query(RequestKeys.Req_password) password: String?,
//        @Query(RequestKeys.Req_device_type) device_type: String?,
//        @Query(RequestKeys.Req_secure_id) secure_id: String?
//    ): Call<UserDetailsResponse?>?

    @POST(APIMethods.isPasswordChange)
    fun isPasswordChange(
        @Body jsonObject: JsonObject
    ): Call<PasswordCreationResponse?>?

    @POST(APIMethods.isForgetPassword)
    fun isForgetPassword(
        @Body jsonObject: JsonObject
    ): Call<ForgetOtpSendResponse?>?

    @POST(APIMethods.isResetPassword)
    fun isResetPassword(
        @Body jsonObject: JsonObject
    ): Call<PasswordResetResponse?>?

    @POST(APIMethods.isCreateNewPassword)
    fun isCreateNewPassword(
        @Body jsonObject: JsonObject
    ): Call<PasswordResetResponse?>?



}