package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.GlobalVariableResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYearResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardResponse
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentResponse
import com.vs.schoolmessenger.Parent.Attendance.ChildAttendanceResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.CouponMenuResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummaryResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayResponse
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestApplyResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.SendAbsenteeSMSResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportDataResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudentsResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesResponse
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceDetails
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionReportResponse
import com.vs.schoolmessenger.School.Event.Response.EventSendResponse
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeePendingReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
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
import com.vs.schoolmessenger.School.NoticeBoard.Response.NoticeBoardSendResponse
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolStrengthResponse
import com.vs.schoolmessenger.School.StudentReport.GetStudentReportData
import okhttp3.RequestBody

class App(application: Application) : AndroidViewModel(application) {

    private var apiSchoolRepositories: SchoolServices = SchoolServices()
    private var apiParentRepositories: ParentServices = ParentServices()


    var isDashBoardData: LiveData<DashboardResponse?>? = null
        private set

    var isGetAds: LiveData<AdsResponse?>? = null
        private set

    var isGlobalVariables: LiveData<GlobalVariableResponse?>? = null
        private set

    var isGetStaffList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isGetSubjectList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isStandardSectionList: LiveData<StandardResponse?>? = null
        private set

    var isStudentList: LiveData<NameAndIdsResponse?>? = null

    var isGetGroupList: LiveData<NameAndIdsResponse?>? = null
        private set

    var isGetCommmunicationlist: LiveData<VoiceDataResponse?>? = null
        private set

    var isGetCommmunicationlistload: LiveData<VoiceDataResponse?>? = null
        private set


    var isGetVoiceHistory: LiveData<VoiceDetails?>? = null
        private set

    var isGetTextHistory: LiveData<TextDetailsResponse?>? = null
        private set

    var isGetHomeWorkReport: LiveData<HomeWorkReportApiResponse?>? = null
        private set

    var isGetAssignmentReport: LiveData<AssignmentResponse?>? = null
        private set

    var isAssignmentDelete: LiveData<LPDeleteResponse?>? = null
        private set



    var isGetDailyCollectionReport: LiveData<DailyCollectionReportResponse?>? = null
        private set

    var isGetSchoolStrengthReport: LiveData<SchoolStrengthResponse?>? = null
        private set


    var isNoticeBoardReport: LiveData<NoticeBoardResponse?>? = null
        private set

    var IsGetEventReport: LiveData<EventResponse?>? = null
        private set
    var IsGetHolidayReport: LiveData<HolidayResponse?>? = null
        private set

    var isDetailedPendingReport: LiveData<FeePendingReportResponse?>? = null
        private set

    var isDetailedWisePendingReport: LiveData<FeePendingReportResponse?>? = null
        private set

    var isSendText: LiveData<TextSendResponse?>? = null
        private set

    var isSendHomeWork: LiveData<HomeWorkSendResponse?>? = null
        private set

    var isAssignmentSend: LiveData<HomeWorkSendResponse?>? = null
        private set



    var isVoiceSend: LiveData<TextSendResponse?>? = null
        private set


    var isUpdateStatusArchive: LiveData<StatusArchiveResponse?>? = null
        private set

    var isGetAcademicList: LiveData<AcademicYearResponse?>? = null
    var isUpdateStatusCommunication: LiveData<StatusArchiveResponse?>? = null
        private set


    var isHomeWorkDetailsList: LiveData<GetHomeworkData?>? = null
        private set

    var isHomeWorkDetailsListArchive: LiveData<GetHomeworkData?>? = null
        private set

