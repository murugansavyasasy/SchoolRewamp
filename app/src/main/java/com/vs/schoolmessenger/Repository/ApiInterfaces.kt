package com.vs.schoolmessenger.Repository

import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.CountryResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordCreationResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordResetResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationResponse
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpSendResponse
import com.vs.schoolmessenger.Auth.OTP.OtpResponse
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.DeviceToken
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StaffListResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.StandardResponse
import com.vs.schoolmessenger.CommonScreens.GroupList.GroupListResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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

    @POST(APIMethods.isDeviceToken)
    fun isDeviceToken(
        @Body jsonObject: JsonObject
    ): Call<DeviceToken?>?


    @GET(APIMethods.isGetDashBoard)
    fun isDashBoard(
        @Header("Authorization") token: String,  // Pass token as a header
        @Query("member_type") isMemberType: String  // Pass isMemberType as a query parameter
    ): Call<DashboardResponse?>

    @GET(APIMethods.isGetAds)
    fun isGetAds(
        @Header("Authorization") token: String,  // Pass token as a header
        @Query("menu_id") isMenuId: String  // Pass isMemberType as a query parameter
    ): Call<AdsResponse?>

    @GET(APIMethods.getStaffList)
    fun getStaffList(
        @Header("Authorization") token: String,
    ): Call<StaffListResponse?>

    @GET(APIMethods.getSubjectList)
    fun getSubjectList(
        @Header("Authorization") token: String,
    ): Call<NameAndIdsResponse?>


    @GET(APIMethods.getStandard)
    fun getStandard(
        @Header("Authorization") token: String,
        @Query("section_id") isSectionId: String
    ): Call<StandardResponse?>

    @GET(APIMethods.getStudentList)
    fun getStudentList(
        @Header("Authorization") token: String,
        @Query("section_id") isSectionId: String
    ): Call<NameAndIdsResponse?>

    @GET(APIMethods.isGroupList)
    fun isGroupList(
        @Header("Authorization") token: String
    ): Call<GroupListResponse?>
}