package com.vs.schoolmessenger.Repository

import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.PreSignedUrl
import com.vs.schoolmessenger.Auth.Country.CountryResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordCreationResponse
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordResetResponse
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationResponse
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpSendResponse
import com.vs.schoolmessenger.Auth.OTP.OtpResponse
import com.vs.schoolmessenger.Auth.Splash.VersionCheckResponse
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.DeviceToken
import com.vs.schoolmessenger.CommonScreens.GlobalVariableResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYearResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardResponse
import com.vs.schoolmessenger.Parent.Attendance.ChildAttendanceResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayResponse
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceDetails
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.SendAbsenteeSMSResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.StudentAttendanceReportDataResponse

import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionReportResponse
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffLocationResponse
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrengthResponse
import com.vs.schoolmessenger.School.StudentReport.GetStudentReportData
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
//        @Query(APIKeyNames.Req_mobile_number) mobile_number: String?,
//        @Query(APIKeyNames.Req_password) password: String?,
//        @Query(APIKeyNames.Req_device_type) device_type: String?,
//        @Query(APIKeyNames.Req_secure_id) secure_id: String?
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

    @GET(APIMethods.isGlobalVariables)
    fun isGetGlobalVariable(
        @Header(APIKeyNames.Authorization) token: String  // Pass token as a header
    ): Call<GlobalVariableResponse?>

    @GET(APIMethods.isGetDashBoard)
    fun isDashBoard(
        @Header(APIKeyNames.Authorization) token: String,  // Pass token as a header
        @Query(APIKeyNames.member_type) isMemberType: String  // Pass isMemberType as a query parameter
    ): Call<DashboardResponse?>

    @GET(APIMethods.isGetAds)
    fun isGetAds(
        @Header(APIKeyNames.Authorization) token: String,  // Pass token as a header
        @Query(APIKeyNames.menu_id) isMenuId: String  // Pass isMemberType as a query parameter
    ): Call<AdsResponse?>

    @GET(APIMethods.getStaffList)
    fun getStaffList(
        @Header(APIKeyNames.Authorization) token: String,
    ): Call<NameAndIdsResponse?>

    @GET(APIMethods.getSubjectList)
    fun getSubjectList(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int,
        @Query(APIKeyNames.section_ids) isSectionId: String
    ): Call<NameAndIdsResponse?>


    @GET(APIMethods.getStandard)
    fun getStandard(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int,
    ): Call<StandardResponse?>

    @GET(APIMethods.getStudentList)
    fun getStudentList(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.section_id) isSectionId: String,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int
    ): Call<NameAndIdsResponse?>

    @GET(APIMethods.isGroupList)
    fun isGroupList(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int,
    ): Call<NameAndIdsResponse?>


    @GET(APIMethods.isGetCommmunicationlist)
    fun isGetCommmunicationlist(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<VoiceDataResponse?>


    @GET(APIMethods.isGetCommmunicationlistload)
    fun isGetCommmunicationlistload(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<VoiceDataResponse?>


    @GET(APIMethods.isGetVoiceHistory)
    fun isGetVoiceHistory(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.is_emergency) is_emergency: String
    ): Call<VoiceDetails?>

    @GET(APIMethods.isGetTextHistory)
    fun isGetTextHistory(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<TextDetailsResponse?>

    @GET(APIMethods.isGetHomeWorkReport)
    fun isGetHomeWorkReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.section_id) isSectionId: Int,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int,
        @Query(APIKeyNames.date) isdate: String
    ): Call<HomeWorkReportApiResponse?>


    @POST(APIMethods.isSendText)
    fun isSendText(
        @Header(APIKeyNames.Authorization) token: String,
        @Body jsonObject: JsonObject
    ): Call<TextSendResponse>?

    @POST(APIMethods.isSendHomeWork)
    fun isSendHomeWork(
        @Header(APIKeyNames.Authorization) token: String,
        @Body jsonObject: JsonObject
    ): Call<HomeWorkSendResponse>?

    @POST(APIMethods.isSendVoice)
    fun isSendVoice(
        @Header(APIKeyNames.Authorization) token: String,
        @Body jsonObject: JsonObject
    ): Call<TextSendResponse>?

    @GET("get-s3-presigned-url")
    fun getPreSignedUrl(
        @Query(APIKeyNames.bucket) bucket: String?,
        @Query(APIKeyNames.fileName) fileName: String?,
        @Query(APIKeyNames.bucketPath) bucketPath: String?,
        @Query(APIKeyNames.fileType) fileType: String?
    ): Call<PreSignedUrl?>?


    @POST(APIMethods.isUpdateStatusCommunication)
    fun isUpdateStatusCommunication(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<StatusArchiveResponse>?


    @POST(APIMethods.isUpdateStatusArchive)
    fun isUpdateStatusArchive(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<StatusArchiveResponse>?

    @GET(APIMethods.isGetAcademicYear)
    fun isGetAcademicYear(
        @Header(APIKeyNames.Authorization) token: String,
    ): Call<AcademicYearResponse?>


    //get Homeworkdetails
    @GET(APIMethods.isHomeWorkDetails)
    fun isHomeWorkDetails(
        @Header(APIKeyNames.Authorization) token: String,
    ): Call<GetHomeworkData?>

    @GET(APIMethods.homework_list_archive)
    fun homework_list_archive(
        @Header(APIKeyNames.Authorization) token: String,
    ): Call<GetHomeworkData?>

    @POST(APIMethods.punch_giometric_attendance)
    fun punchGiometricAttendance(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @POST(APIMethods.add_giometric_location)
    fun addGiometricLocation(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @POST(APIMethods.remove_location)
    fun removeLocation(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @POST(APIMethods.update_location)
    fun updateLocation(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @GET(APIMethods.giometric_location_history)
    fun getLocationHistory(@Header(APIKeyNames.Authorization) token: String): Call<LocationHistoryResponse?>?

    @GET(APIMethods.staff_locations)
    fun getStaffLocations(@Header(APIKeyNames.Authorization) token: String): Call<StaffLocationResponse?>?

    @GET(APIMethods.giometric_staff_attendance_report)
    fun getStaffAttendanceReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.attendance_dt) isAttendanceDt: String?,
    ): Call<StaffAttendanceReportResponse?>?

    @GET(APIMethods.giometric_principal_attendance_report)
    fun getStaffWiseAttendanceReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(
            APIKeyNames.attendance_dt
        ) isAttendanceDt: String?,
    ): Call<StaffAttendanceReportResponse?>?

    @GET(APIMethods.giometric_principal_attendance_report)
    fun getStaffWiseAttendanceReportStaffList(
        @Header(APIKeyNames.Authorization) token: String, @Query(
            APIKeyNames.attendance_month
        ) attendance_month: String?, @Query(APIKeyNames.staff_id) isStaffId: Int?
    ): Call<StaffAttendanceReportResponse?>?

    @GET(APIMethods.punch_history)
    fun getPunchHistory(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.from_date) bucket: String?,
        @Query(APIKeyNames.to_date) fileName: String?,
        @Query(APIKeyNames.staff_id) staff_id: String?
    ): Call<PunchHistoryResponse?>?

    @GET(APIMethods.student_report)
    fun getStudentReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.class_id) class_id: Int?,
        @Query(APIKeyNames.section_id) section_id: Int?
    ): Call<GetStudentReportData?>?

    @GET(APIMethods.isGetDailyCollectionReport)
    fun isGetDailyCollectionReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.type) istype: String,
        @Query(APIKeyNames.from_date) isfromdate: String,
        @Query(APIKeyNames.to_date) istodate: String
    ): Call<DailyCollectionReportResponse?>


    @GET(APIMethods.isGetSchoolStrengthReport)
    fun isGetSchoolStrengthReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int
    ): Call<SchoolStrengthResponse?>


    @GET(APIMethods.isDetailedPendingReport)
    fun isDetailedPendingReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) academic_year_id: Int?
    ): Call<FeePendingReportResponse?>?


    @GET(APIMethods.isDetailedWisePendingReport)
    fun isDetailedWisePendingReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) academic_year_id: Int?
    ): Call<FeePendingReportResponse?>?


    @GET(APIMethods.isNoticeBoardReport)
    fun isNoticeBoardReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<NoticeBoardResponse?>?

    @GET(APIMethods.IsGetEventReport)
    fun IsGetEventReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<EventResponse?>?

    @GET(APIMethods.IsGetHolidayReport)
    fun IsGetHolidayReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<HolidayResponse?>?

    @POST(APIMethods.send_absentee_sms)
    fun UpdateSendAbsenteeSMS(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: JsonObject
    ): Call<SendAbsenteeSMSResponse>?

    @GET(APIMethods.get_student_attendance_report_for_scchool)
    fun isGetStudentAttendanceReportForSchool(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.standard_id) standard_id: String,
        @Query(APIKeyNames.section_id) section_id: String,
        @Query(APIKeyNames.from_date) from_date: String,
        @Query(APIKeyNames.to_date) to_date: String
    ): Call<StudentAttendanceReportDataResponse?>


    @GET(APIMethods.get_child_attendance_report)
    fun isGetChildAttendanceReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<ChildAttendanceResponse?>



}