    var isPunchAttendance: LiveData<StatusMessageModel?>? = null
    var isAddLocation: LiveData<StatusMessageModel?>? = null
    var isRemoveLocation: LiveData<StatusMessageModel?>? = null
    var isUpdateLocation: LiveData<StatusMessageModel?>? = null
    var isLocationHistory: LiveData<LocationHistoryResponse?>? = null
    var isStaffLocations: LiveData<StaffLocationResponse?>? = null
    var isPunchHistory: LiveData<PunchHistoryResponse?>? = null
    var isStaffAttendanceReport: LiveData<StaffAttendanceReportResponse?>? = null
    var isStaffWiseAttendanceReport: LiveData<StaffAttendanceReportResponse?>? = null
    var isStaffWiseAttendanceReportList: LiveData<StaffAttendanceReportResponse?>? = null

    var isStudentReportList: LiveData<GetStudentReportData?>? = null
        private set
    var isSendAbsenteeSMS: LiveData<SendAbsenteeSMSResponse?>? = null
    var isChildAttendanceReport: LiveData<ChildAttendanceResponse?>? = null
    var isAttachmentResponse: LiveData<AttachmentResponse?>? = null
    var isAttachmentResponseArchive: LiveData<AttachmentResponse?>? = null

    var getabsenteescountbydate: LiveData<AbsenteesResponse?>? = null

    var isGetStudentAttendanceReportData: LiveData<StudentAttendanceReportDataResponse?>? = null

    var getabsenteesstudentbydate: LiveData<AbsenteeStudentsResponse?>? = null

    var sendnotice: LiveData<NoticeBoardSendResponse?>? = null
    var sendevent: LiveData<EventSendResponse?>? = null
    var isAttachmentSend: LiveData<NoticeBoardSendResponse?>? = null
    var isLeaveRequest: LiveData<LeaveRequestApplyResponse?>? = null

    var getleaverequest: LiveData<LeaveRequestResponse?>? = null

    var isleaverequestapprove: LiveData<LeaveActionResponse?>? = null

    var getlpStaffReport: LiveData<AllClassResponse?>? = null
    var getlpViewReport: LiveData<LessonPlanViewSummaryResponse?>? = null
    var getlpeditReport: LiveData<LessonPlanEditResponse?>? = null

    var isupdatelessonplan: LiveData<LessonPlanUpdateResponse?>? = null

    var islessonplandelete: LiveData<LPDeleteResponse?>? = null

    var getcouponmenu: LiveData<CouponMenuResponse?>? = null

    var getCouponsSummary: LiveData<CampaignResponse?>? = null

    var getCouponsCategorySummary: LiveData<CampaignResponse?>? = null
    var getmycouponsSummary: LiveData<TicketSummaryResponse?>? = null




