package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Introduction.Model.GetFeature
import com.vs.schoolmessenger.CommonScreens.Ads.AdsResponse
import com.vs.schoolmessenger.CommonScreens.GlobalVariableResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardCountResponse
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYearResponse
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIdsResponse
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardResponse
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileListResponse
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Model.FrequentlyModelResponse
import com.vs.schoolmessenger.Dashboard.Settings.Notification.DeleteNotificationResponse
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.ReviewResponse
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.SubmitReviewResponse
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model.WhatsNewUpdateResponse
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
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentList
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.SendAbsenteeSMSResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportDataResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudentsResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesResponse
import com.vs.schoolmessenger.School.Assignment.AssignmentTargetDetails.AssignmentTargetDetailsResponse
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentResponse
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionResponse
import com.vs.schoolmessenger.School.Attachment.AttachmentTargetDetails.AttachmentTargetDetailResponse
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetailsResponse
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendResponse
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceDetails
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionReportResponse
import com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard.ChildStandardResponse
import com.vs.schoolmessenger.School.Event.Model.EventCategoryResponse
import com.vs.schoolmessenger.School.Event.Model.EventDeleteResponse
import com.vs.schoolmessenger.School.Event.Model.SchoolEventResponse
import com.vs.schoolmessenger.School.Event.Response.EventSendResponse
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExam
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivities
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data.MarkResponse
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Model.SaveMarksModel
import com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model.UploadMarkResponse
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeePendingReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerModelRequest
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockApiResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.AnswerModelResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.BlockedStudentsResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.QuestionResponse
import com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel.AvgSkillResponse
import com.vs.schoolmessenger.School.LSRW.Model.LsrwDeleteResponse
import com.vs.schoolmessenger.School.LSRW.Model.LsrwSkillSendResponse
import com.vs.schoolmessenger.School.LSRW.Model.LsrwremarkUpdateModel
import com.vs.schoolmessenger.School.LSRW.Model.lsrwskillresponse
import com.vs.schoolmessenger.School.LSRW.SubmissionStudentListModel.StudentSubmissionLsrwResponse
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import com.vs.schoolmessenger.School.LeaveRequests.Response.LeaveActionResponse
import com.vs.schoolmessenger.School.LeaveRequests.Response.LeaveRequestResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreateModel.LessonPlanCreateResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreateModel.LessonPlanTemplateResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanDeleteModel.LPDeleteResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.LessonPlanEditResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanUpdateModel.LessonPlanUpdateResponse
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchHistoryResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportResponse
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffLocationResponse
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaff
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeBoardStaffResponse
import com.vs.schoolmessenger.School.NoticeBoard.Response.NoticeBoardDeleteResponse
import com.vs.schoolmessenger.School.NoticeBoard.Response.NoticeBoardSendResponse
import com.vs.schoolmessenger.School.PTM.DataClass.BookedSlotResponse
import com.vs.schoolmessenger.School.PTM.DataClass.SlotBookingResponse
import com.vs.schoolmessenger.School.PTM.DataClass.SlotResponse
import com.vs.schoolmessenger.School.PTM.DataClass.SlotValidationResponse
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.AddQuestionResponse
import com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz.CreateQuizResponse
import com.vs.schoolmessenger.School.QuizExam.Model.DeleteQuiz.DeleteQuizResponse
import com.vs.schoolmessenger.School.QuizExam.Model.DeleteQuizQuestion.DeleteQuizQuestionResponse
import com.vs.schoolmessenger.School.QuizExam.Model.EditQuiz.EditQuizResponse
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBank
import com.vs.schoolmessenger.School.QuizExam.Model.QuizCheckLevel.GetCheckLevel
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReport
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReport
import com.vs.schoolmessenger.School.QuizExam.Model.QuizSubmissionList.GetQuizSubmissionList
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolStrengthResponse
import com.vs.schoolmessenger.School.StudentReport.GetStudentReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SchoolServices {

    var client_auth: RestClient
    var isDashBoard: MutableLiveData<DashboardResponse?>
    var isDashBoardCount: MutableLiveData<DashboardCountResponse?>
    var isGetAds: MutableLiveData<AdsResponse?>
    var isGetGlobalVariables: MutableLiveData<GlobalVariableResponse?>
    var isGetStaffList: MutableLiveData<NameAndIdsResponse?>
    var isGetSubjectList: MutableLiveData<NameAndIdsResponse?>
    var isGetStandardSection: MutableLiveData<StandardResponse?>
    var isGetStudentList: MutableLiveData<NameAndIdsResponse?>
    var isGetGroupList: MutableLiveData<NameAndIdsResponse?>
    var isGetCommmunicationlist: MutableLiveData<VoiceDataResponse?>
    var isGetCommmunicationlistload: MutableLiveData<VoiceDataResponse?>
    var isGetVoiceHistory: MutableLiveData<VoiceDetails?>
    var isGetTextHistory: MutableLiveData<TextDetailsResponse?>
    var isGetHomeWorkReport: MutableLiveData<HomeWorkReportApiResponse?>
    var isGetAssignmentReport: MutableLiveData<AssignmentResponse?>
    var isGetEventCategory: MutableLiveData<EventCategoryResponse?>
    var isAssignmentDelete: MutableLiveData<LPDeleteResponse?>
    var isGetDailyCollectionReport: MutableLiveData<DailyCollectionReportResponse?>
    var isSendText: MutableLiveData<TextSendResponse?>
    var isSendHomeWork: MutableLiveData<HomeWorkSendResponse?>
    var isSendAssignment: MutableLiveData<HomeWorkSendResponse?>
    var isUpdateAssignment: MutableLiveData<HomeWorkSendResponse?>
    var isUpdateHomeWork: MutableLiveData<StatusMessageModel?>
    var isUpdateEvent: MutableLiveData<StatusMessageModel?>
    var isUpdateAttachment: MutableLiveData<StatusMessageModel?>
    var isUpdateNoticeBoard: MutableLiveData<StatusMessageModel?>
    var isDeleteHomeWork: MutableLiveData<StatusMessageModel?>
    var isDeleteAttachment: MutableLiveData<StatusMessageModel?>
    var isSendVoice: MutableLiveData<TextSendResponse?>
    var isUpdateStatusArchive: MutableLiveData<StatusArchiveResponse?>
    var isAcademicYear: MutableLiveData<AcademicYearResponse?>
    var isUpdateStatusCommunication: MutableLiveData<StatusArchiveResponse?>
    var isHomeWorkDetailsData: MutableLiveData<GetHomeworkData?>
    var isNoticeBoardReport: MutableLiveData<NoticeBoardStaffResponse?>
    var isNoticeBoardStaffReport: MutableLiveData<NoticeBoardStaffResponse?>
    var isGetSchoolStrengthReport: MutableLiveData<SchoolStrengthResponse?>
    var isDetailedPendingReport: MutableLiveData<FeePendingReportResponse?>

    var isDetailedWisePendingReport: MutableLiveData<FeePendingReportResponse?>

    var isPunchAttendance: MutableLiveData<StatusMessageModel?>
    var isAddLocation: MutableLiveData<StatusMessageModel?>
    var isRemoveLocation: MutableLiveData<StatusMessageModel?>
    var isUpdateLocation: MutableLiveData<StatusMessageModel?>
    var isLocationHistory: MutableLiveData<LocationHistoryResponse?>
    var isStaffLocations: MutableLiveData<StaffLocationResponse?>
    var isPunchHistory: MutableLiveData<PunchHistoryResponse?>
    var isStaffAttendanceReport: MutableLiveData<StaffAttendanceReportResponse?>
    var isStaffWiseAttendanceReport: MutableLiveData<StaffAttendanceReportResponse?>
    var isStaffWiseAttendanceReportList: MutableLiveData<StaffAttendanceReportResponse?>
    var isStudentReportList: MutableLiveData<GetStudentReportData?>

    var IsGetEventReport: MutableLiveData<EventResponse?>
    var IsGetEventSchoolReport: MutableLiveData<SchoolEventResponse?>

    var IsGetHolidayReport: MutableLiveData<HolidayResponse?>
    var isSendAbsenteeSMS: MutableLiveData<SendAbsenteeSMSResponse?>
    var isStudentAttendanceReportForSchool: MutableLiveData<StudentAttendanceReportDataResponse?>

    var getabsenteescountbydate: MutableLiveData<AbsenteesResponse?>

    var getabsenteesstudentbydate: MutableLiveData<AbsenteeStudentsResponse?>


    var sendnotice: MutableLiveData<NoticeBoardSendResponse?>
    var sendevent: MutableLiveData<EventSendResponse?>
    var isSendAttachment: MutableLiveData<NoticeBoardSendResponse?>
    var getleaverequest: MutableLiveData<LeaveRequestResponse?>
    var isleaverequestapprove: MutableLiveData<LeaveActionResponse?>
    var getlpStaffReport: MutableLiveData<AllClassResponse?>
    var getlpViewReport: MutableLiveData<LessonPlanViewSummaryResponse?>
    var getlpeditReport: MutableLiveData<LessonPlanEditResponse?>
    var getlpcreateReport: MutableLiveData<LessonPlanTemplateResponse?>
    var isupdatelessonplan: MutableLiveData<LessonPlanUpdateResponse?>
    var iscreatelessonplan: MutableLiveData<LessonPlanCreateResponse?>
    var islessonplandelete: MutableLiveData<LPDeleteResponse?>
    var getcouponmenu: MutableLiveData<CouponMenuResponse?>
    var getCouponsSummary: MutableLiveData<CampaignResponse?>
    var getCouponsCategorySummary: MutableLiveData<CampaignResponse?>
    var getmycoupons: MutableLiveData<TicketSummaryResponse?>
    var getCouponDetails: MutableLiveData<ActivateCouponSummaryResponse?>
    var sendactivatecoupon: MutableLiveData<ActivateCouponResponse?>
    var getstaffquestions: MutableLiveData<QuestionResponse?>
    var sendanswer: MutableLiveData<AnswerModelResponse?>
    var isnoticeboarddelete: MutableLiveData<NoticeBoardDeleteResponse?>
    var isEventDelete: MutableLiveData<EventDeleteResponse?>
    var isLsrwDelete: MutableLiveData<LsrwDeleteResponse?>
    var isAttachmentResponse: MutableLiveData<AttachmentReportResponse?>
    var getassignmentlist: MutableLiveData<SubmissionResponse?>
    var islsrwskillsreport: MutableLiveData<lsrwskillresponse?>
    var islsrwStudentlist: MutableLiveData<StudentSubmissionLsrwResponse?>
    var islsrwSkillCreate: MutableLiveData<LsrwSkillSendResponse?>
    var islsrwstats: MutableLiveData<AvgSkillResponse?>
    var islsrwremarkupdate: MutableLiveData<LsrwremarkUpdateModel?>

    var isPtmSlotCreate: MutableLiveData<StatusMessageModel?>
    var isPtmSlotResponse: MutableLiveData<SlotResponse?>
    var isBookedSlotResponse: MutableLiveData<BookedSlotResponse?>
    var isPtmSlotCancelReOpen: MutableLiveData<StatusMessageModel?>
    var isPtmSlotCancelClose: MutableLiveData<StatusMessageModel?>
    var isDateWiseSlot: MutableLiveData<SlotBookingResponse?>
    var isSlotValidation: MutableLiveData<SlotValidationResponse?>
    var isCreateQuiz: MutableLiveData<CreateQuizResponse?>
    var isDeleteQuiz: MutableLiveData<DeleteQuizResponse?>
    var isEditQuiz: MutableLiveData<EditQuizResponse?>
    var isGetQuizExamReport: MutableLiveData<GetQuizExamReport?>
    var isGetCheckLevel: MutableLiveData<GetCheckLevel?>
    var isGetQuizQuestionReport: MutableLiveData<GetQuizQuestionReport?>
    var isGetQuizSubmissionList: MutableLiveData<GetQuizSubmissionList?>
    var isGetPickFromQBank: MutableLiveData<GetPickFromQBank?>
    var isAddQuestion: MutableLiveData<AddQuestionResponse?>
    var isGetMessageFromStaff: MutableLiveData<GetMessagesStaff?>
    var isGetMessageFromStaffArchive: MutableLiveData<GetMessagesStaff?>
    var isSchoolprofilelist: MutableLiveData<ProfileListResponse?>
    var getchildhomeworkstandard: MutableLiveData<ChildStandardResponse?>
    var getassignmentchildhomework: MutableLiveData<AssignmentTargetDetailsResponse?>
    var getattachmentchildhomework: MutableLiveData<AttachmentTargetDetailResponse?>
    var getdashboardnewupdates: MutableLiveData<WhatsNewUpdateResponse?>
    var getattendanceStudentList: MutableLiveData<GetAttendanceStudentList?>
    var isblockstudent: MutableLiveData<BlockApiResponse?>
    var isblockstudentlist: MutableLiveData<BlockedStudentsResponse?>
    var isfrequentlyasked: MutableLiveData<FrequentlyModelResponse?>
    var isdeletenotification: MutableLiveData<DeleteNotificationResponse?>
    var getreviewlist: MutableLiveData<ReviewResponse?>
    var reviewpost: MutableLiveData<SubmitReviewResponse?>
    var isgetfeature: MutableLiveData<GetFeature?>
    var isgetStaffWiseExam: MutableLiveData<getStaffWisExam?>
    var isgetSubjectWiseActivities: MutableLiveData<getSubjectWiseACtivities?>
    var isGetMarkDetails: MutableLiveData<MarkResponse?>
    var isgetDeleteQuizQuestion: MutableLiveData<DeleteQuizQuestionResponse?>
    var uploadmarks: MutableLiveData<UploadMarkResponse?>
    var savemarks: MutableLiveData<SaveMarksModel?>


    init {
        client_auth = RestClient()
        isDashBoard = MutableLiveData()
        isDashBoardCount = MutableLiveData()
        isGetAds = MutableLiveData()
        isGetGlobalVariables = MutableLiveData()
        isGetStaffList = MutableLiveData()
        isGetSubjectList = MutableLiveData()
        isGetStandardSection = MutableLiveData()
        isGetStudentList = MutableLiveData()
        isGetGroupList = MutableLiveData()
        isGetCommmunicationlist = MutableLiveData()
        isGetCommmunicationlistload = MutableLiveData()
        isGetVoiceHistory = MutableLiveData()
        isGetTextHistory = MutableLiveData()
        isGetHomeWorkReport = MutableLiveData()
        isGetAssignmentReport = MutableLiveData()
        isGetEventCategory = MutableLiveData()
        isAssignmentDelete = MutableLiveData()
        isNoticeBoardReport = MutableLiveData()
        isGetDailyCollectionReport = MutableLiveData()
        isSendText = MutableLiveData()
        isSendHomeWork = MutableLiveData()
        isSendAssignment = MutableLiveData()
        isUpdateAssignment = MutableLiveData()
        isUpdateHomeWork = MutableLiveData()
        isUpdateEvent = MutableLiveData()
        isUpdateAttachment = MutableLiveData()
        isDeleteHomeWork = MutableLiveData()
        isDeleteAttachment = MutableLiveData()
        isSendVoice = MutableLiveData()
        isUpdateStatusArchive = MutableLiveData()
        isAcademicYear = MutableLiveData()
        isUpdateStatusCommunication = MutableLiveData()
        isHomeWorkDetailsData = MutableLiveData()
        isDetailedPendingReport = MutableLiveData()
        isDetailedWisePendingReport = MutableLiveData()
        isGetSchoolStrengthReport = MutableLiveData()
        isPunchAttendance = MutableLiveData()
        isAddLocation = MutableLiveData()
        isUpdateNoticeBoard = MutableLiveData()
        isRemoveLocation = MutableLiveData()
        isUpdateLocation = MutableLiveData()
        isLocationHistory = MutableLiveData()
        isStaffLocations = MutableLiveData()
        isPunchHistory = MutableLiveData()
        isStaffAttendanceReport = MutableLiveData()
        isStaffWiseAttendanceReport = MutableLiveData()
        isStaffWiseAttendanceReportList = MutableLiveData()
        isStudentReportList = MutableLiveData()
        IsGetEventReport = MutableLiveData()
        IsGetEventSchoolReport = MutableLiveData()
        IsGetHolidayReport = MutableLiveData()
        isSendAbsenteeSMS = MutableLiveData()
        isStudentAttendanceReportForSchool = MutableLiveData()
        getabsenteescountbydate = MutableLiveData()
        getabsenteesstudentbydate = MutableLiveData()
        sendnotice = MutableLiveData()
        sendevent = MutableLiveData()
        isSendAttachment = MutableLiveData()
        getleaverequest = MutableLiveData()
        isleaverequestapprove = MutableLiveData()
        getlpStaffReport = MutableLiveData()
        getlpViewReport = MutableLiveData()
        getlpeditReport = MutableLiveData()
        getlpcreateReport = MutableLiveData()
        isupdatelessonplan = MutableLiveData()
        iscreatelessonplan = MutableLiveData()
        islessonplandelete = MutableLiveData()
        getcouponmenu = MutableLiveData()
        getCouponsSummary = MutableLiveData()
        getCouponsCategorySummary = MutableLiveData()
        getmycoupons = MutableLiveData()
        getCouponDetails = MutableLiveData()
        sendactivatecoupon = MutableLiveData()
        getstaffquestions = MutableLiveData()
        sendanswer = MutableLiveData()
        isnoticeboarddelete = MutableLiveData()
        isEventDelete = MutableLiveData()
        isLsrwDelete = MutableLiveData()
        isNoticeBoardStaffReport = MutableLiveData()
        isAttachmentResponse = MutableLiveData()
        getassignmentlist = MutableLiveData()
        islsrwskillsreport = MutableLiveData()
        islsrwStudentlist = MutableLiveData()
        islsrwSkillCreate = MutableLiveData()
        islsrwstats = MutableLiveData()
        islsrwremarkupdate = MutableLiveData()

        isPtmSlotCreate = MutableLiveData()
        isPtmSlotResponse = MutableLiveData()
        isBookedSlotResponse = MutableLiveData()
        isPtmSlotCancelReOpen = MutableLiveData()
        isPtmSlotCancelClose = MutableLiveData()
        isDateWiseSlot = MutableLiveData()
        isSlotValidation = MutableLiveData()
        isCreateQuiz = MutableLiveData()
        isGetQuizExamReport = MutableLiveData()
        isGetCheckLevel = MutableLiveData()
        isGetQuizQuestionReport = MutableLiveData()
        isGetQuizSubmissionList = MutableLiveData()
        isGetPickFromQBank = MutableLiveData()
        isAddQuestion = MutableLiveData()
        isGetMessageFromStaff = MutableLiveData()
        isSchoolprofilelist = MutableLiveData()
        getchildhomeworkstandard = MutableLiveData()
        getassignmentchildhomework = MutableLiveData()
        isGetMessageFromStaffArchive = MutableLiveData()
        getattachmentchildhomework = MutableLiveData()
        getdashboardnewupdates = MutableLiveData()
        getattendanceStudentList = MutableLiveData()
        isblockstudent = MutableLiveData()
        isblockstudentlist = MutableLiveData()
        isfrequentlyasked = MutableLiveData()
        isdeletenotification = MutableLiveData()
        getreviewlist = MutableLiveData()
        reviewpost = MutableLiveData()
        isgetfeature = MutableLiveData()
        isgetStaffWiseExam = MutableLiveData()
        isgetSubjectWiseActivities = MutableLiveData()
        isGetMarkDetails = MutableLiveData()
        isDeleteQuiz = MutableLiveData()
        isEditQuiz = MutableLiveData()
        isgetDeleteQuizQuestion = MutableLiveData()
        uploadmarks = MutableLiveData()
        savemarks = MutableLiveData()
    }

//Old Dashboard Api
//    fun isDashBoard(isToken: String, isMemberType: String, activity: Activity) {
//        RestClient.apiInterfaces.isDashBoard(isToken, isMemberType)
//            ?.enqueue(object : Callback<DashboardResponse?> {
//                override fun onResponse(
//                    call: Call<DashboardResponse?>, response: Response<DashboardResponse?>
//                ) {
//                    Log.d(
//                        "isGetCountryList", response.code().toString() + " - " + response.toString()
//                    )
//                    if (response.code() == 200) {
//                        if (response.body() != null) {
//                            val status = response.body()!!.status
//                            if (status) {
//                                isDashBoard.postValue(response.body())
//                            } else {
//                                isDashBoard.postValue(response.body())
//                            }
//                        }
//                    } else {
//
//
//                    }
//                }
//
//                override fun onFailure(call: Call<DashboardResponse?>, t: Throwable) {
//                    isDashBoard.postValue(null)
//                    t.printStackTrace()
//                }
//            })
//    }
//
//    val isDashBoardLiveData: LiveData<DashboardResponse?>
//        get() = isDashBoard


    //New Dashboard Api
    fun isDashBoard(
        isToken: String,
        isMemberType: String,
        isMobileNumber: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isDashBoard(isToken, isMemberType, isMobileNumber)
            ?.enqueue(object : Callback<DashboardResponse?> {
                override fun onResponse(
                    call: Call<DashboardResponse?>, response: Response<DashboardResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDashBoard.postValue(response.body())
                            } else {
                                isDashBoard.postValue(response.body())
                            }
                        }
                    } else {


                    }
                }

                override fun onFailure(call: Call<DashboardResponse?>, t: Throwable) {
                    isDashBoard.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isDashBoardLiveData: LiveData<DashboardResponse?>
        get() = isDashBoard


    fun isDashBoardCount(isToken: String, isMemberType: String, activity: Activity) {
        RestClient.apiInterfaces.isDashBoardCount(isToken, isMemberType)
            ?.enqueue(object : Callback<DashboardCountResponse?> {
                override fun onResponse(
                    call: Call<DashboardCountResponse?>, response: Response<DashboardCountResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDashBoardCount.postValue(response.body())
                            } else {
                                isDashBoardCount.postValue(response.body())
                            }
                        }
                    } else {


                    }
                }

                override fun onFailure(call: Call<DashboardCountResponse?>, t: Throwable) {
                    isDashBoardCount.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isDashBoardCountLiveData: LiveData<DashboardCountResponse?>
        get() = isDashBoardCount


    fun isGetAds(isToken: String, isMenuId: String, activity: Activity) {
        RestClient.apiInterfaces.isGetAds(isToken, isMenuId)
            ?.enqueue(object : Callback<AdsResponse?> {
                override fun onResponse(
                    call: Call<AdsResponse?>, response: Response<AdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetAds.postValue(response.body())
                            } else {
                                isGetAds.postValue(response.body())
                            }
                        }
                    } else {
                    }
                }

                override fun onFailure(call: Call<AdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetAdsLiveData: LiveData<AdsResponse?>
        get() = isGetAds

    fun isGetGlobalVariables(jsonObject: JsonObject, isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetGlobalVariable(jsonObject, isToken)
            ?.enqueue(object : Callback<GlobalVariableResponse?> {
                override fun onResponse(
                    call: Call<GlobalVariableResponse?>, response: Response<GlobalVariableResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetGlobalVariables.postValue(response.body())
                            } else {
                                isGetGlobalVariables.postValue(response.body())
                            }
                        }
                    } else {
                    }
                }

                override fun onFailure(call: Call<GlobalVariableResponse?>, t: Throwable) {
                    isGetGlobalVariables.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetGlobalVariablesLiveData: LiveData<GlobalVariableResponse?>
        get() = isGetGlobalVariables

    fun isGetStaffList(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getStaffList(isToken)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetStaffList.postValue(response.body())
                            } else {
                                isGetStaffList.postValue(response.body())
                            }
                        }
                    } else {
                        isGetStaffList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetStaffList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetStaffListLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetStaffList

    fun isGetSubjectList(
        isToken: String, isAcademicYearId: Int, isSection: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getSubjectList(isToken, isSection)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetSubjectList.postValue(response.body())
                            } else {
                                isGetSubjectList.postValue(response.body())
                            }
                        }
                    } else {
                        isGetSubjectList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSubjectListLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetSubjectList


    fun isGetStandardSection(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.getStandard(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<StandardResponse?> {
                override fun onResponse(
                    call: Call<StandardResponse?>, response: Response<StandardResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetStandardSection.postValue(response.body())
                            } else {
                                isGetStandardSection.postValue(response.body())
                            }
                        }
                    } else {
                        isGetStandardSection.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StandardResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetStandardSectionLiveData: LiveData<StandardResponse?>
        get() = isGetStandardSection


    fun isGetStudentList(
        isToken: String, isSection: String, isAcademicYearId: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.getStudentList(isToken, isSection, isAcademicYearId)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetStudentList.postValue(response.body())
                            } else {
                                isGetStudentList.postValue(response.body())
                            }
                        }
                    } else {
                        isGetStudentList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isStudentLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetStudentList


    fun isGetCommmunicationlist(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetCommmunicationlist(isToken)
            ?.enqueue(object : Callback<VoiceDataResponse?> {
                override fun onResponse(
                    call: Call<VoiceDataResponse?>, response: Response<VoiceDataResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetCommmunicationlist.postValue(response.body())
                            } else {
                                isGetCommmunicationlist.postValue(response.body())
                            }
                        }
                    } else {
                        isGetCommmunicationlist.postValue(null)
                    }
                }

                override fun onFailure(call: Call<VoiceDataResponse?>, t: Throwable) {
                    isGetCommmunicationlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetCommunicationLiveData: LiveData<VoiceDataResponse?>
        get() = isGetCommmunicationlist

    fun isGetCommmunicationlistload(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetCommmunicationlistload(isToken)
            ?.enqueue(object : Callback<VoiceDataResponse?> {
                override fun onResponse(
                    call: Call<VoiceDataResponse?>, response: Response<VoiceDataResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetCommmunicationlistload.postValue(response.body())
                            } else {
                                isGetCommmunicationlistload.postValue(response.body())
                            }
                        }
                    } else {
                        isGetCommmunicationlistload.postValue(null)
                    }
                }

                override fun onFailure(call: Call<VoiceDataResponse?>, t: Throwable) {
                    isGetCommmunicationlistload.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetCommunicationloadLiveData: LiveData<VoiceDataResponse?>
        get() = isGetCommmunicationlistload


    fun isGetGroupList(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.isGroupList(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<NameAndIdsResponse?> {
                override fun onResponse(
                    call: Call<NameAndIdsResponse?>, response: Response<NameAndIdsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetGroupList.postValue(response.body())
                            } else {
                                isGetGroupList.postValue(response.body())
                            }
                        }
                    } else {
                        isGetGroupList.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NameAndIdsResponse?>, t: Throwable) {
                    isGetGroupList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetGroupLiveData: LiveData<NameAndIdsResponse?>
        get() = isGetGroupList


    fun isGetVoiceHistory(isToken: String, isEmergency: String, activity: Activity) {
        RestClient.apiInterfaces.isGetVoiceHistory(isToken)
            ?.enqueue(object : Callback<VoiceDetails?> {
                override fun onResponse(
                    call: Call<VoiceDetails?>, response: Response<VoiceDetails?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetVoiceHistory.postValue(response.body())
                            } else {
                                isGetVoiceHistory.postValue(response.body())
                            }
                        }
                    } else {
                        isGetVoiceHistory.postValue(null)
                    }
                }

                override fun onFailure(call: Call<VoiceDetails?>, t: Throwable) {
                    isGetVoiceHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetVoiceHistoryLiveData: LiveData<VoiceDetails?>
        get() = isGetVoiceHistory

    fun isGetTextHistory(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetTextHistory(isToken)
            ?.enqueue(object : Callback<TextDetailsResponse?> {
                override fun onResponse(
                    call: Call<TextDetailsResponse?>, response: Response<TextDetailsResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            response.body()!!.status

                            isGetTextHistory.postValue(response.body())

                        }
                    } else {
                        isGetTextHistory.postValue(null)
                    }
                }

                override fun onFailure(call: Call<TextDetailsResponse?>, t: Throwable) {
                    isGetTextHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetTextHistoryLiveData: LiveData<TextDetailsResponse?>
        get() = isGetTextHistory


    fun isGetHomeWorkReport(
        isToken: String, isSection: Int, isAcademicYearId: Int, isdate: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isGetHomeWorkReport(isToken, isSection, isAcademicYearId, isdate)
            ?.enqueue(object : Callback<HomeWorkReportApiResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkReportApiResponse?>,
                    response: Response<HomeWorkReportApiResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetHomeWorkReport.postValue(response.body())
                            } else {
                                isGetHomeWorkReport.postValue(response.body())
                            }
                        }
                    } else {
                        isGetHomeWorkReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<HomeWorkReportApiResponse?>, t: Throwable) {
                    isGetHomeWorkReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetHomeWorkReportLiveData: LiveData<HomeWorkReportApiResponse?>
        get() = isGetHomeWorkReport

    fun isGetAssignmentReport(
        isToken: String, isAcademicYearId: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.isGetAssignmentReport(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<AssignmentResponse?> {
                override fun onResponse(
                    call: Call<AssignmentResponse?>, response: Response<AssignmentResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetAssignmentReport.postValue(response.body())
                            } else {
                                isGetAssignmentReport.postValue(response.body())
                            }
                        }
                    } else {
                        isGetAssignmentReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<AssignmentResponse?>, t: Throwable) {
                    isGetAssignmentReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetAssignmentReportLiveData: LiveData<AssignmentResponse?>
        get() = isGetAssignmentReport

    fun isEventCategories(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isEventCategories(isToken)
            ?.enqueue(object : Callback<EventCategoryResponse?> {
                override fun onResponse(
                    call: Call<EventCategoryResponse?>, response: Response<EventCategoryResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetEventCategory.postValue(response.body())
                            } else {
                                isGetEventCategory.postValue(response.body())
                            }
                        }
                    } else {
                        isGetEventCategory.postValue(null)
                    }
                }

                override fun onFailure(call: Call<EventCategoryResponse?>, t: Throwable) {
                    isGetEventCategory.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetEventCategoryLiveData: LiveData<EventCategoryResponse?>
        get() = isGetEventCategory

    fun isDeleteAssignment(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        RestClient.apiInterfaces.isAssignmentDelete(isToken, jsonObject)
            ?.enqueue(object : Callback<LPDeleteResponse?> {
                override fun onResponse(
                    call: Call<LPDeleteResponse?>, response: Response<LPDeleteResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isAssignmentDelete.postValue(response.body())
                            } else {
                                isAssignmentDelete.postValue(response.body())
                            }
                        }
                    } else {
                        isAssignmentDelete.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LPDeleteResponse?>, t: Throwable) {
                    isAssignmentDelete.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDeleteAssignmentLiveData: LiveData<LPDeleteResponse?>
        get() = isAssignmentDelete

    fun isEditHomeWork(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.isHomeWorkUpdate(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isUpdateHomeWork.postValue(response.body())
                            } else {
                                isUpdateHomeWork.postValue(response.body())
                            }
                        }
                    } else {
                        isUpdateHomeWork.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isUpdateHomeWork.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isUpdateHomeworkLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateHomeWork

    fun isEditEvent(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.isEventUpdate(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isUpdateEvent.postValue(response.body())
                            } else {
                                isUpdateEvent.postValue(response.body())
                            }
                        }
                    } else {
                        isUpdateEvent.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isUpdateEvent.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isUpdateEventLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateEvent


    fun isEditAttachment(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.isAttachmentUpdate(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isUpdateAttachment.postValue(response.body())
                            } else {
                                isUpdateAttachment.postValue(response.body())
                            }
                        }
                    } else {
                        isUpdateAttachment.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isUpdateAttachment.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isUpdateAttachmentLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateAttachment


    fun isEditNoticeBoard(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.isNoticeBoardUpdate(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isUpdateNoticeBoard.postValue(response.body())
                            } else {
                                isUpdateNoticeBoard.postValue(response.body())
                            }
                        }
                    } else {
                        isUpdateNoticeBoard.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isUpdateNoticeBoard.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isUpdateNoticeBoardLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateNoticeBoard


    fun isHomeWorkDelete(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.isHomeWorkDelete(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDeleteHomeWork.postValue(response.body())
                            } else {
                                isDeleteHomeWork.postValue(response.body())
                            }
                        }
                    } else {
                        isDeleteHomeWork.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isDeleteHomeWork.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDeleteHomeworkLiveData: LiveData<StatusMessageModel?>
        get() = isDeleteHomeWork

    fun isAttachmentDelete(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.isAttachmentDelete(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDeleteAttachment.postValue(response.body())
                            } else {
                                isDeleteAttachment.postValue(response.body())
                            }
                        }
                    } else {
                        isDeleteAttachment.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isDeleteAttachment.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isAttachmentLiveData: LiveData<StatusMessageModel?>
        get() = isDeleteAttachment


    fun isNoticeBoardReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isNoticeBoardReport(isToken)
            ?.enqueue(object : Callback<NoticeBoardStaffResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardStaffResponse?>,
                    response: Response<NoticeBoardStaffResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isNoticeBoardReport.postValue(response.body())
                            } else {
                                isNoticeBoardReport.postValue(response.body())
                            }
                        }
                    } else {
                        isNoticeBoardReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NoticeBoardStaffResponse?>, t: Throwable) {
                    isNoticeBoardReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isNoticeBoardReportLiveData: LiveData<NoticeBoardStaffResponse?>
        get() = isNoticeBoardReport


    fun isNoticeBoardStaffReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isNoticeBoardStaffReport(isToken)
            ?.enqueue(object : Callback<NoticeBoardStaffResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardStaffResponse?>,
                    response: Response<NoticeBoardStaffResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isNoticeBoardStaffReport.postValue(response.body())
                            } else {
                                isNoticeBoardStaffReport.postValue(response.body())
                            }
                        }
                    } else {
                        isNoticeBoardStaffReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<NoticeBoardStaffResponse?>, t: Throwable) {
                    isNoticeBoardStaffReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isNoticeBoardStaffReportLiveData: LiveData<NoticeBoardStaffResponse?>
        get() = isNoticeBoardStaffReport


    fun IsGetEventReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.IsGetEventReport(isToken)
            ?.enqueue(object : Callback<EventResponse?> {
                override fun onResponse(
                    call: Call<EventResponse?>, response: Response<EventResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                IsGetEventReport.postValue(response.body())
                            } else {
                                IsGetEventReport.postValue(response.body())
                            }
                        }
                    } else {
                        IsGetEventReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<EventResponse?>, t: Throwable) {
                    IsGetEventReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val IsGetEventReportLiveData: LiveData<EventResponse?>
        get() = IsGetEventReport


    fun IsGetEventSchoolReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.IsGetEventSchoolReport(isToken)
            ?.enqueue(object : Callback<SchoolEventResponse?> {
                override fun onResponse(
                    call: Call<SchoolEventResponse?>, response: Response<SchoolEventResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                IsGetEventSchoolReport.postValue(response.body())
                            } else {
                                IsGetEventSchoolReport.postValue(response.body())
                            }
                        }
                    } else {
                        IsGetEventSchoolReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<SchoolEventResponse?>, t: Throwable) {
                    IsGetEventSchoolReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val IsGetEventSchoolReportLiveData: LiveData<SchoolEventResponse?>
        get() = IsGetEventSchoolReport

    fun IsGetHolidayReport(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.IsGetHolidayReport(isToken)
            ?.enqueue(object : Callback<HolidayResponse?> {
                override fun onResponse(
                    call: Call<HolidayResponse?>, response: Response<HolidayResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                IsGetHolidayReport.postValue(response.body())
                            } else {
                                IsGetHolidayReport.postValue(response.body())
                            }
                        }
                    } else {
                        IsGetHolidayReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<HolidayResponse?>, t: Throwable) {
                    IsGetHolidayReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val IsGetHolidayReportLiveData: LiveData<HolidayResponse?>
        get() = IsGetHolidayReport


    fun isGetDailyCollectionReport(
        isToken: String, istype: String, isfromdate: String, istodate: String, country_id: String, activity: Activity
    ) {
        RestClient.apiInterfaces.isGetDailyCollectionReport(isToken, istype, isfromdate, istodate, country_id)
            ?.enqueue(object : Callback<DailyCollectionReportResponse?> {
                override fun onResponse(
                    call: Call<DailyCollectionReportResponse?>,
                    response: Response<DailyCollectionReportResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetDailyCollectionReport.postValue(response.body())
                            } else {
                                isGetDailyCollectionReport.postValue(response.body())
                            }
                        }
                    } else {
                        isGetDailyCollectionReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<DailyCollectionReportResponse?>, t: Throwable) {
                    isGetDailyCollectionReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetDailyCollectionReportLiveData: LiveData<DailyCollectionReportResponse?>
        get() = isGetDailyCollectionReport


    fun isGetSchoolStrengthReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        RestClient.apiInterfaces.isGetSchoolStrengthReport(isToken, isAcademicYearId)
            ?.enqueue(object : Callback<SchoolStrengthResponse?> {
                override fun onResponse(
                    call: Call<SchoolStrengthResponse?>, response: Response<SchoolStrengthResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isGetSchoolStrengthReport.postValue(response.body())
                            } else {
                                isGetSchoolStrengthReport.postValue(response.body())
                            }
                        }
                    } else {
                        isGetSchoolStrengthReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<SchoolStrengthResponse?>, t: Throwable) {
                    isGetSchoolStrengthReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isGetSchoolStrengthReportLiveData: LiveData<SchoolStrengthResponse?>
        get() = isGetSchoolStrengthReport


    fun isDetailedPendingReport(isToken: String, isAcademicYearId: Int, country_id: String, activity: Activity) {
        RestClient.apiInterfaces.isDetailedPendingReport(isToken, isAcademicYearId, country_id)
            ?.enqueue(object : Callback<FeePendingReportResponse?> {
                override fun onResponse(
                    call: Call<FeePendingReportResponse?>,
                    response: Response<FeePendingReportResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDetailedPendingReport.postValue(response.body())
                            } else {
                                isDetailedPendingReport.postValue(response.body())
                            }
                        }
                    } else {
                        isDetailedPendingReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<FeePendingReportResponse?>, t: Throwable) {
                    isDetailedPendingReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDetailedPendingReportLiveData: LiveData<FeePendingReportResponse?>
        get() = isDetailedPendingReport


    fun isDetailedWisePendingReport(isToken: String, isAcademicYearId: Int, country_id: String, activity: Activity) {
        RestClient.apiInterfaces.isDetailedWisePendingReport(isToken, isAcademicYearId, country_id)
            ?.enqueue(object : Callback<FeePendingReportResponse?> {
                override fun onResponse(
                    call: Call<FeePendingReportResponse?>,
                    response: Response<FeePendingReportResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isDetailedWisePendingReport.postValue(response.body())
                            } else {
                                isDetailedWisePendingReport.postValue(response.body())
                            }
                        }
                    } else {
                        isDetailedWisePendingReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<FeePendingReportResponse?>, t: Throwable) {
                    isDetailedWisePendingReport.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val isDetailedWisePendingReportLiveData: LiveData<FeePendingReportResponse?>
        get() = isDetailedWisePendingReport


    fun isSendText(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.isSendText(isToken, jsonObject)
            ?.enqueue(object : Callback<TextSendResponse?> {
                override fun onResponse(
                    call: Call<TextSendResponse?>, response: Response<TextSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendText.postValue(response.body())
                    } else {
                        isSendText.postValue(response.body())
                    }


                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<TextSendResponse?>, t: Throwable) {
                    isSendText.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSendTextLiveData: LiveData<TextSendResponse?>
        get() = isSendText


    fun isSendHomeWork(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isSendHomeWork(isToken, jsonObject)
            ?.enqueue(object : Callback<HomeWorkSendResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkSendResponse?>, response: Response<HomeWorkSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendHomeWork.postValue(response.body())
                    } else {
                        isSendHomeWork.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<HomeWorkSendResponse?>, t: Throwable) {
                    isSendHomeWork.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSendHomeWorkLiveData: LiveData<HomeWorkSendResponse?>
        get() = isSendHomeWork

    fun isSendAssignment(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isAssignmentSend(isToken, jsonObject)
            ?.enqueue(object : Callback<HomeWorkSendResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkSendResponse?>, response: Response<HomeWorkSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendAssignment.postValue(response.body())
                    } else {
                        isSendAssignment.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<HomeWorkSendResponse?>, t: Throwable) {
                    isSendAssignment.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isSendAssignmentLiveData: LiveData<HomeWorkSendResponse?>
        get() = isSendAssignment

    fun assignmentUpdate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isAssignmentUpdate(isToken, jsonObject)
            ?.enqueue(object : Callback<HomeWorkSendResponse?> {
                override fun onResponse(
                    call: Call<HomeWorkSendResponse?>, response: Response<HomeWorkSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isUpdateAssignment.postValue(response.body())
                    } else {
                        isUpdateAssignment.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<HomeWorkSendResponse?>, t: Throwable) {
                    isUpdateAssignment.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isUpdateAssignmentLiveData: LiveData<HomeWorkSendResponse?>
        get() = isUpdateAssignment


    fun isUpdateStatusArchive(isToken: String, jsonObject: JsonObject, activity: Activity) {
//        val request = StatusArchiveModelRequest(isToken, jsonObject)

        RestClient.apiInterfaces.isUpdateStatusArchive(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusArchiveResponse> {
                override fun onResponse(
                    call: Call<StatusArchiveResponse>, response: Response<StatusArchiveResponse>
                ) {
                    Log.d("isUpdateStatusArchive", "${response.code()} - ${response.body()}")

                    if (response.code() == 200 && response.body() != null) {
                        isUpdateStatusArchive.postValue(response.body())
                    } else {
                        isUpdateStatusArchive.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<StatusArchiveResponse>, t: Throwable) {
                    isUpdateStatusArchive.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isUpdateStatusArchiveLiveData: LiveData<StatusArchiveResponse?>
        get() = isUpdateStatusArchive


    fun isUpdateStatusCommunication(isToken: String, jsonObject: JsonObject, activity: Activity) {
//        val request = StatusArchiveModelRequest(isToken, jsonObject)

        RestClient.apiInterfaces.isUpdateStatusCommunication(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusArchiveResponse> {
                override fun onResponse(
                    call: Call<StatusArchiveResponse>, response: Response<StatusArchiveResponse>
                ) {
                    Log.d("isUpdateStatusArchive", "${response.code()} - ${response.body()}")

                    if (response.code() == 200 && response.body() != null) {
                        isUpdateStatusCommunication.postValue(response.body())
                    } else {
                        isUpdateStatusCommunication.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<StatusArchiveResponse>, t: Throwable) {
                    isUpdateStatusCommunication.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isUpdateStatusCommunicationLiveData: LiveData<StatusArchiveResponse?>
        get() = isUpdateStatusCommunication


    fun isSendVoice(isToken: String, jsonObject: JsonObject, activity: Activity) {

        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.isSendVoice(isToken, jsonObject)
            ?.enqueue(object : Callback<TextSendResponse?> {
                override fun onResponse(
                    call: Call<TextSendResponse?>, response: Response<TextSendResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    isSendVoice.postValue(response.body())
                }

                override fun onFailure(call: Call<TextSendResponse?>, t: Throwable) {
                    isSendVoice.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSendVoiceLiveData: LiveData<TextSendResponse?>
        get() = isSendVoice


    fun isGetAcademicYear(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.isGetAcademicYear(isToken)
            ?.enqueue(object : Callback<AcademicYearResponse?> {
                override fun onResponse(
                    call: Call<AcademicYearResponse?>, response: Response<AcademicYearResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isAcademicYear.postValue(response.body())
                            } else {
                                isAcademicYear.postValue(response.body())
                            }
                        }
                    } else {
                        isAcademicYear.postValue(null)
                    }
                }

                override fun onFailure(call: Call<AcademicYearResponse?>, t: Throwable) {
                    isAcademicYear.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetAcademicLiveData: LiveData<AcademicYearResponse?>
        get() = isAcademicYear


    //    //get HomeworkDetails
    fun isHomeWorkDetails(isToken: String, activity: Activity,date: String) {
        Log.d("GetHomeworkData", isToken.toString())
        RestClient.apiInterfaces.isHomeWorkDetails(isToken,date)
            ?.enqueue(object : Callback<GetHomeworkData?> {
                override fun onResponse(
                    call: Call<GetHomeworkData?>, response: Response<GetHomeworkData?>
                ) {
                    Log.d("GetHomeworkData", response.body().toString())

                    if (response.code() == 200) {
                        if (response.body() != null) {
                            isHomeWorkDetailsData.postValue(response.body())
                            Log.d("GetHomeworkDataRespone", response.body().toString())

                        }
                    } else {
                        isHomeWorkDetailsData.postValue(null)
                    }
                }

                override fun onFailure(call: Call<GetHomeworkData?>, t: Throwable) {
                    t.printStackTrace()
                    isHomeWorkDetailsData.postValue(null)
                    Log.d("GetHomeworkData", "Response,No Data Found")
                }
            })
    }

    val isHomeWorkDetailsLiveData: LiveData<GetHomeworkData?>
        get() = isHomeWorkDetailsData


    //    //get HomeworkDetails
    fun homework_list_archive(isToken: String, activity: Activity) {
        Log.d("GetHomeworkData", isToken.toString())
        RestClient.apiInterfaces.homework_list_archive(isToken)
            ?.enqueue(object : Callback<GetHomeworkData?> {
                override fun onResponse(
                    call: Call<GetHomeworkData?>, response: Response<GetHomeworkData?>
                ) {
                    Log.d("GetHomeworkData", response.body().toString())

                    if (response.code() == 200) {
                        if (response.body() != null) {
                            isHomeWorkDetailsData.postValue(response.body())
                            Log.d("GetHomeworkDataRespone", response.body().toString())

                        }
                    } else {
                        isHomeWorkDetailsData.postValue(null)
                    }
                }

                override fun onFailure(call: Call<GetHomeworkData?>, t: Throwable) {
                    t.printStackTrace()
                    isHomeWorkDetailsData.postValue(null)
                    Log.d("GetHomeworkData", "Response,No Data Found")
                }
            })
    }

    val isHomeWorkDetailsListLiveData: LiveData<GetHomeworkData?>
        get() = isHomeWorkDetailsData


    fun punchAttendance(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.punchGiometricAttendance(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            isPunchAttendance.postValue(response.body())
                        }
                    } else {
                        isPunchAttendance.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isPunchAttendance.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPunchAttendanceLiveData: LiveData<StatusMessageModel?>
        get() = isPunchAttendance


    fun addLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.addGiometricLocation(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "addLocation_res", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isAddLocation.postValue(response.body())
                            } else {
                                isAddLocation.postValue(response.body())
                            }
                        }
                    } else {
                        isAddLocation.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isAddLocation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAddLocationLiveData: LiveData<StatusMessageModel?>
        get() = isAddLocation


    fun removeLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.removeLocation(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "remove_location_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isRemoveLocation.postValue(response.body())
                            } else {
                                isRemoveLocation.postValue(response.body())
                            }
                        }
                    } else {
                        isRemoveLocation.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isRemoveLocation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isRemoveLocationLiveData: LiveData<StatusMessageModel?>
        get() = isRemoveLocation


    fun updateLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.updateLocation(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "update_location_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isUpdateLocation.postValue(response.body())
                            } else {
                                isUpdateLocation.postValue(response.body())
                            }
                        }
                    } else {
                        isUpdateLocation.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isUpdateLocation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isUpdateLocationLiveData: LiveData<StatusMessageModel?>
        get() = isUpdateLocation


    fun getStaffLocations(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getStaffLocations(isToken)
            ?.enqueue(object : Callback<StaffLocationResponse?> {
                override fun onResponse(
                    call: Call<StaffLocationResponse?>, response: Response<StaffLocationResponse?>
                ) {
                    Log.d(
                        "staff_locations_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffLocations.postValue(response.body())
                            } else {
                                isStaffLocations.postValue(response.body())
                            }
                        }
                    } else {
                        isStaffLocations.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<StaffLocationResponse?>, t: Throwable
                ) {
                    isStaffLocations.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isStaffLocationsLiveData: LiveData<StaffLocationResponse?>
        get() = isStaffLocations

    fun getLocationHistory(isToken: String, activity: Activity) {
        RestClient.apiInterfaces.getLocationHistory(isToken)
            ?.enqueue(object : Callback<LocationHistoryResponse?> {
                override fun onResponse(
                    call: Call<LocationHistoryResponse?>,
                    response: Response<LocationHistoryResponse?>
                ) {
                    Log.d(
                        "location_history_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isLocationHistory.postValue(response.body())
                            } else {
                                isLocationHistory.postValue(response.body())
                            }
                        }
                    } else {
                        isLocationHistory.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<LocationHistoryResponse?>, t: Throwable
                ) {
                    isLocationHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isLocationHistoryLiveData: LiveData<LocationHistoryResponse?>
        get() = isLocationHistory


//    fun getPunchHistory(isToken: String, activity: Activity) {
//        RestClient.apiInterfaces.getPunchHistory(isToken)

    fun getPunchHistory(isToken: String, isDate: String, staff_id: String, activity: Activity) {
        RestClient.apiInterfaces.getPunchHistory(isToken, isDate, isDate, staff_id)
            ?.enqueue(object : Callback<PunchHistoryResponse?> {
                override fun onResponse(
                    call: Call<PunchHistoryResponse?>, response: Response<PunchHistoryResponse?>
                ) {
                    Log.d(
                        "punch_history_res",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
//                        if (response.body() != null) {
                        val status = response.body()!!.status
                        if (status) {
                            isPunchHistory.postValue(response.body())
                        } else {
                            isPunchHistory.postValue(response.body())
                        }
//                        }
                    } else {
                        isPunchHistory.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<PunchHistoryResponse?>, t: Throwable
                ) {
                    isPunchHistory.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isPunchHistoryLiveData: LiveData<PunchHistoryResponse?>
        get() = isPunchHistory


    fun getGiometricStaffAttendancereport(
        isToken: String, attendance_dt: String, activity: Activity
    ) {

        RestClient.apiInterfaces.getStaffAttendanceReport(isToken, attendance_dt)
            ?.enqueue(object : Callback<StaffAttendanceReportResponse?> {
                override fun onResponse(
                    call: Call<StaffAttendanceReportResponse?>,
                    response: Response<StaffAttendanceReportResponse?>
                ) {
                    Log.d(
                        "staff_attendance_report",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffAttendanceReport.postValue(response.body())
                            } else {
                                isStaffAttendanceReport.postValue(response.body())
                            }
                        }
                    } else {
                        isStaffAttendanceReport.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<StaffAttendanceReportResponse?>, t: Throwable
                ) {
                    isStaffAttendanceReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isGiometricStaffAttendanceReportLiveData: LiveData<StaffAttendanceReportResponse?>
        get() = isStaffAttendanceReport


    fun getGiometricStaffWiseAttendancereport(
        isToken: String, isCurrentDate: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getStaffWiseAttendanceReport(isToken, isCurrentDate)
            ?.enqueue(object : Callback<StaffAttendanceReportResponse?> {
                override fun onResponse(
                    call: Call<StaffAttendanceReportResponse?>,
                    response: Response<StaffAttendanceReportResponse?>
                ) {
                    Log.d(
                        "staffwise_attendance_report",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isStaffWiseAttendanceReport.postValue(response.body())
                            } else {
                                isStaffWiseAttendanceReport.postValue(response.body())
                            }
                        }
                    } else {
                        isStaffWiseAttendanceReport.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<StaffAttendanceReportResponse?>, t: Throwable
                ) {
                    isStaffWiseAttendanceReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGiometricStaffWiseAttendanceReportLiveData: LiveData<StaffAttendanceReportResponse?>
        get() = isStaffWiseAttendanceReport

    fun getGiometricStaffWiseAttendancereportStaffList(
        isToken: String, isSelectedDate: String, isStaffId: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.getStaffWiseAttendanceReportStaffList(
            isToken, isSelectedDate, isStaffId
        )?.enqueue(object : Callback<StaffAttendanceReportResponse?> {
            override fun onResponse(
                call: Call<StaffAttendanceReportResponse?>,
                response: Response<StaffAttendanceReportResponse?>
            ) {
                Log.d(
                    "staffwise_attendance_report",
                    response.code().toString() + " - " + response.toString()
                )
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val status = response.body()!!.status
                        if (status) {
                            isStaffWiseAttendanceReportList.postValue(response.body())
                        } else {
                            isStaffWiseAttendanceReportList.postValue(response.body())
                        }
                    }
                } else {
                    isStaffWiseAttendanceReportList.postValue(null)
                }
            }

            override fun onFailure(
                call: Call<StaffAttendanceReportResponse?>, t: Throwable
            ) {
                isStaffWiseAttendanceReportList.postValue(null)
                t.printStackTrace()
            }
        })
    }

    val isGiometricStaffWiseAttendanceReportLiveDataList: LiveData<StaffAttendanceReportResponse?>
        get() = isStaffWiseAttendanceReportList


    fun getStudentReportList(
        isToken: String,
        isAcademicYearId: Int,
        class_id: Int? = null,
        section_id: Int? = null,
        activity: Activity
    ) {
        RestClient.apiInterfaces.getStudentReport(isToken, isAcademicYearId, class_id, section_id)
            ?.enqueue(object : Callback<GetStudentReportData?> {
                override fun onResponse(
                    call: Call<GetStudentReportData?>, response: Response<GetStudentReportData?>
                ) {
                    Log.d(
                        "GetStudentReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentReportData", response.body().toString())
                                isStudentReportList.postValue(response.body())
                            } else {
                                Log.d("GetStudentReportData", response.body().toString())
                                isStudentReportList.postValue(response.body())
                            }
                        }
                    } else {
                        isStudentReportList.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<GetStudentReportData?>, t: Throwable
                ) {
                    isStudentReportList.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isStudentReportLiveData: LiveData<GetStudentReportData?>
        get() = isStudentReportList


    fun getabsenteescountbydate(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getabsenteescountbydate(isToken)
            ?.enqueue(object : Callback<AbsenteesResponse?> {
                override fun onResponse(
                    call: Call<AbsenteesResponse?>, response: Response<AbsenteesResponse?>
                ) {
                    Log.d(
                        "GetStudentReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteescountbydate.postValue(response.body())
                            } else {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteescountbydate.postValue(response.body())
                            }
                        }
                    } else {
                        getabsenteescountbydate.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<AbsenteesResponse?>, t: Throwable
                ) {
                    getabsenteescountbydate.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getabsenteescountbydateLiveData: LiveData<AbsenteesResponse?>
        get() = getabsenteescountbydate


    fun getabsenteesstudentbydate(
        isToken: String,
        absent_on: String? = null,
        standard_id: String,
        section_id: String? = null,
        activity: Activity

    ) {
        RestClient.apiInterfaces.getabsenteesstudentbydate(
            isToken,
            absent_on,
            standard_id,
            section_id
        )
            ?.enqueue(object : Callback<AbsenteeStudentsResponse?> {
                override fun onResponse(
                    call: Call<AbsenteeStudentsResponse?>,
                    response: Response<AbsenteeStudentsResponse?>
                ) {
                    Log.d(
                        "GetStudentReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteesstudentbydate.postValue(response.body())
                            } else {
                                Log.d("GetStudentReportData", response.body().toString())
                                getabsenteesstudentbydate.postValue(response.body())
                            }
                        }
                    } else {
                        getabsenteesstudentbydate.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<AbsenteeStudentsResponse?>, t: Throwable
                ) {
                    getabsenteesstudentbydate.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getabsenteesstudentbydateLiveData: LiveData<AbsenteeStudentsResponse?>
        get() = getabsenteesstudentbydate


    fun isUpdateSendAbsenteeSMS(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.apiInterfaces.UpdateSendAbsenteeSMS(isToken, jsonObject)
            ?.enqueue(object : Callback<SendAbsenteeSMSResponse?> {
                override fun onResponse(
                    call: Call<SendAbsenteeSMSResponse?>,
                    response: Response<SendAbsenteeSMSResponse?>
                ) {
                    Log.d(
                        "SendAbsenteeSMS", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isSendAbsenteeSMS.postValue(response.body())
                            } else {
                                isSendAbsenteeSMS.postValue(response.body())
                            }
                        }
                    } else {
                        isSendAbsenteeSMS.postValue(null)
                    }
                }

                override fun onFailure(call: Call<SendAbsenteeSMSResponse?>, t: Throwable) {
                    isSendAbsenteeSMS.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSendAbsenteeSMSLiveData: LiveData<SendAbsenteeSMSResponse?>
        get() = isSendAbsenteeSMS


    fun getStudentAttendanceReportForSchool(
        isToken: String,
        section_id: String,
        from_date: String,
        to_date: String,
        class_id: String,
        activity: Activity
    ) {
        RestClient.apiInterfaces.isGetStudentAttendanceReportForSchool(
            isToken, section_id, from_date, to_date, class_id
        )?.enqueue(object : Callback<StudentAttendanceReportDataResponse?> {
            override fun onResponse(
                call: Call<StudentAttendanceReportDataResponse?>,
                response: Response<StudentAttendanceReportDataResponse?>
            ) {
                Log.d(
                    "GetStudentAttendanceReport Response",
                    response.code().toString() + " - " + response.toString()
                )
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val status = response.body()!!.status
                        if (status) {
                            Log.d("GetStudentAttendanceReportData", response.body().toString())
                            isStudentAttendanceReportForSchool.postValue(response.body())
                        } else {
                            Log.d("GetStudentAttendanceReportData", response.body().toString())
                            isStudentAttendanceReportForSchool.postValue(response.body())
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<StudentAttendanceReportDataResponse?>, t: Throwable
            ) {
                isStudentAttendanceReportForSchool.postValue(null)
                t.printStackTrace()
            }
        })
    }


    val isStudentAttendanceReportLiveData: LiveData<StudentAttendanceReportDataResponse?>
        get() = isStudentAttendanceReportForSchool


    fun sendnotice(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.sendnotice(isToken, jsonObject)
            ?.enqueue(object : Callback<NoticeBoardSendResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardSendResponse?>,
                    response: Response<NoticeBoardSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        sendnotice.postValue(response.body())
                    } else {
                        sendnotice.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<NoticeBoardSendResponse?>, t: Throwable) {
                    sendnotice.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendnoticeLiveData: LiveData<NoticeBoardSendResponse?>
        get() = sendnotice


    fun sendevent(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.sendevent(isToken, jsonObject)
            ?.enqueue(object : Callback<EventSendResponse?> {
                override fun onResponse(
                    call: Call<EventSendResponse?>, response: Response<EventSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        sendevent.postValue(response.body())
                    } else {
                        sendevent.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<EventSendResponse?>, t: Throwable) {
                    sendevent.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendeventLiveData: LiveData<EventSendResponse?>
        get() = sendevent


    fun sendAttachment(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.sendAttachment(isToken, jsonObject)
            ?.enqueue(object : Callback<NoticeBoardSendResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardSendResponse?>,
                    response: Response<NoticeBoardSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        isSendAttachment.postValue(response.body())
                    } else {
                        isSendAttachment.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<NoticeBoardSendResponse?>, t: Throwable) {
                    isSendAttachment.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendAttachmentLiveData: LiveData<NoticeBoardSendResponse?>
        get() = isSendAttachment


    fun getleaverequest(
        isToken: String, member_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getleaverequest(isToken, member_type)
            ?.enqueue(object : Callback<LeaveRequestResponse?> {
                override fun onResponse(
                    call: Call<LeaveRequestResponse?>, response: Response<LeaveRequestResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getleaverequest.postValue(response.body())
                            } else {
                                getleaverequest.postValue(response.body())
                            }
                        }
                    } else {
                        getleaverequest.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LeaveRequestResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val leaverequestLiveData: LiveData<LeaveRequestResponse?>
        get() = getleaverequest


    fun isleaverequestapprove(
        isToken: String, request: LeaveApproveRequest, activity: Activity
    ) {
        RestClient.apiInterfaces.isleaverequestapprove(isToken, request)
            ?.enqueue(object : Callback<LeaveActionResponse?> {
                override fun onResponse(
                    call: Call<LeaveActionResponse?>, response: Response<LeaveActionResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isleaverequestapprove.postValue(response.body())
                            } else {
                                isleaverequestapprove.postValue(response.body())
                            }
                        }
                    } else {
                        isleaverequestapprove.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LeaveActionResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isleaverequestapproveLiveData: LiveData<LeaveActionResponse?>
        get() = isleaverequestapprove


    fun getlpStaffReport(
        isToken: String, request_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpStaffReport(isToken, request_type)
            ?.enqueue(object : Callback<AllClassResponse?> {
                override fun onResponse(
                    call: Call<AllClassResponse?>, response: Response<AllClassResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpStaffReport.postValue(response.body())
                            } else {
                                getlpStaffReport.postValue(response.body())
                            }
                        }
                    } else {
                        getlpStaffReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<AllClassResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpStaffReportLiveData: LiveData<AllClassResponse?>
        get() = getlpStaffReport


    fun getlpViewReport(
        isToken: String, section_subject_id: String, lesson_plan_status: Int, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpViewReport(isToken, section_subject_id, lesson_plan_status)
            ?.enqueue(object : Callback<LessonPlanViewSummaryResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanViewSummaryResponse?>,
                    response: Response<LessonPlanViewSummaryResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpViewReport.postValue(response.body())
                            } else {
                                getlpViewReport.postValue(response.body())
                            }
                        }
                    } else {
                        getlpViewReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanViewSummaryResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpViewReportLiveData: LiveData<LessonPlanViewSummaryResponse?>
        get() = getlpViewReport


    fun getlpeditReport(
        isToken: String, particular_id: String, request_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpeditReport(isToken, particular_id, request_type)
            ?.enqueue(object : Callback<LessonPlanEditResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanEditResponse?>, response: Response<LessonPlanEditResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpeditReport.postValue(response.body())
                            } else {
                                getlpeditReport.postValue(response.body())
                            }
                        }
                    } else {
                        getlpeditReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanEditResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpeditReportLiveData: LiveData<LessonPlanEditResponse?>
        get() = getlpeditReport


    fun getlpcreateReport(
        isToken: String, request_type: String, activity: Activity
    ) {
        RestClient.apiInterfaces.getlpcreateReport(isToken, request_type)
            ?.enqueue(object : Callback<LessonPlanTemplateResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanTemplateResponse?>,
                    response: Response<LessonPlanTemplateResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                getlpcreateReport.postValue(response.body())
                            } else {
                                getlpcreateReport.postValue(response.body())
                            }
                        }
                    } else {
                        getlpcreateReport.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanTemplateResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isgetlpcreateReportLiveData: LiveData<LessonPlanTemplateResponse?>
        get() = getlpcreateReport


    fun isupdatelessonplan(isToken: String, requestBody: RequestBody, activity: Activity) {
        RestClient.apiInterfaces.isupdatelessonplan(isToken, requestBody)
            ?.enqueue(object : Callback<LessonPlanUpdateResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanUpdateResponse?>,
                    response: Response<LessonPlanUpdateResponse?>
                ) {
                    Log.d("isGetCountryList", "${response.code()} - $response")
                    if (response.code() == 200 && response.body() != null) {
                        isupdatelessonplan.postValue(response.body())
                    } else {
                        isupdatelessonplan.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanUpdateResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val isupdatelessonplanLiveData: LiveData<LessonPlanUpdateResponse?>
        get() = isupdatelessonplan


    fun iscreatelessonplan(isToken: String, requestBody: RequestBody, activity: Activity) {
        RestClient.apiInterfaces.iscreatelessonplan(isToken, requestBody)
            ?.enqueue(object : Callback<LessonPlanCreateResponse?> {
                override fun onResponse(
                    call: Call<LessonPlanCreateResponse?>,
                    response: Response<LessonPlanCreateResponse?>
                ) {
                    Log.d("isGetCountryList", "${response.code()} - $response")
                    if (response.code() == 200 && response.body() != null) {
                        iscreatelessonplan.postValue(response.body())
                    } else {
                        iscreatelessonplan.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LessonPlanCreateResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val iscreatelessonplanLiveData: LiveData<LessonPlanCreateResponse?>
        get() = iscreatelessonplan


    fun islessonplandelete(isToken: String, requestBody: RequestBody, activity: Activity) {
        RestClient.apiInterfaces.islessonplandelete(isToken, requestBody)
            ?.enqueue(object : Callback<LPDeleteResponse?> {
                override fun onResponse(
                    call: Call<LPDeleteResponse?>, response: Response<LPDeleteResponse?>
                ) {
                    Log.d("isGetCountryList", "${response.code()} - $response")
                    if (response.code() == 200 && response.body() != null) {
                        islessonplandelete.postValue(response.body())
                    } else {
                        islessonplandelete.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LPDeleteResponse?>, t: Throwable) {
                    isGetAds.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val islessonplandeleteLiveData: LiveData<LPDeleteResponse?>
        get() = islessonplandelete


    fun getcouponmenu(
        parentname: String, apiKey: String
    ) {
        RestClient.couponApiInterfaces.getcouponmenu(parentname, apiKey)
            ?.enqueue(object : Callback<CouponMenuResponse?> {
                override fun onResponse(
                    call: Call<CouponMenuResponse?>, response: Response<CouponMenuResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            response.body()?.data?.let {
                                getcouponmenu.postValue(response.body())
                            } ?: run {
                                getcouponmenu.postValue(response.body())
                            }


                        }
                    } else {
                        getcouponmenu.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CouponMenuResponse?>, t: Throwable) {
                    getcouponmenu.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getcouponmenuLiveData: LiveData<CouponMenuResponse?>
        get() = getcouponmenu


    fun getCouponsSummary(
        mobile_no: String, parentName: String, apiKey: String
    ) {
        val request = CouponSummaryRequest(
            mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.getCouponsSummary(parentName, apiKey, request)
            ?.enqueue(object : Callback<CampaignResponse?> {
                override fun onResponse(
                    call: Call<CampaignResponse?>, response: Response<CampaignResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            response.body()?.data?.let {
                                getCouponsSummary.postValue(response.body())
                            } ?: run {
                                getCouponsSummary.postValue(response.body())
                            }


                        }
                    } else {
                        getCouponsSummary.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CampaignResponse?>, t: Throwable) {
                    getCouponsSummary.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getCouponsSummaryLiveData: LiveData<CampaignResponse?>
        get() = getCouponsSummary

    fun getCouponsCategorySummary(
        category_id: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        val request = CategorySummaryRequest(
            category_id = category_id, mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.getCouponsCategorySummary(parentName, apiKey, request)
            ?.enqueue(object : Callback<CampaignResponse?> {
                override fun onResponse(
                    call: Call<CampaignResponse?>, response: Response<CampaignResponse?>
                ) {
                    if (response.code() == 200) {
                        getCouponsCategorySummary.postValue(response.body())
                    } else {
                        getCouponsCategorySummary.postValue(null)
                    }
                }

                override fun onFailure(call: Call<CampaignResponse?>, t: Throwable) {
                    getCouponsCategorySummary.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getCouponsCategorySummaryLiveData: LiveData<CampaignResponse?>
        get() = getCouponsCategorySummary


    fun getmycouponsSummary(
        coupon_status: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        val request = MyCouponSummaryRequest(
            coupon_status = coupon_status, mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.getmycoupons(parentName, apiKey, request)
            ?.enqueue(object : Callback<TicketSummaryResponse?> {
                override fun onResponse(
                    call: Call<TicketSummaryResponse?>, response: Response<TicketSummaryResponse?>
                ) {
                    if (response.code() == 200) {
                        getmycoupons.postValue(response.body())
                    } else {
                        getmycoupons.postValue(null)
                    }
                }

                override fun onFailure(call: Call<TicketSummaryResponse?>, t: Throwable) {
                    getmycoupons.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getmycouponsSummaryLiveData: LiveData<TicketSummaryResponse?>
        get() = getmycoupons


    fun getCouponDetails(
        source_link: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        val request = CouponDetailsRequest(
            source_link = source_link, mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.getCouponDetails(parentName, apiKey, request)
            ?.enqueue(object : Callback<ActivateCouponSummaryResponse?> {
                override fun onResponse(
                    call: Call<ActivateCouponSummaryResponse?>,
                    response: Response<ActivateCouponSummaryResponse?>
                ) {
                    if (response.code() == 200) {
                        getCouponDetails.postValue(response.body())
                    } else {
                        getCouponDetails.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ActivateCouponSummaryResponse?>, t: Throwable) {
                    getCouponDetails.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val getCouponDetailsLiveData: LiveData<ActivateCouponSummaryResponse?>
        get() = getCouponDetails


    fun sendactivatecoupon(
        source_link: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        val request = ActivateCouponRequest(
            source_link = source_link, mobile_no = mobile_no
        )
        RestClient.couponApiInterfaces.sendactivatecoupon(parentName, apiKey, request)
            ?.enqueue(object : Callback<ActivateCouponResponse?> {
                override fun onResponse(
                    call: Call<ActivateCouponResponse?>, response: Response<ActivateCouponResponse?>
                ) {
                    if (response.code() == 200) {
                        sendactivatecoupon.postValue(response.body())
                    } else {
                        sendactivatecoupon.postValue(null)
                    }
                }

                override fun onFailure(call: Call<ActivateCouponResponse?>, t: Throwable) {
                    sendactivatecoupon.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendactivatecouponLiveData: LiveData<ActivateCouponResponse?>
        get() = sendactivatecoupon


    fun getstaffquestions(
        isToken: String,
        is_class_teacher: Boolean,
        section_id: String,
        subject_id: String,
        offset: Int
    ) {
        RestClient.apiInterfaces.getstaffquestions(
            isToken, is_class_teacher,
            section_id,
            subject_id,
            offset,
        )?.enqueue(object : Callback<QuestionResponse?> {
            override fun onResponse(
                call: Call<QuestionResponse?>, response: Response<QuestionResponse?>
            ) {
                Log.d(
                    "GetChildAttendanceReportData Response",
                    response.code().toString() + " - " + response.toString()
                )
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val status = response.body()!!.status
                        if (status) {
                            Log.d("GetChildAttendanceReportData", response.body().toString())
                            getstaffquestions.postValue(response.body())
                        } else {
                            Log.d("GetChildAttendanceReportData", response.body().toString())
                            getstaffquestions.postValue(response.body())
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<QuestionResponse?>, t: Throwable
            ) {
                getstaffquestions.postValue(null)
                t.printStackTrace()
            }
        })
    }

    val getstaffquestionsLiveData: LiveData<QuestionResponse?>
        get() = getstaffquestions


    fun sendanswer(
        isToken: String, request: AnswerModelRequest
    ) {
        RestClient.apiInterfaces.sendanswer(isToken, request)
            ?.enqueue(object : Callback<AnswerModelResponse?> {
                override fun onResponse(
                    call: Call<AnswerModelResponse?>, response: Response<AnswerModelResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        sendanswer.postValue(response.body())
                    } else {
                        sendanswer.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<AnswerModelResponse?>, t: Throwable) {
                    sendanswer.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val sendanswerLiveData: LiveData<AnswerModelResponse?>
        get() = sendanswer


    fun isnoticeboarddelete(
        isToken: String, request: JsonObject, activity: Activity
    ) {
        RestClient.apiInterfaces.isnoticeboarddelete(isToken, request)
            ?.enqueue(object : Callback<NoticeBoardDeleteResponse?> {
                override fun onResponse(
                    call: Call<NoticeBoardDeleteResponse?>,
                    response: Response<NoticeBoardDeleteResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isnoticeboarddelete.postValue(response.body())
                            } else {
                                isnoticeboarddelete.postValue(response.body())
                            }
                        }
                    } else {
                        isnoticeboarddelete.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<NoticeBoardDeleteResponse?>, t: Throwable
                ) {
                    isnoticeboarddelete.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isnoticeboarddeleteLiveData: LiveData<NoticeBoardDeleteResponse?>
        get() = isnoticeboarddelete


    fun isEventDelete(
        isToken: String, request: JsonObject, activity: Activity
    ) {
        RestClient.apiInterfaces.isEventDelete(isToken, request)
            ?.enqueue(object : Callback<EventDeleteResponse?> {
                override fun onResponse(
                    call: Call<EventDeleteResponse?>, response: Response<EventDeleteResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isEventDelete.postValue(response.body())
                            } else {
                                isEventDelete.postValue(response.body())
                            }
                        }
                    } else {
                        isEventDelete.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<EventDeleteResponse?>, t: Throwable
                ) {
                    isEventDelete.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isEventDeleteLiveData: LiveData<EventDeleteResponse?>
        get() = isEventDelete


    fun isLsrwDelete(
        isToken: String, request: JsonObject, activity: Activity
    ) {
        RestClient.apiInterfaces.isLsrwDelete(isToken, request)
            ?.enqueue(object : Callback<LsrwDeleteResponse?> {
                override fun onResponse(
                    call: Call<LsrwDeleteResponse?>, response: Response<LsrwDeleteResponse?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                isLsrwDelete.postValue(response.body())
                            } else {
                                isLsrwDelete.postValue(response.body())
                            }
                        }
                    } else {
                        isLsrwDelete.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<LsrwDeleteResponse?>, t: Throwable
                ) {
                    isLsrwDelete.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isLsrwDeleteLiveData: LiveData<LsrwDeleteResponse?>
        get() = isLsrwDelete

    fun getAttachmentReportList(
        isToken: String, activity: Activity
    ) {
        RestClient.apiInterfaces.attachmentReportList(isToken)
            ?.enqueue(object : Callback<AttachmentReportResponse?> {
                override fun onResponse(
                    call: Call<AttachmentReportResponse?>,
                    response: Response<AttachmentReportResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
//                        if (response.body() != null) {
                        val status = response.body()!!.status
                        if (status) {
                            Log.d("GetChildAttendanceReportData", response.body().toString())
                            isAttachmentResponse.postValue(response.body())
                        } else {
                            Log.d("GetChildAttendanceReportData", response.body().toString())
                            isAttachmentResponse.postValue(response.body())
                        }
                        // }
                    }
                }

                override fun onFailure(
                    call: Call<AttachmentReportResponse?>, t: Throwable
                ) {
                    isAttachmentResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAttachmentResponseLiveData: LiveData<AttachmentReportResponse?>
        get() = isAttachmentResponse


    fun getassignmentlist(
        isToken: String, id: String, type: String
    ) {
        RestClient.apiInterfaces.getassignmentlist(isToken, id, type)
            ?.enqueue(object : Callback<SubmissionResponse?> {
                override fun onResponse(
                    call: Call<SubmissionResponse?>, response: Response<SubmissionResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getassignmentlist.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                getassignmentlist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SubmissionResponse?>, t: Throwable
                ) {
                    getassignmentlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getassignmentlistLiveData: LiveData<SubmissionResponse?>
        get() = getassignmentlist

    fun isPtmSlotCreating(
        isToken: String, jsonObject: JsonArray
    ) {
        RestClient.apiInterfaces.isCreateSlots(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isPtmSlotCreate.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isPtmSlotCreate.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StatusMessageModel?>, t: Throwable
                ) {
                    isPtmSlotCreate.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPtmSlotCreateLiveData: LiveData<StatusMessageModel?>
        get() = isPtmSlotCreate


    fun isPtmSlotForStaff(
        isToken: String, isEventDate: String
    ) {
        RestClient.apiInterfaces.isSlotDetailsForStaff(isToken, isEventDate)
            ?.enqueue(object : Callback<SlotResponse?> {
                override fun onResponse(
                    call: Call<SlotResponse?>, response: Response<SlotResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isPtmSlotResponse.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isPtmSlotResponse.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SlotResponse?>, t: Throwable
                ) {
                    isPtmSlotResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPtmSlotResponseLiveData: LiveData<SlotResponse?>
        get() = isPtmSlotResponse

    fun isBookedSlot(
        isToken: String, isEventDate: String
    ) {
        RestClient.apiInterfaces.isBookedSlots(isToken, isEventDate)
            ?.enqueue(object : Callback<BookedSlotResponse?> {
                override fun onResponse(
                    call: Call<BookedSlotResponse?>, response: Response<BookedSlotResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isBookedSlotResponse.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isBookedSlotResponse.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<BookedSlotResponse?>, t: Throwable
                ) {
                    isBookedSlotResponse.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isBookedSlotResponseLiveData: LiveData<BookedSlotResponse?>
        get() = isBookedSlotResponse

    fun isSlotCancelReOpen(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isSlotCancelAndReOpen(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>, response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isPtmSlotCancelReOpen.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isPtmSlotCancelReOpen.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StatusMessageModel?>, t: Throwable
                ) {
                    isPtmSlotCancelReOpen.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isPtmSlotCancelReOpenLiveData: LiveData<StatusMessageModel?>
        get() = isPtmSlotCancelReOpen

    fun isSlotCancelAndClose(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isSlotCancelAndClose(isToken, jsonObject)
            ?.enqueue(object : Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: Call<StatusMessageModel?>,
                    response: Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        "${response.code()} - $response"
                    )

                    if (response.isSuccessful && response.body() != null) {
                        isPtmSlotCancelClose.postValue(response.body())
                    } else {
                        val errorMsg = try {
                            response.errorBody()?.string()
                        } catch (e: Exception) {
                            null
                        }

                        val parsedMessage = if (!errorMsg.isNullOrEmpty()) {
                            try {
                                val json = JSONObject(errorMsg)
                                json.optString("message", "Unknown server error")
                            } catch (_: Exception) {
                                errorMsg
                            }
                        } else {
                            "Server error ${response.code()}"
                        }
                        isPtmSlotCancelClose.postValue(
                            StatusMessageModel(false, parsedMessage, emptyList())
                        )
                    }
                }

                override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
                    isPtmSlotCancelClose.postValue(
                        StatusMessageModel(false, t.message ?: "Network failure", emptyList())
                    )
                    t.printStackTrace()
                }
            })
    }

    val isPtmSlotCancelCloseLiveData: LiveData<StatusMessageModel?>
        get() = isPtmSlotCancelClose


    fun isDatewiseBookedSlots(
        isToken: String, iseventDate: String
    ) {
        RestClient.apiInterfaces.isDatewiseBookedSlots(isToken, iseventDate)
            ?.enqueue(object : Callback<SlotBookingResponse?> {
                override fun onResponse(
                    call: Call<SlotBookingResponse?>, response: Response<SlotBookingResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isDateWiseSlot.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isDateWiseSlot.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SlotBookingResponse?>, t: Throwable
                ) {
                    isDateWiseSlot.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isDateWiseSlotLiveData: LiveData<SlotBookingResponse?>
        get() = isDateWiseSlot


    fun isSlotValidationForStaff(
        isToken: String, jsonObject: JsonArray
    ) {
        RestClient.apiInterfaces.isSlotValidationForStaff(isToken, jsonObject)
            ?.enqueue(object : Callback<SlotValidationResponse?> {
                override fun onResponse(
                    call: Call<SlotValidationResponse?>, response: Response<SlotValidationResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSlotValidation.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                isSlotValidation.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SlotValidationResponse?>, t: Throwable
                ) {
                    isSlotValidation.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSlotValidationLiveData: LiveData<SlotValidationResponse?>
        get() = isSlotValidation


    fun islsrwskillsreport(
        isToken: String
    ) {
        RestClient.apiInterfaces.islsrwskillsreport(isToken)
            ?.enqueue(object : Callback<lsrwskillresponse?> {
                override fun onResponse(
                    call: Call<lsrwskillresponse?>,
                    response: Response<lsrwskillresponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwskillsreport.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwskillsreport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<lsrwskillresponse?>,
                    t: Throwable
                ) {
                    islsrwskillsreport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val islsrwskillsreportLiveData: LiveData<lsrwskillresponse?>
        get() = islsrwskillsreport


    fun islsrwStudentlist(
        isToken: String,
        id: String
    ) {
        RestClient.apiInterfaces.islsrwStudentlist(isToken, id)
            ?.enqueue(object : Callback<StudentSubmissionLsrwResponse?> {
                override fun onResponse(
                    call: Call<StudentSubmissionLsrwResponse?>,
                    response: Response<StudentSubmissionLsrwResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwStudentlist.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwStudentlist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<StudentSubmissionLsrwResponse?>,
                    t: Throwable
                ) {
                    islsrwStudentlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val islsrwStudentlistLiveData: LiveData<StudentSubmissionLsrwResponse?>
        get() = islsrwStudentlist


    fun islsrwstats(
        isToken: String,
        month_id: Int,

        ) {
        RestClient.apiInterfaces.islsrwstats(isToken, month_id)
            ?.enqueue(object : Callback<AvgSkillResponse?> {
                override fun onResponse(
                    call: Call<AvgSkillResponse?>,
                    response: Response<AvgSkillResponse?>
                ) {
                    Log.d(
                        "GetChildAttendanceReportData Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwstats.postValue(response.body())
                            } else {
                                Log.d("GetChildAttendanceReportData", response.body().toString())
                                islsrwstats.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AvgSkillResponse?>,
                    t: Throwable
                ) {
                    islsrwstats.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val islsrwstatsLiveData: LiveData<AvgSkillResponse?>
        get() = islsrwstats


    fun islsrwremarkupdate(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {

        RestClient.apiInterfaces.islsrwremarkupdate(isToken, jsonObject)
            ?.enqueue(object : Callback<LsrwremarkUpdateModel?> {
                override fun onResponse(
                    call: Call<LsrwremarkUpdateModel?>, response: Response<LsrwremarkUpdateModel?>
                ) {
                    Log.d(
                        "isGetCountryList", response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                islsrwremarkupdate.postValue(response.body())
                            } else {
                                islsrwremarkupdate.postValue(response.body())
                            }
                        }
                    } else {
                        islsrwremarkupdate.postValue(null)
                    }
                }

                override fun onFailure(call: Call<LsrwremarkUpdateModel?>, t: Throwable) {
                    islsrwremarkupdate.postValue(null)
                    Log.d("t.printStackTrace()", t.printStackTrace().toString())
                }
            })
    }

    val islsrwremarkupdateLiveData: LiveData<LsrwremarkUpdateModel?>
        get() = islsrwremarkupdate


    fun isSubmitQuiz(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isCreateQuiz(isToken, jsonObject)
            ?.enqueue(object : Callback<CreateQuizResponse?> {
                override fun onResponse(
                    call: Call<CreateQuizResponse?>, response: Response<CreateQuizResponse?>
                ) {
                    Log.d(
                        "isSubmitQuiz Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isSubmitQuizData", response.body().toString())
                                isCreateQuiz.postValue(response.body())
                            } else {
                                Log.d("isSubmitQuizData", response.body().toString())
                                isCreateQuiz.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<CreateQuizResponse?>, t: Throwable
                ) {
                    isCreateQuiz.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isCreateQuizLiveData: LiveData<CreateQuizResponse?>
        get() = isCreateQuiz


    fun isGetQuizExamReport(
        isToken: String, type: String
    ) {
        RestClient.apiInterfaces.isGetExamQuizReport(isToken, type)
            ?.enqueue(object : Callback<GetQuizExamReport?> {
                override fun onResponse(
                    call: Call<GetQuizExamReport?>, response: Response<GetQuizExamReport?>
                ) {
                    Log.d(
                        "GetQuizExamReport Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetQuizExamReportData", response.body().toString())
                                isGetQuizExamReport.postValue(response.body())
                            } else {
                                Log.d("GetQuizExamReportData", response.body().toString())
                                isGetQuizExamReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetQuizExamReport?>, t: Throwable
                ) {
                    isGetQuizExamReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetQuizExamReportLiveData: LiveData<GetQuizExamReport?>
        get() = isGetQuizExamReport


    fun isGetCheckLevel(
        isToken: String, class_id: String, subject_id: String, section_id: String
    ) {
        RestClient.apiInterfaces.isGetCheckLevel(isToken, class_id, subject_id, section_id)
            ?.enqueue(object : Callback<GetCheckLevel?> {
                override fun onResponse(
                    call: Call<GetCheckLevel?>, response: Response<GetCheckLevel?>
                ) {
                    Log.d(
                        "GetCheckLevelResponse",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetCheckLevelData", response.body().toString())
                                isGetCheckLevel.postValue(response.body())
                            } else {
                                Log.d("GetCheckLevelData", response.body().toString())
                                isGetCheckLevel.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetCheckLevel?>, t: Throwable
                ) {
                    isGetCheckLevel.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetCheckLevelLiveData: LiveData<GetCheckLevel?>
        get() = isGetCheckLevel


    fun islsrwSkillCreate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        RestClient.changeApiBaseUrl(SharedPreference.getBaseUrl(activity).toString())
        RestClient.apiInterfaces.islsrwSkillCreate(isToken, jsonObject)
            ?.enqueue(object : Callback<LsrwSkillSendResponse?> {
                override fun onResponse(
                    call: Call<LsrwSkillSendResponse?>, response: Response<LsrwSkillSendResponse?>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        islsrwSkillCreate.postValue(response.body())
                    } else {
                        islsrwSkillCreate.postValue(response.body())
                    }

                    Log.d("isGetCountryList", "${response.code()} - ${response}")
                }

                override fun onFailure(call: Call<LsrwSkillSendResponse?>, t: Throwable) {
                    islsrwSkillCreate.postValue(null)
                    t.printStackTrace()
                }
            })
    }


    val islsrwSkillCreateLiveData: LiveData<LsrwSkillSendResponse?>
        get() = islsrwSkillCreate


    fun isGetQuizQuestionReport(
        isToken: String, class_id: String,
    ) {
        RestClient.apiInterfaces.isGetQuizQuestionReport(isToken, class_id)
            ?.enqueue(object : Callback<GetQuizQuestionReport?> {
                override fun onResponse(
                    call: Call<GetQuizQuestionReport?>, response: Response<GetQuizQuestionReport?>
                ) {
                    Log.d(
                        "GetQuizQuestionReport",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetQuizQuestionReportData", response.body().toString())
                                isGetQuizQuestionReport.postValue(response.body())
                            } else {
                                Log.d("GetQuizQuestionReportData", response.body().toString())
                                isGetQuizQuestionReport.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetQuizQuestionReport?>, t: Throwable
                ) {
                    isGetQuizQuestionReport.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetQuizQuestionReportLiveData: LiveData<GetQuizQuestionReport?>
        get() = isGetQuizQuestionReport


    fun isGetQuizSubmissionList(
        isToken: String, id: String,
    ) {
        RestClient.apiInterfaces.isGetQuizSubmissionList(isToken, id)
            ?.enqueue(object : Callback<GetQuizSubmissionList?> {
                override fun onResponse(
                    call: Call<GetQuizSubmissionList?>, response: Response<GetQuizSubmissionList?>
                ) {
                    Log.d(
                        "GetQuizQuestionReport",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetQuizSubmissionListData", response.body().toString())
                                isGetQuizSubmissionList.postValue(response.body())
                            } else {
                                Log.d("GetQuizSubmissionListData", response.body().toString())
                                isGetQuizSubmissionList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetQuizSubmissionList?>, t: Throwable
                ) {
                    isGetQuizSubmissionList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetQuizSubmissionListLiveData: LiveData<GetQuizSubmissionList?>
        get() = isGetQuizSubmissionList


    fun isGetPickFromQBank(
        isToken: String, subject_id: String
    ) {
        RestClient.apiInterfaces.isGetPickFromQBank(isToken, subject_id)
            ?.enqueue(object : Callback<GetPickFromQBank?> {
                override fun onResponse(
                    call: Call<GetPickFromQBank?>, response: Response<GetPickFromQBank?>
                ) {
                    Log.d(
                        "isGetPickFromQBank",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isGetPickFromQBankData", response.body().toString())
                                isGetPickFromQBank.postValue(response.body())
                            } else {
                                Log.d("isGetPickFromQBankData", response.body().toString())
                                isGetPickFromQBank.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetPickFromQBank?>, t: Throwable
                ) {
                    isGetPickFromQBank.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetPickFromQBankLiveData: LiveData<GetPickFromQBank?>
        get() = isGetPickFromQBank


    fun isQuizAddQuestion(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isAddQuestion(isToken, jsonObject)
            ?.enqueue(object : Callback<AddQuestionResponse?> {
                override fun onResponse(
                    call: Call<AddQuestionResponse?>, response: Response<AddQuestionResponse?>
                ) {
                    Log.d(
                        "isAddQuestion Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isAddQuestionData", response.body().toString())
                                isAddQuestion.postValue(response.body())
                            } else {
                                Log.d("isAddQuestionData", response.body().toString())
                                isAddQuestion.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AddQuestionResponse?>, t: Throwable
                ) {
                    isAddQuestion.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isAddQuestionLiveData: LiveData<AddQuestionResponse?>
        get() = isAddQuestion


    fun isGetMessageFromStaff(
        isToken: String
    ) {
        RestClient.apiInterfaces.isGetMessageFromStaff(isToken)
            ?.enqueue(object : Callback<GetMessagesStaff?> {
                override fun onResponse(
                    call: Call<GetMessagesStaff?>, response: Response<GetMessagesStaff?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                isGetMessageFromStaff.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                isGetMessageFromStaff.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetMessagesStaff?>, t: Throwable
                ) {
                    isGetMessageFromStaff.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetMessageStaffLiveData: LiveData<GetMessagesStaff?>
        get() = isGetMessageFromStaff


    fun isGetMessageFromStaffArchive(
        isToken: String
    ) {
        RestClient.apiInterfaces.isGetMessageFromStaffArchive(isToken)
            ?.enqueue(object : Callback<GetMessagesStaff?> {
                override fun onResponse(
                    call: Call<GetMessagesStaff?>, response: Response<GetMessagesStaff?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                isGetMessageFromStaffArchive.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                isGetMessageFromStaffArchive.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetMessagesStaff?>, t: Throwable
                ) {
                    isGetMessageFromStaffArchive.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetMessageStaffArchiveLiveData: LiveData<GetMessagesStaff?>
        get() = isGetMessageFromStaffArchive


    fun isSchoolprofilelist(
        isToken: String
    ) {
        RestClient.apiInterfaces.isSchoolprofilelist(isToken)
            ?.enqueue(object : Callback<ProfileListResponse?> {
                override fun onResponse(
                    call: Call<ProfileListResponse?>, response: Response<ProfileListResponse?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                isSchoolprofilelist.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                isSchoolprofilelist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<ProfileListResponse?>, t: Throwable
                ) {
                    isSchoolprofilelist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isSchoolprofilelistLiveData: LiveData<ProfileListResponse?>
        get() = isSchoolprofilelist


    fun getchildhomeworkstandard(
        isToken: String,
        id: Int
    ) {
        RestClient.apiInterfaces.getchildhomeworkstandard(isToken, id)
            ?.enqueue(object : Callback<ChildStandardResponse?> {
                override fun onResponse(
                    call: Call<ChildStandardResponse?>, response: Response<ChildStandardResponse?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getchildhomeworkstandard.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getchildhomeworkstandard.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<ChildStandardResponse?>, t: Throwable
                ) {
                    getchildhomeworkstandard.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getchildhomeworkstandardLiveData: LiveData<ChildStandardResponse?>
        get() = getchildhomeworkstandard


    fun getassignmentchildhomework(
        isToken: String,
        id: Int,
        target_type: Int
    ) {
        RestClient.apiInterfaces.getassignmentchildhomework(isToken, id, target_type)
            ?.enqueue(object : Callback<AssignmentTargetDetailsResponse?> {
                override fun onResponse(
                    call: Call<AssignmentTargetDetailsResponse?>,
                    response: Response<AssignmentTargetDetailsResponse?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getassignmentchildhomework.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getassignmentchildhomework.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AssignmentTargetDetailsResponse?>, t: Throwable
                ) {
                    getassignmentchildhomework.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getassignmentchildhomeworkLiveData: LiveData<AssignmentTargetDetailsResponse?>
        get() = getassignmentchildhomework


    fun getattachmentchildhomework(
        isToken: String,
        id: Int,
        target_type: Int
    ) {
        RestClient.apiInterfaces.getattachmentchildhomework(isToken, id, target_type)
            ?.enqueue(object : Callback<AttachmentTargetDetailResponse?> {
                override fun onResponse(
                    call: Call<AttachmentTargetDetailResponse?>,
                    response: Response<AttachmentTargetDetailResponse?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getattachmentchildhomework.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getattachmentchildhomework.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AttachmentTargetDetailResponse?>, t: Throwable
                ) {
                    getattachmentchildhomework.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getattachmentchildhomeworkLiveData: LiveData<AttachmentTargetDetailResponse?>
        get() = getattachmentchildhomework


    fun getdashboardnewupdates(
        isToken: String,
        role_type: String
    ) {
        RestClient.apiInterfaces.getdashboardnewupdates(isToken, role_type)
            ?.enqueue(object : Callback<WhatsNewUpdateResponse?> {
                override fun onResponse(
                    call: Call<WhatsNewUpdateResponse?>, response: Response<WhatsNewUpdateResponse?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getdashboardnewupdates.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getdashboardnewupdates.postValue(response.body())
                            }
                        }
                    } else {
                        getdashboardnewupdates.postValue(null)
                    }
                }

                override fun onFailure(
                    call: Call<WhatsNewUpdateResponse?>, t: Throwable
                ) {
                    getdashboardnewupdates.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getdashboardnewupdatesLiveData: LiveData<WhatsNewUpdateResponse?>
        get() = getdashboardnewupdates


    fun isGetAttendanceStudentList(
        isToken: String,
        class_id: String,
        section_id: String,
        date: String,
        attendance_type: String,
    ) {
        RestClient.apiInterfaces.getAttendanceStudentList(
            isToken,
            class_id,
            section_id,
            date,
            attendance_type
        )
            ?.enqueue(object : Callback<GetAttendanceStudentList?> {
                override fun onResponse(
                    call: Call<GetAttendanceStudentList?>,
                    response: Response<GetAttendanceStudentList?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getattendanceStudentList.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getattendanceStudentList.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetAttendanceStudentList?>, t: Throwable
                ) {
                    getattendanceStudentList.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getAttendanceStudentListLiveData: LiveData<GetAttendanceStudentList?>
        get() = getattendanceStudentList


    fun isblockstudent(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isblockstudent(isToken, jsonObject)
            ?.enqueue(object : Callback<BlockApiResponse?> {
                override fun onResponse(
                    call: Call<BlockApiResponse?>, response: Response<BlockApiResponse?>
                ) {
                    Log.d(
                        "isAddQuestion Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isAddQuestionData", response.body().toString())
                                isblockstudent.postValue(response.body())
                            } else {
                                Log.d("isAddQuestionData", response.body().toString())
                                isblockstudent.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<BlockApiResponse?>, t: Throwable
                ) {
                    isblockstudent.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isblockstudentLiveData: LiveData<BlockApiResponse?>
        get() = isblockstudent


    fun isblockstudentlist(
        isToken: String
    ) {
        RestClient.apiInterfaces.isblockstudentlist(isToken)
            ?.enqueue(object : Callback<BlockedStudentsResponse?> {
                override fun onResponse(
                    call: Call<BlockedStudentsResponse?>,
                    response: Response<BlockedStudentsResponse?>
                ) {
                    Log.d(
                        "isAddQuestion Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isAddQuestionData", response.body().toString())
                                isblockstudentlist.postValue(response.body())
                            } else {
                                Log.d("isAddQuestionData", response.body().toString())
                                isblockstudentlist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<BlockedStudentsResponse?>, t: Throwable
                ) {
                    isblockstudentlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isblockstudentlistLiveData: LiveData<BlockedStudentsResponse?>
        get() = isblockstudentlist


    fun isfrequentlyasked(
        isToken: String
    ) {
        RestClient.apiInterfaces.isfrequentlyasked(isToken)
            ?.enqueue(object : Callback<FrequentlyModelResponse?> {
                override fun onResponse(
                    call: Call<FrequentlyModelResponse?>,
                    response: Response<FrequentlyModelResponse?>
                ) {
                    Log.d(
                        "isAddQuestion Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isAddQuestionData", response.body().toString())
                                isfrequentlyasked.postValue(response.body())
                            } else {
                                Log.d("isAddQuestionData", response.body().toString())
                                isfrequentlyasked.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<FrequentlyModelResponse?>, t: Throwable
                ) {
                    isfrequentlyasked.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isfrequentlyaskedLiveData: LiveData<FrequentlyModelResponse?>
        get() = isfrequentlyasked


    fun isdeletenotification(
        isToken: String,
        jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isdeletenotification(isToken, jsonObject)
            ?.enqueue(object : Callback<DeleteNotificationResponse?> {
                override fun onResponse(
                    call: Call<DeleteNotificationResponse?>,
                    response: Response<DeleteNotificationResponse?>
                ) {
                    Log.d(
                        "isAddQuestion Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isAddQuestionData", response.body().toString())
                                isdeletenotification.postValue(response.body())
                            } else {
                                Log.d("isAddQuestionData", response.body().toString())
                                isdeletenotification.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DeleteNotificationResponse?>, t: Throwable
                ) {
                    isdeletenotification.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isdeletenotificationLiveData: LiveData<DeleteNotificationResponse?>
        get() = isdeletenotification


    fun isgetfeature() {
        RestClient.apiInterfaces.isgetfeature()
            ?.enqueue(object : Callback<GetFeature?> {
                override fun onResponse(
                    call: Call<GetFeature?>, response: Response<GetFeature?>
                ) {
                    Log.d(
                        "GetFeature Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetFeature", response.body().toString())
                                isgetfeature.postValue(response.body())
                            } else {
                                Log.d("GetFeature", response.body().toString())
                                isgetfeature.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<GetFeature?>, t: Throwable
                ) {
                    isgetfeature.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getnewfeatureLiveData: LiveData<GetFeature?>
        get() = isgetfeature


    fun getreviewlist(
        isToken: String,
        mobile_number: String
    ) {
        RestClient.apiInterfaces.getreviewlist(isToken, mobile_number)
            ?.enqueue(object : Callback<ReviewResponse?> {
                override fun onResponse(
                    call: Call<ReviewResponse?>, response: Response<ReviewResponse?>
                ) {
                    Log.d(
                        "GetMessagesStaff Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getreviewlist.postValue(response.body())
                            } else {
                                Log.d("GetMessagesStaffData", response.body().toString())
                                getreviewlist.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<ReviewResponse?>, t: Throwable
                ) {
                    getreviewlist.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getreviewlistLiveData: LiveData<ReviewResponse?>
        get() = getreviewlist


    fun reviewpost(
        isToken: String,
        jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.reviewpost(isToken, jsonObject)
            ?.enqueue(object : Callback<SubmitReviewResponse?> {
                override fun onResponse(
                    call: Call<SubmitReviewResponse?>, response: Response<SubmitReviewResponse?>
                ) {
                    Log.d(
                        "isAddQuestion Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isAddQuestionData", response.body().toString())
                                reviewpost.postValue(response.body())
                            } else {
                                Log.d("isAddQuestionData", response.body().toString())
                                reviewpost.postValue(response.body())
                            }
                        }
                        else{
                            reviewpost.postValue(null)
                        }
                    }
                    else{
                        reviewpost.postValue(null)

                    }
                }

                override fun onFailure(
                    call: Call<SubmitReviewResponse?>, t: Throwable
                ) {
                    reviewpost.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val reviewpostLiveData: LiveData<SubmitReviewResponse?>
        get() = reviewpost


    fun isGetStaffWiseExam(
        isToken: String,
        section_id: String,
    ) {
        RestClient.apiInterfaces.getStaffWiseExam(isToken, section_id)
            ?.enqueue(object : Callback<getStaffWisExam?> {
                override fun onResponse(
                    call: Call<getStaffWisExam?>,
                    response: Response<getStaffWisExam?>
                ) {
                    Log.d(
                        "getStaffWisExam Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("getStaffWisExam", response.body().toString())
                                isgetStaffWiseExam.postValue(response.body())
                            } else {
                                Log.d("getStaffWisExam", response.body().toString())
                                isgetStaffWiseExam.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<getStaffWisExam?>, t: Throwable
                ) {
                    isgetStaffWiseExam.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getStaffWiseExamLiveData: LiveData<getStaffWisExam?>
        get() = isgetStaffWiseExam


    fun isGetSubjectWiseActivities(
        isToken: String,
        exam_id: String,
    ) {
        RestClient.apiInterfaces.getSubjectWiseActivities(isToken, exam_id)
            ?.enqueue(object : Callback<getSubjectWiseACtivities?> {
                override fun onResponse(
                    call: Call<getSubjectWiseACtivities?>,
                    response: Response<getSubjectWiseACtivities?>
                ) {
                    Log.d(
                        "isgetSubjectWiseActivities Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isgetSubjectWiseActivities", response.body().toString())
                                isgetSubjectWiseActivities.postValue(response.body())
                            } else {
                                Log.d("isgetSubjectWiseActivities", response.body().toString())
                                isgetSubjectWiseActivities.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<getSubjectWiseACtivities?>, t: Throwable
                ) {
                    isgetSubjectWiseActivities.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val getSubjectWiseActivitiesLiveData: LiveData<getSubjectWiseACtivities?>
        get() = isgetSubjectWiseActivities


    fun isGetMarkDetails(
        isToken: String,
       jsonObject: JsonObject,
    ) {
        RestClient.apiInterfaces.getMarkDetails(isToken, jsonObject)
            ?.enqueue(object : Callback<MarkResponse?> {
                override fun onResponse(
                    call: Call<MarkResponse?>,
                    response: Response<MarkResponse?>
                ) {
                    Log.d(
                        "isgetSubjectWiseActivities Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("isgetSubjectWiseActivities", response.body().toString())
                                isGetMarkDetails.postValue(response.body())
                            } else {
                                Log.d("isgetSubjectWiseActivities", response.body().toString())
                                isGetMarkDetails.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<MarkResponse?>, t: Throwable
                ) {
                    isGetMarkDetails.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isGetMarkDetailsLiveData: LiveData<MarkResponse?>
        get() = isGetMarkDetails

    fun isDeleteQuiz(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isDeleteQuiz(isToken, jsonObject)
            ?.enqueue(object : Callback<DeleteQuizResponse?> {
                override fun onResponse(
                    call: Call<DeleteQuizResponse?>, response: Response<DeleteQuizResponse?>
                ) {
                    Log.d(
                        "DeleteQuiz Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("DeleteQuizResponse", response.body().toString())
                                isDeleteQuiz.postValue(response.body())
                            } else {
                                Log.d("DeleteQuizResponse", response.body().toString())
                                isDeleteQuiz.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DeleteQuizResponse?>, t: Throwable
                ) {
                    isDeleteQuiz.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isDeleteQuizLiveData: LiveData<DeleteQuizResponse?>
        get() = isDeleteQuiz



    fun isEditQuiz(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isEditQuiz(isToken, jsonObject)
            ?.enqueue(object : Callback<EditQuizResponse?> {
                override fun onResponse(
                    call: Call<EditQuizResponse?>, response: Response<EditQuizResponse?>
                ) {
                    Log.d(
                        "DeleteQuiz Response",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("DeleteQuizResponse", response.body().toString())
                                isEditQuiz.postValue(response.body())
                            } else {
                                Log.d("DeleteQuizResponse", response.body().toString())
                                isEditQuiz.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<EditQuizResponse?>, t: Throwable
                ) {
                    isEditQuiz.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isEditQuizLiveData: LiveData<EditQuizResponse?>
        get() = isEditQuiz







    fun isDeleteQuizQuestion(
        isToken: String, jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.isDeleteQuizQuestion(isToken, jsonObject)
            ?.enqueue(object : Callback<DeleteQuizQuestionResponse?> {
                override fun onResponse(
                    call: Call<DeleteQuizQuestionResponse?>, response: Response<DeleteQuizQuestionResponse?>
                ) {
                    Log.d(
                        "DeleteQuizQuestionResponse ",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("DeleteQuizQuestionResponse", response.body().toString())
                                isgetDeleteQuizQuestion.postValue(response.body())
                            } else {
                                Log.d("DeleteQuizQuestionResponse", response.body().toString())
                                isgetDeleteQuizQuestion.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DeleteQuizQuestionResponse?>, t: Throwable
                ) {
                    isgetDeleteQuizQuestion.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val isDeleteQuizQuestionLiveData: LiveData<DeleteQuizQuestionResponse?>
        get() = isgetDeleteQuizQuestion




    fun uploadmarks(part: MultipartBody.Part, activity: Activity) {
        RestClient.apiInterfaces.uploadmarks( part)
            ?.enqueue(object : Callback<UploadMarkResponse?> {
                override fun onResponse(
                    call: Call<UploadMarkResponse?>, response: Response<UploadMarkResponse?>
                ) {
                    Log.d(
                        "UploadMarkResponse ",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
//                        if (response.body() != null) {
//                            val status = response.body()!!.status
//                            if (status) {
//                                Log.d("UploadMarkResponse", response.body().toString())
                                uploadmarks.postValue(response.body())
//                            } else {
//                                Log.d("UploadMarkResponse", response.body().toString())
//                                uploadmarks.postValue(response.body())
//                            }
//                        }
                    }
                    else {
                        Constant.hideLoading(activity)
                        uploadmarks.postValue(null)
                        val errorBodyString = response.errorBody()?.string()
                        val gson = Gson()
                        val errorModel = gson.fromJson(errorBodyString, ErrorResponse::class.java)
                        Toast.makeText(activity, errorModel.message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(
                    call: Call<UploadMarkResponse?>, t: Throwable
                ) {
                    uploadmarks.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val uploadmarksLiveData: LiveData<UploadMarkResponse?>
        get() = uploadmarks




    fun savemarks(isToken:String,
         jsonObject: JsonObject
    ) {
        RestClient.apiInterfaces.savemarks(isToken, jsonObject)
            ?.enqueue(object : Callback<SaveMarksModel?> {
                override fun onResponse(
                    call: Call<SaveMarksModel?>, response: Response<SaveMarksModel?>
                ) {
                    Log.d(
                        "DeleteQuizQuestionResponse ",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                            if (status) {
                                Log.d("DeleteQuizQuestionResponse", response.body().toString())
                                savemarks.postValue(response.body())
                            } else {
                                Log.d("DeleteQuizQuestionResponse", response.body().toString())
                                savemarks.postValue(response.body())
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<SaveMarksModel?>, t: Throwable
                ) {
                    savemarks.postValue(null)
                    t.printStackTrace()
                }
            })
    }

    val savemarksLiveData: LiveData<SaveMarksModel?>
        get() = savemarks




}