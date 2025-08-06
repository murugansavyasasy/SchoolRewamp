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
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentResponse
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport.ChildAttendanceResponse
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesListResponse
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesTypesResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.CouponMenuResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCoupon.ActivateCouponResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCouponSummary.ActivateCouponSummaryResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.MyCouponSummaryRequest
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummaryResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponRequestModel.ActivateCouponRequest
import com.vs.schoolmessenger.Parent.Coupon.CouponRequestModel.CategorySummaryRequest
import com.vs.schoolmessenger.Parent.Coupon.CouponRequestModel.CouponDetailsRequest
import com.vs.schoolmessenger.Parent.Coupon.CouponRequestModel.CouponSummaryRequest
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarksResponse
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamTimeTableResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ProgressCardResponse
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.InteractionWithStaffResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.QuestionModelResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.QuestionModelRequest
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestApplyResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDeleteResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveUpdateResponse
import com.vs.schoolmessenger.Parent.Timetable.TimeTableResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.SendAbsenteeSMSResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportDataResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudentsResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesResponse
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentResponse
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionResponse
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceDetails
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionReportResponse
import com.vs.schoolmessenger.School.Event.Model.EventDeleteResponse
import com.vs.schoolmessenger.School.Event.Model.SchoolEventResponse
import com.vs.schoolmessenger.School.Event.Response.EventSendResponse
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeePendingReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerModelRequest
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.AnswerModelResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.QuestionResponse
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import com.vs.schoolmessenger.School.LeaveRequests.Response.LeaveActionResponse
import com.vs.schoolmessenger.School.LeaveRequests.Response.LeaveRequestResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanDeleteModel.LPDeleteResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.LessonPlanEditResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanUpdateModel.LessonPlanUpdateResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffLocationResponse
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeBoardStaffResponse
import com.vs.schoolmessenger.School.NoticeBoard.Response.NoticeBoardDeleteResponse
import com.vs.schoolmessenger.School.NoticeBoard.Response.NoticeBoardSendResponse
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolStrengthResponse
import com.vs.schoolmessenger.School.StudentReport.GetStudentReportData
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
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


    @GET(APIMethods.isGetAssignmentReport)
    fun isGetAssignmentReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.academic_year_id) isAcademicYearId: Int,
    ): Call<AssignmentResponse?>

    @PUT(APIMethods.isAssignmentDelete)
    fun isAssignmentDelete(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<LPDeleteResponse?>

    @PUT(APIMethods.isHomeWorkUpdate)
    fun isHomeWorkUpdate(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<StatusMessageModel?>

    @PUT(APIMethods.isAttachmentUpdate)
    fun isAttachmentUpdate(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<StatusMessageModel?>


    @PUT(APIMethods.isNoticeBoardUpdate)
    fun isNoticeBoardUpdate(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<StatusMessageModel?>


    @PUT(APIMethods.isHomeWorkDelete)
    fun isHomeWorkDelete(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<StatusMessageModel?>

    @PUT(APIMethods.isAttachmentDelete)
    fun isAttachmentDelete(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<StatusMessageModel?>



    @POST(APIMethods.isSendText)
    fun isSendText(
        @Header(APIKeyNames.Authorization) token: String, @Body jsonObject: JsonObject
    ): Call<TextSendResponse>?

    @POST(APIMethods.isSendHomeWork)
    fun isSendHomeWork(
        @Header(APIKeyNames.Authorization) token: String, @Body jsonObject: JsonObject
    ): Call<HomeWorkSendResponse>?

    @POST(APIMethods.isAssignmentSend)
    fun isAssignmentSend(
        @Header(APIKeyNames.Authorization) token: String, @Body jsonObject: JsonObject
    ): Call<HomeWorkSendResponse>?

    @POST(APIMethods.isSendVoice)
    fun isSendVoice(
        @Header(APIKeyNames.Authorization) token: String, @Body jsonObject: JsonObject
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
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<StatusArchiveResponse>?

    @POST(APIMethods.isUpdateStatusArchive)
    fun isUpdateStatusArchive(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
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


    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isHomeWorkComplete)
    fun isHomeWorkComplete(
        @Header(APIKeyNames.Authorization) token: String,
        @Body jsonObject: JsonObject
    ): Call<StatusMessageModel?>

    @GET(APIMethods.homework_list_archive)
    fun homework_list_archive(
        @Header(APIKeyNames.Authorization) token: String,
    ): Call<GetHomeworkData?>

    @POST(APIMethods.punch_giometric_attendance)
    fun punchGiometricAttendance(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @POST(APIMethods.add_giometric_location)
    fun addGiometricLocation(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @POST(APIMethods.remove_location)
    fun removeLocation(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<StatusMessageModel>?

    @POST(APIMethods.update_location)
    fun updateLocation(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
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
        @Query(APIKeyNames.academic_year_id) academic_year_id: Int,
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

    @GET(APIMethods.isNoticeBoardStaffReport)
    fun isNoticeBoardStaffReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<NoticeBoardStaffResponse?>?

    @GET(APIMethods.IsGetEventReport)
    fun IsGetEventReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<EventResponse?>?

    @GET(APIMethods.IsGetEventSchoolReport)
    fun IsGetEventSchoolReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<SchoolEventResponse?>?

    @GET(APIMethods.IsGetHolidayReport)
    fun IsGetHolidayReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<HolidayResponse?>?

    @POST(APIMethods.send_absentee_sms)
    fun UpdateSendAbsenteeSMS(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<SendAbsenteeSMSResponse>?

    @GET(APIMethods.get_student_attendance_report_for_scchool)
    fun isGetStudentAttendanceReportForSchool(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.section_id) section_id: String,
        @Query(APIKeyNames.from_date) from_date: String,
        @Query(APIKeyNames.to_date) to_date: String,
        @Query(APIKeyNames.standard_id) standard_id: String
    ): Call<StudentAttendanceReportDataResponse?>

    @GET(APIMethods.get_child_attendance_report)
    fun isGetChildAttendanceReport(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<ChildAttendanceResponse?>

    @GET(APIMethods.get_certificate_types)
    fun isGetCertificatesTypes(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<CertificatesTypesResponse?>

    @POST(APIMethods.send_certificate_request)
    fun sendCertificateRequest(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<StatusMessageModel?>

    @GET(APIMethods.get_certificates_list)
    fun isGetCertificateRequests(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<CertificatesListResponse?>


    @GET(APIMethods.get_time_table)
    fun isGetTimeTable(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.day_id) day_id: Int?,
    ): Call<TimeTableResponse?>

    @GET(APIMethods.getabsenteescountbydate)
    fun getabsenteescountbydate(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<AbsenteesResponse?>

    @GET(APIMethods.getabsenteesstudentbydate)
    fun getabsenteesstudentbydate(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.absent_on) absent_on: String?,
        @Query(APIKeyNames.section_id) section_id: String?
    ): Call<AbsenteeStudentsResponse?>

    @POST(APIMethods.sendnotice)
    fun sendnotice(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<NoticeBoardSendResponse>?


    @POST(APIMethods.sendevent)
    fun sendevent(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<EventSendResponse>?

    @POST(APIMethods.sendAttachment)
    fun sendAttachment(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<NoticeBoardSendResponse>?

    @GET(APIMethods.attachmentList)
    fun attachmentList(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<AttachmentReportResponse?>

    @GET(APIMethods.attachmentReportList)
    fun attachmentReportList(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<AttachmentReportResponse?>




    @GET(APIMethods.attachmentListArchive)
    fun attachmentListArchive(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<AttachmentResponse?>

    @POST(APIMethods.leave_request_apply)
    fun LeaveRequestApply(
        @Header(APIKeyNames.Authorization) token: String, @Body request: JsonObject
    ): Call<LeaveRequestApplyResponse>?

    @GET(APIMethods.getleaverequest)
    fun getleaverequest(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.member_type) member_type: String?
    ): Call<LeaveRequestResponse?>

    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isleaverequestapprove)
    fun isleaverequestapprove(
        @Header(APIKeyNames.Authorization) token: String, @Body request: LeaveApproveRequest
    ): Call<LeaveActionResponse?>


    @GET(APIMethods.getlpStaffReport)
    fun getlpStaffReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.request_type) request_type: String?
    ): Call<AllClassResponse?>


    @GET(APIMethods.getlpViewReport)
    fun getlpViewReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.section_subject_id) section_subject_id: String?,
        @Query(APIKeyNames.lesson_plan_status) lesson_plan_status: Int?
    ): Call<LessonPlanViewSummaryResponse?>


    @GET(APIMethods.getlpeditReport)
    fun getlpeditReport(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.particular_id) particular_id: String?,
        @Query(APIKeyNames.request_type) request_type: String?
    ): Call<LessonPlanEditResponse?>


    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isupdatelessonplan)
    fun isupdatelessonplan(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: RequestBody
    ): Call<LessonPlanUpdateResponse?>

    @Headers("Content-Type: application/json")
    @PUT(APIMethods.islessonplandelete)
    fun islessonplandelete(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: RequestBody
    ): Call<LPDeleteResponse?>


    @GET(APIMethods.get_category_list)
    fun getcouponmenu(
        @Header("Partner-Name") partnerName: String, @Header("api-key") apiKey: String
    ): Call<CouponMenuResponse?>


    @POST(APIMethods.get_campaigns)
    fun getCouponsSummary(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @Body request: CouponSummaryRequest,
    ): Call<CampaignResponse?>?


    @POST(APIMethods.get_campaigns)
    fun getCouponsCategorySummary(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @Body request: CategorySummaryRequest,
    ): Call<CampaignResponse?>?


    @POST(APIMethods.my_coupons)
    fun getmycoupons(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @Body request: MyCouponSummaryRequest,
    ): Call<TicketSummaryResponse?>?

    @POST(APIMethods.get_campaign_details)
    fun getCouponDetails(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @Body request: CouponDetailsRequest,
    ): Call<ActivateCouponSummaryResponse?>?


    @POST(APIMethods.activate_coupon)
    fun sendactivatecoupon(
        @Header("Partner-Name") parentName: String?,
        @Header("api-key") apiKey: String?,
        @Body request: ActivateCouponRequest,
    ): Call<ActivateCouponResponse?>?


    @GET(APIMethods.staff_details_for_chat)
    fun getdetailsforchat(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<InteractionWithStaffResponse?>?

    @GET(APIMethods.student_details_for_chat)
    fun getstudentdetailsforchat(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<InteractionWithStudentResponse?>?

    @GET(APIMethods.get_staff_answers)
    fun getstaffanswers(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.staff_id) request_type: String?,
        @Query(APIKeyNames.subject_id) subject_id: String?,
        @Query(APIKeyNames.offset) offset: Int?,
        @Query(APIKeyNames.is_class_teacher) is_class_teacher: Boolean?
    ): Call<AnswerResponse?>?


    @GET(APIMethods.staff_get_questions)
    fun getstaffquestions(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.is_class_teacher) is_class_teacher: Boolean?,
        @Query(APIKeyNames.section_id) section_id: String?,
        @Query(APIKeyNames.subject_id) subject_id: String?,
        @Query(APIKeyNames.offset) offset: Int?
    ): Call<QuestionResponse?>?


    @POST(APIMethods.student_ask_question)
    fun sendquestion(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: QuestionModelRequest,
    ): Call<QuestionModelResponse?>?


    @POST(APIMethods.staff_ans_question)
    fun sendanswer(
        @Header(APIKeyNames.Authorization) token: String,
        @Body request: AnswerModelRequest,
    ): Call<AnswerModelResponse?>?


    @GET(APIMethods.get_exams)
    fun getexams(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<ExamTimeTableResponse?>?


    @GET(APIMethods.exam_list)
    fun getexamslist(
        @Header(APIKeyNames.Authorization) token: String
    ): Call<ExamResponse?>?


    @GET(APIMethods.view_marks)
    fun getviewmarks(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.exam_id) exam_id: String
    ): Call<ExamMarksResponse?>?

    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isleaverequestupdate)
    fun isleaverequestupdate(
        @Header(APIKeyNames.Authorization) token: String, @Body request: LeaveRequestUpdate
    ): Call<LeaveUpdateResponse?>

    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isleaverequestdelete)
    fun isleaverequestdelete(
        @Header(APIKeyNames.Authorization) token: String, @Body request: LeaveRequestDelete
    ): Call<LeaveRequestDeleteResponse?>


    @GET(APIMethods.progress_card)
    fun getProgressMarks(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.exam_id) exam_id: String
    ): Call<ProgressCardResponse?>?


    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isnoticeboarddelete)
    fun isnoticeboarddelete(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: JsonObject
    ): Call<NoticeBoardDeleteResponse?>


    @Headers("Content-Type: application/json")
    @PUT(APIMethods.isEventDelete)
    fun isEventDelete(
        @Header(APIKeyNames.Authorization) token: String, @Body requestBody: RequestBody
    ): Call<EventDeleteResponse?>



    @GET(APIMethods.isAssignmentSubmittedList)
    fun getassignmentlist(
        @Header(APIKeyNames.Authorization) token: String,
        @Query(APIKeyNames.id) id: String,
        @Query(APIKeyNames.type) type: String
    ): Call<SubmissionResponse?>?


}