    fun init() {
        isDashBoardData = apiSchoolRepositories.isDashBoardLiveData
        isGetAds = apiSchoolRepositories.isGetAdsLiveData
        isGlobalVariables = apiSchoolRepositories.isGetGlobalVariablesLiveData
        isGetStaffList = apiSchoolRepositories.isGetStaffListLiveData
        isGetSubjectList = apiSchoolRepositories.isSubjectListLiveData
        isStandardSectionList = apiSchoolRepositories.isGetStandardSectionLiveData
        isStudentList = apiSchoolRepositories.isStudentLiveData
        isGetGroupList = apiSchoolRepositories.isGetGroupLiveData
        isGetCommmunicationlist = apiSchoolRepositories.isGetCommunicationLiveData
        isGetCommmunicationlistload = apiSchoolRepositories.isGetCommunicationloadLiveData
        isGetVoiceHistory = apiSchoolRepositories.isGetVoiceHistoryLiveData
        isGetTextHistory = apiSchoolRepositories.isGetTextHistoryLiveData
        isGetHomeWorkReport = apiSchoolRepositories.isGetHomeWorkReportLiveData
        isGetAssignmentReport = apiSchoolRepositories.isGetAssignmentReportLiveData
        isAssignmentDelete = apiSchoolRepositories.isDeleteAssignmentLiveData
        isNoticeBoardReport = apiSchoolRepositories.isNoticeBoardReportLiveData
        isGetDailyCollectionReport = apiSchoolRepositories.isGetDailyCollectionReportLiveData
        isGetSchoolStrengthReport = apiSchoolRepositories.isGetSchoolStrengthReportLiveData
        isDetailedPendingReport = apiSchoolRepositories.isDetailedPendingReportLiveData
        isDetailedWisePendingReport = apiSchoolRepositories.isDetailedWisePendingReportLiveData
        isSendText = apiSchoolRepositories.isSendTextLiveData
        isSendHomeWork = apiSchoolRepositories.isSendHomeWorkLiveData
        isAssignmentSend = apiSchoolRepositories.isSendAssignmentLiveData
        IsGetEventReport = apiSchoolRepositories.IsGetEventReportLiveData
        IsGetHolidayReport = apiSchoolRepositories.IsGetHolidayReportLiveData
        isVoiceSend = apiSchoolRepositories.isSendVoiceLiveData
        isUpdateStatusArchive = apiSchoolRepositories.isUpdateStatusArchiveLiveData
        isGetAcademicList = apiSchoolRepositories.isGetAcademicLiveData
        isUpdateStatusCommunication = apiSchoolRepositories.isUpdateStatusCommunicationLiveData
        isHomeWorkDetailsList = apiSchoolRepositories.isHomeWorkDetailsLiveData
        isHomeWorkDetailsListArchive = apiSchoolRepositories.isHomeWorkDetailsListLiveData
        isPunchAttendance = apiSchoolRepositories.isPunchAttendanceLiveData
        isAddLocation = apiSchoolRepositories.isAddLocationLiveData
        isRemoveLocation = apiSchoolRepositories.isRemoveLocationLiveData
        isUpdateLocation = apiSchoolRepositories.isUpdateLocationLiveData
        isLocationHistory = apiSchoolRepositories.isLocationHistoryLiveData
        isStaffLocations = apiSchoolRepositories.isStaffLocationsLiveData
        isPunchHistory = apiSchoolRepositories.isPunchHistoryLiveData
        isStaffAttendanceReport = apiSchoolRepositories.isGiometricStaffAttendanceReportLiveData
        isStaffWiseAttendanceReport =
            apiSchoolRepositories.isGiometricStaffWiseAttendanceReportLiveData
        isStaffWiseAttendanceReportList =
            apiSchoolRepositories.isGiometricStaffWiseAttendanceReportLiveDataList
        isStudentReportList = apiSchoolRepositories.isStudentReportLiveData
        isSendAbsenteeSMS = apiSchoolRepositories.isSendAbsenteeSMSLiveData

        isChildAttendanceReport = apiParentRepositories.isChildAttendanceReportLiveData
        isAttachmentResponse = apiParentRepositories.isAttachmentResponseLiveData
        isAttachmentResponseArchive = apiParentRepositories.isAttachmentResponseArchiveLiveData
        getabsenteescountbydate = apiSchoolRepositories.getabsenteescountbydateLiveData
        getabsenteesstudentbydate = apiSchoolRepositories.getabsenteesstudentbydateLiveData
        isGetStudentAttendanceReportData = apiSchoolRepositories.isStudentAttendanceReportLiveData

        sendnotice = apiSchoolRepositories.sendnoticeLiveData
        sendevent = apiSchoolRepositories.sendeventLiveData
        isAttachmentSend = apiSchoolRepositories.sendAttachmentLiveData
        isLeaveRequest = apiParentRepositories.leaveRequestLiveData
        getleaverequest = apiSchoolRepositories.leaverequestLiveData
        getleaverequest =apiSchoolRepositories.leaverequestLiveData
        isleaverequestapprove = apiSchoolRepositories.isleaverequestapproveLiveData
        isupdatelessonplan = apiSchoolRepositories.isupdatelessonplanLiveData

        getlpStaffReport = apiSchoolRepositories.isgetlpStaffReportLiveData
        getlpViewReport = apiSchoolRepositories.isgetlpViewReportLiveData
        getlpeditReport = apiSchoolRepositories.isgetlpeditReportLiveData

        islessonplandelete = apiSchoolRepositories.islessonplandeleteLiveData

        getcouponmenu = apiSchoolRepositories.getcouponmenuLiveData

        getCouponsSummary = apiSchoolRepositories.getCouponsSummaryLiveData
        getCouponsCategorySummary = apiSchoolRepositories.getCouponsCategorySummaryLiveData
        getmycouponsSummary = apiSchoolRepositories.getmycouponsSummaryLiveData

    }

    fun isDashBoardData(isToken: String, isMemberType: String, activity: Activity) {
        apiSchoolRepositories.isDashBoard(isToken, isMemberType, activity)
    }

    fun isGetAds(isToken: String, isMenuId: String, activity: Activity) {
        apiSchoolRepositories.isGetAds(isToken, isMenuId, activity)
    }

    fun isGetGlobalVariables(isToken: String, activity: Activity) {
        apiSchoolRepositories.isGetGlobalVariables(isToken, activity)
    }

    fun isGetStaffList(isToken: String, activity: Activity) {
        apiSchoolRepositories.isGetStaffList(isToken, activity)
    }

    fun isGetSubjectList(
        isToken: String,
        isAcademicYearId: Int,
        isSectionId: String,
        activity: Activity
    ) {
        apiSchoolRepositories.isGetSubjectList(isToken, isAcademicYearId, isSectionId, activity)
    }


    fun isGetStandardSection(isToken: String, isAcademicYearId: Int, activity: Activity) {
        Log.d("isAcademicYearIdData", isAcademicYearId.toString())
        apiSchoolRepositories.isGetStandardSection(isToken, isAcademicYearId, activity)
    }

    fun isGetStudentList(
        isToken: String,
        isSection: String,
        isAcademicYearId: Int,
        activity: Activity
    ) {
        apiSchoolRepositories.isGetStudentList(isToken, isSection, isAcademicYearId, activity)
    }

    fun isGetGroupList(isToken: String, isAcademicYearId: Int, activity: Activity) {
        apiSchoolRepositories.isGetGroupList(isToken, isAcademicYearId, activity)
    }

    fun isGetCommmunicationlistload(isToken: String, activity: Activity) {
        apiSchoolRepositories.isGetCommmunicationlistload(isToken, activity)
    }


    fun isGetCommmunicationlist(isToken: String, activity: Activity) {
        apiSchoolRepositories.isGetCommmunicationlist(isToken, activity)
    }

    fun isGetHomeWorkReport(
        isToken: String,
        isSectionId: Int,
        isAcademicYearId: Int,
        isdate: String,
        activity: Activity
    ) {
        apiSchoolRepositories.isGetHomeWorkReport(
            isToken,
            isSectionId,
            isAcademicYearId,
            isdate,
            activity
        )
    }


    fun isGetAssignmentReport(
        isToken: String,
        isAcademicYearId: Int,
        activity: Activity
    ) {
        apiSchoolRepositories.isGetAssignmentReport(
            isToken,
            isAcademicYearId,
            activity
        )
    }

    fun isAssignmentDelete(
        isToken: String,
        jsonObject: JsonObject,
        activity: Activity
    ) {
        apiSchoolRepositories.isDeleteAssignment(
            isToken,
            jsonObject,
            activity
        )
    }

    fun isGetDailyCollectionReport(
        isToken: String,
        istype: String,
        isfromdate: String,
        istodate: String,
        activity: Activity
    ) {
        apiSchoolRepositories.isGetDailyCollectionReport(
            isToken,
            istype,
            isfromdate,
            istodate,
            activity
        )
    }

    fun isDetailedPendingReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        apiSchoolRepositories.isDetailedPendingReport(isToken, isAcademicYearId, activity)
    }

    fun isDetailedWisePendingReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        apiSchoolRepositories.isDetailedWisePendingReport(isToken, isAcademicYearId, activity)
    }

    fun isGetSchoolStrengthReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        apiSchoolRepositories.isGetSchoolStrengthReport(isToken, isAcademicYearId, activity)
    }

    fun isNoticeBoardReport(isToken: String, activity: Activity) {
        apiSchoolRepositories.isNoticeBoardReport(isToken, activity)
    }

    fun IsGetEventReport(isToken: String, activity: Activity) {
        apiSchoolRepositories.IsGetEventReport(isToken, activity)
    }

    fun IsGetHolidayReport(isToken: String, activity: Activity) {
        apiSchoolRepositories.IsGetHolidayReport(isToken, activity)
    }

    fun isGetTextHistory(isToken: String, activity: Activity) {
        apiSchoolRepositories.isGetTextHistory(isToken, activity)
    }

    fun isGetVoiceHistory(isToken: String, isEmergency: String, activity: Activity) {
        apiSchoolRepositories.isGetVoiceHistory(isToken, isEmergency, activity)
    }

    fun isSendText(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.isSendText(isToken, josnObject, activity)
    }

    fun isSendHomeWork(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.isSendHomeWork(isToken, josnObject, activity)
    }

    fun isSendAssignment(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.isSendAssignment(isToken, josnObject, activity)
    }

    fun isVoiceSend(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.isSendVoice(isToken, josnObject, activity)
    }

    fun isUpdateStatusArchive(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories?.isUpdateStatusArchive(isToken, jsonObject, activity)
    }

    fun isGetAcademicYear(isToken: String, activity: Activity) {
        apiSchoolRepositories?.isGetAcademicYear(isToken, activity)
    }


    fun isUpdateStatusCommunication(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories?.isUpdateStatusCommunication(isToken, jsonObject, activity)
    }

    //get homework details
    fun isHomeWorkDetails(isToken: String, activity: Activity) {
        apiSchoolRepositories?.isHomeWorkDetails(isToken, activity)
    }

    //get homework details
    fun isHomeworkListArchive(isToken: String, activity: Activity) {
        apiSchoolRepositories.homework_list_archive(isToken, activity)
    }


    fun punchAttendance(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.punchAttendance(isToken, jsonObject, activity)
    }

    fun addLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.addLocation(isToken, jsonObject, activity)
    }

    fun removeLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.removeLocation(isToken, jsonObject, activity)
    }

    fun updateLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.updateLocation(isToken, jsonObject, activity)
    }

    fun getStaffLocations(isToken: String, activity: Activity) {
        apiSchoolRepositories.getStaffLocations(isToken, activity)
    }

    fun getPunchHistory(isToken: String, isDate: String, staff_id: String, activity: Activity) {
        apiSchoolRepositories.getPunchHistory(isToken, isDate, staff_id, activity)
    }

    fun getLocationHistory(isToken: String, activity: Activity) {
        apiSchoolRepositories.getLocationHistory(isToken, activity)
    }

    fun getStaffAttendanceReport(isToken: String, attendance_dt: String, activity: Activity) {
        apiSchoolRepositories.getGiometricStaffAttendancereport(isToken, attendance_dt, activity)
    }

    fun getStaffWiseAttendanceReport(isToken: String, isCurrentDate: String, activity: Activity) {
        apiSchoolRepositories.getGiometricStaffWiseAttendancereport(
            isToken,
            isCurrentDate,
            activity
        )
    }

    fun getStaffWiseAttendanceReportList(
        isToken: String,
        isSelectedDate: String,
        isStaffId: Int,
        activity: Activity
    ) {
        apiSchoolRepositories.getGiometricStaffWiseAttendancereportStaffList(
            isToken,
            isSelectedDate,
            isStaffId,
            activity
        )
    }

    //Get Student Report Details
    fun getStudentReportDetails(
        isToken: String,
        isAcademicYearId: Int,
        class_id: Int? = null,
        section_id: Int? = null,
        activity: Activity
    ) {
        apiSchoolRepositories.getStudentReportList(
            isToken,
            isAcademicYearId,
            class_id,
            section_id,
            activity
        )
    }

    fun isUpdateSendAbsenteeSMS(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.isUpdateSendAbsenteeSMS(isToken, jsonObject, activity)
    }

    fun getChildAttendanceReport(isToken: String, activity: Activity) {
        apiParentRepositories.getChildAttendanceReport(isToken, activity)
    }


    fun getabsenteescountbydate(isToken: String, activity: Activity) {
        apiSchoolRepositories.getabsenteescountbydate(isToken, activity)
    }

    fun getabsenteesstudentbydate(
        isToken: String,
        absent_on: String,
        section_id: String,
        activity: Activity
    ) {
        apiSchoolRepositories.getabsenteesstudentbydate(isToken, absent_on, section_id, activity)
    }

    fun getStudentAttendanceReport(
        isToken: String,
        section_id: String,
        from_date: String,
        to_date: String,
        class_id: String,
        activity: Activity
    ) {
        apiSchoolRepositories.getStudentAttendanceReportForSchool(
            isToken,
            section_id,
            from_date,
            to_date,
            class_id,
            activity
        )
    }


    fun sendnotice(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.sendnotice(isToken, josnObject, activity)
    }

    fun sendevent(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.sendevent(isToken, josnObject, activity)
    }

    fun sendAttachment(isToken: String, josnObject: JsonObject, activity: Activity) {
        apiSchoolRepositories.sendAttachment(isToken, josnObject, activity)
    }

    fun getAttachment(isToken: String, activity: Activity) {
        apiParentRepositories.getAttachmentList(isToken, activity)
    }

    fun getAttachmentArchive(isToken: String, activity: Activity) {
        apiParentRepositories.attachmentListArchive(isToken, activity)
    }

    fun isSendLeaveRequestApply(isToken: String, jsonObject: JsonObject, activity: Activity) {
        apiParentRepositories.isLeaveRequestApply(isToken, jsonObject, activity)
    }

    fun getleaverequest(isToken: String, member_type: String, activity: Activity) {
        apiSchoolRepositories.getleaverequest(isToken, member_type, activity)
    }



    fun getlpStaffReport(isToken: String, request_type: String, activity: Activity) {
        apiSchoolRepositories.getlpStaffReport(isToken,request_type,activity)
    }

    fun getlpViewReport(isToken: String, section_subject_id: String, lesson_plan_status: Int, activity : Activity) {

        apiSchoolRepositories.getlpViewReport(isToken,section_subject_id,lesson_plan_status,activity)
    }

    fun getlpeditReport(isToken: String, particular_id: String, request_type: String, activity : Activity) {

        apiSchoolRepositories.getlpeditReport(isToken,particular_id,request_type,activity)
    }

    fun isleaverequestapprove(isToken: String, request: LeaveApproveRequest, activity: Activity) {
        apiSchoolRepositories.isleaverequestapprove(isToken,request,activity)
    }

    fun isupdatelessonplan(isToken: String, requestBody: RequestBody, activity: Activity) {
        apiSchoolRepositories.isupdatelessonplan(isToken,requestBody,activity)
    }

    fun islessonplandelete(isToken: String,requestBody: RequestBody, activity: Activity) {
        apiSchoolRepositories.islessonplandelete(isToken,requestBody,activity)
    }

    fun getcouponmenu(parentName: String, apiKey: String) {
        apiSchoolRepositories.getcouponmenu(parentName,apiKey)
    }

    fun getCouponsSummary(mobile_no:String,parentName: String, apiKey: String) {
        apiSchoolRepositories.getCouponsSummary(mobile_no,parentName,apiKey)
    }

    fun getCouponsCategorySummary(category_id: String, mobile_no:String, parentName: String, apiKey: String) {
        apiSchoolRepositories.getCouponsCategorySummary(category_id,mobile_no,parentName,apiKey)
    }

    fun getmycouponsSummary(coupon_status: String, mobile_no:String, parentName: String, apiKey: String) {
        apiSchoolRepositories.getmycouponsSummary(coupon_status,mobile_no,parentName,apiKey)
    }

}



