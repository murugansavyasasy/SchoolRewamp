package com.vs.schoolmessenger.Repository

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ProfileUpdateResponse
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Model.FrequentlyModelResponse
import com.vs.schoolmessenger.Dashboard.Settings.Notification.DeleteNotificationResponse
import com.vs.schoolmessenger.Dashboard.Settings.Notification.NotificationResponse
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.ReviewResponse
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.SubmitReviewResponse
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model.WhatsNewUpdateResponse
import com.vs.schoolmessenger.Parent.Assignment.Model.AssignmentSubmitResponse
import com.vs.schoolmessenger.Parent.Assignment.Model.MySubmissionDeleteResponse
import com.vs.schoolmessenger.Parent.Assignment.Model.MySubmissionEditResponse
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentResponse
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.MySubmittedAssignmentsResponse
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport.ChildAttendanceResponse
import com.vs.schoolmessenger.Parent.Attendance.Model.getStudentStats
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.getBusList
import com.vs.schoolmessenger.Parent.BusTracking.Model.LatestLocation.GetLatestGeoLocationResponse
import com.vs.schoolmessenger.Parent.BusTracking.Model.LiveBus.getLiveBus
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesListResponse
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificatesTypesResponse
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestMarkResponse
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestResponse
import com.vs.schoolmessenger.Parent.Communication.StatusArchiveResponse
import com.vs.schoolmessenger.Parent.Communication.VoiceDataResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.CouponMenuResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.PauketPoints.PauketPointsResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.PauketPoints.SpentPointsModel
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCoupon.ActivateCouponResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCouponSummary.ActivateCouponSummaryResponse
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummaryResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventResponse
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel.ExamMarksResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableResponse
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamTimeTableResponse
import com.vs.schoolmessenger.Parent.ExamMarks.ProgressCardResponse
import com.vs.schoolmessenger.Parent.FeeDetails.Model.FeeInvoiceResponse
import com.vs.schoolmessenger.Parent.FeeDetails.Model.InvoiceDetailsResponse
import com.vs.schoolmessenger.Parent.FeeDetails.Model.OnlinePaymentResponse
import com.vs.schoolmessenger.Parent.FeeDetails.Model.PaymentStatusResponse
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkData
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.getParentHostelDashboard
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDetails.getParentHostelDetails
import com.vs.schoolmessenger.Parent.Hostel.Model.applyOutpassResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.InteractionWithStaffResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.QuestionModelResponse
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request.QuestionModelRequest
import com.vs.schoolmessenger.Parent.LSRW.Model.LSRWSkillSubmitResponse
import com.vs.schoolmessenger.Parent.LSRW.Model.LsrwSkillResponse
import com.vs.schoolmessenger.Parent.LSRW.MySubmissionModel.ActivityResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingHistoryResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotCountResponse
import com.vs.schoolmessenger.Parent.PTM.DataClass.SubjectResponse
import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuizQuestions
import com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission.GetMySubmission
import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizExamList.GetQuizExamList
import com.vs.schoolmessenger.Parent.QuizExam.Model.SubmitQuiz.SubmitQuizResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestApplyResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.GetLeaveCategoriesData
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDelete
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestDeleteResponse
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveRequestUpdate
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel.LeaveUpdateResponse
import com.vs.schoolmessenger.Parent.Timetable.TimeTableResponse
import com.vs.schoolmessenger.Repository.SchoolServices.SchoolServices
import com.vs.schoolmessenger.Repository.SchoolServices.SchoolServicesTwo
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentList
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.SendAbsenteeSMSResponse
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportDataResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudentsResponse
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesResponse
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.getStaffLeaveRequestHistory
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestStatusUpdate.StaffLeaveRequestStatusUpdate
import com.vs.schoolmessenger.School.Assignment.AssignmentTargetDetails.AssignmentTargetDetailsResponse
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentResponse
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionResponse
import com.vs.schoolmessenger.School.Attachment.AttachmentTargetDetails.AttachmentTargetDetailResponse
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportResponse
import com.vs.schoolmessenger.School.AttendanceReportFromStaff.AttendanceReportFromStaffDataClass
import com.vs.schoolmessenger.School.ClassTest.Report.DeleteClassTestExamModel.DeleteClassTestResponse
import com.vs.schoolmessenger.School.ClassTest.Report.ExamTestDeleteModel.ExamTestDeleteResponse
import com.vs.schoolmessenger.School.ClassTest.Report.Model.ExamlistModelResponse
import com.vs.schoolmessenger.School.ClassTest.Review.Model.CreateClassTestResponse
import com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass.Subjectlistresponse
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.ClassEntryMarkResponse
import com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model.UploadMarksClassEntryResponse
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
import com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel.AnalysisSetsResponse
import com.vs.schoolmessenger.School.ExamReview.ApiResponseModel.StudentAnalysisResponse
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeePendingReportResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportApiResponse
import com.vs.schoolmessenger.School.Homework.HomeWorkSendResponse
import com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListModel.GetHomeWorkSubmissionList
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getSchoolHostelAttendanceReport
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelAttendanceSessionType.getHostelAttendanceSession
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.getHostelDashboard
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.getHostelList
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.getSchoolHostelOutpassRequest
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassUpdateStatus.schoolHostelOutpassUpdateStatus
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.getHostelStudentRoomAttendance
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.RoomMarkAttendance.hostelMarkAttendanceRespone
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerModelRequest
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockApiResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.AnswerModelResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.BlockedStudentsResponse
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentResponse
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
import com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffApplyLeaveRequest.StaffLeaveRequestApplyRespone
import com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffDeleteLeaveRequest.StaffLeaveRequestDeleteResponse
import com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffLeaveListCatorgies.GetStaffLeaveCategoriesData
import com.vs.schoolmessenger.School.StaffLeaveRequest.Model.StaffUpdateLeaveRequest.StaffLeaveUpdateRespone
import com.vs.schoolmessenger.School.StudentReport.GetStudentReportData
import com.vs.schoolmessenger.Utils.SharedPreference
import okhttp3.MultipartBody
import okhttp3.RequestBody

class App(application: Application) : AndroidViewModel(application) {

    var apiSchoolRepositories: SchoolServices = SchoolServices()
    var apiSchoolRepositoriesTwo: SchoolServicesTwo = SchoolServicesTwo()

    var apiParentRepositories: ParentServices = ParentServices()


    var isDashBoardData: LiveData<DashboardResponse?>? = null
        private set

    var isDashBoardCountData: LiveData<DashboardCountResponse?>? = null
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

    var isGetEventCategory: LiveData<EventCategoryResponse?>? = null
        private set


    var isAssignmentDelete: LiveData<LPDeleteResponse?>? = null
        private set

    var isEditHomeWork: LiveData<StatusMessageModel?>? = null
        private set

    var isEditEvent: LiveData<StatusMessageModel?>? = null
        private set

    var isEditAttachment: LiveData<StatusMessageModel?>? = null
        private set


    var isEditNoticeBoard: LiveData<StatusMessageModel?>? = null
        private set


    var isDeleteHomeWork: LiveData<StatusMessageModel?>? = null
        private set


    var isDeleteAttachment: LiveData<StatusMessageModel?>? = null
        private set

    var isGetDailyCollectionReport: LiveData<DailyCollectionReportResponse?>? = null
        private set

    var isGetSchoolStrengthReport: LiveData<SchoolStrengthResponse?>? = null
        private set


    var isNoticeBoardReport: LiveData<NoticeBoardStaffResponse?>? = null
    var isNoticeBoardStaffReport: LiveData<NoticeBoardStaffResponse?>? = null
        private set

    var IsGetEventReport: LiveData<EventResponse?>? = null
        private set

    var IsGetEventSchoolReport: LiveData<SchoolEventResponse?>? = null
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

    var isAssignmentUpdate: LiveData<HomeWorkSendResponse?>? = null
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
    var isGioStaffWiseAttendanceReportList: LiveData<AttendanceReportFromStaffDataClass?>? = null

    var isStudentReportList: LiveData<GetStudentReportData?>? = null
        private set
    var isSendAbsenteeSMS: LiveData<SendAbsenteeSMSResponse?>? = null
    var isChildAttendanceReport: LiveData<ChildAttendanceResponse?>? = null
    var isCertificateType: LiveData<CertificatesTypesResponse?>? = null
    var isNotificationResponse: LiveData<NotificationResponse?>? = null
    var isSendCertificateRequest: LiveData<StatusMessageModel?>? = null
    var isCertificateRequestList: LiveData<CertificatesListResponse?>? = null
    var isTimeTabletList: LiveData<TimeTableResponse?>? = null

    var isAttachmentResponse: LiveData<AttachmentReportResponse?>? = null
    var isAttachmentResponseArchive: LiveData<AttachmentReportResponse?>? = null

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
    var getlpcreateReport: LiveData<LessonPlanTemplateResponse?>? = null

    var isupdatelessonplan: LiveData<LessonPlanUpdateResponse?>? = null
    var iscreatelessonplan: LiveData<LessonPlanCreateResponse?>? = null

    var islessonplandelete: LiveData<LPDeleteResponse?>? = null

    var getcouponmenu: LiveData<CouponMenuResponse?>? = null

    var getCouponsSummary: LiveData<CampaignResponse?>? = null

    var getCouponsCategorySummary: LiveData<CampaignResponse?>? = null
    var getmycouponsSummary: LiveData<TicketSummaryResponse?>? = null

    var getCouponDetails: LiveData<ActivateCouponSummaryResponse?>? = null
    var sendactivatecoupon: LiveData<ActivateCouponResponse?>? = null
    var getdetailsforchat: LiveData<InteractionWithStaffResponse?>? = null
    var getstudentdetailsforchat: LiveData<InteractionWithStudentResponse?>? = null
    var getstaffanswers: LiveData<AnswerResponse?>? = null
    var getstaffquestions: LiveData<QuestionResponse?>? = null
    var sendquestion: LiveData<QuestionModelResponse?>? = null
    var sendanswer: LiveData<AnswerModelResponse?>? = null
    var getexams: LiveData<ExamTimetableResponse?>? = null
    var getexamslist: LiveData<ExamResponse?>? = null
    var getviewmarks: LiveData<ExamMarksResponse?>? = null
    var isleaverequestupdate: LiveData<LeaveUpdateResponse?>? = null
    var isstaffleaverequestupdate: LiveData<StaffLeaveUpdateRespone?>? = null
    var isleaverequestdelete: LiveData<LeaveRequestDeleteResponse?>? = null
    var isStaffleaverequestdelete: LiveData<StaffLeaveRequestDeleteResponse?>? = null
    var isnoticeboarddelete: LiveData<NoticeBoardDeleteResponse?>? = null
    var isEventDelete: LiveData<EventDeleteResponse?>? = null
    var isLsrwDelete: LiveData<LsrwDeleteResponse?>? = null
    var isAttachmentReportResponse: LiveData<AttachmentReportResponse?>? = null
    var getProgressMarks: LiveData<ProgressCardResponse?>? = null
    var isHomeWorkComplete: LiveData<StatusMessageModel?>? = null
    var getassignmentlist: LiveData<SubmissionResponse?>? = null
    var getLeaveCategories: LiveData<GetLeaveCategoriesData?>? = null
    var getStaffLeaveCategories: LiveData<GetStaffLeaveCategoriesData?>? = null
    var isAssignmentlist: LiveData<ParentAssignmentResponse?>? = null
    var isSubmitAssignment: LiveData<AssignmentSubmitResponse?>? = null
    var isStudentStats: LiveData<getStudentStats?>? = null
    var getassignmentmysubmission: LiveData<MySubmittedAssignmentsResponse?>? = null
    var islsrwskillsreport: LiveData<lsrwskillresponse?>? = null
    var islsrwstats: LiveData<AvgSkillResponse?>? = null

    var islsrwremarkupdate: LiveData<LsrwremarkUpdateModel?>? = null

    var isPtmSlotCreate: LiveData<StatusMessageModel?>? = null
    var isPtmSlotResponse: LiveData<SlotResponse?>? = null
    var isBookedSlotsData: LiveData<BookedSlotResponse?>? = null
    var isPtmSlotCancelReOpen: LiveData<StatusMessageModel?>? = null
    var isPtmSlotCancelClose: LiveData<StatusMessageModel?>? = null
    var isDateWiseSlot: LiveData<SlotBookingResponse?>? = null
    var isSlotBookingForStudent: LiveData<StatusMessageModel?>? = null
    var isStudentSlotResponse: LiveData<MeetingResponse?>? = null
    var isSlotCountResponse: LiveData<SlotCountResponse?>? = null
    var isSlotCancelByStudent: LiveData<StatusMessageModel?>? = null
    var isSlotValidation: LiveData<SlotValidationResponse?>? = null
    var isSlotDetailsHistory: LiveData<MeetingHistoryResponse?>? = null
    var isSubjectResponse: LiveData<SubjectResponse?>? = null
    var isQuizExamList: LiveData<GetQuizExamList?>? = null
    var isGetQuestion: LiveData<GetQuizQuestions?>? = null
    var isSubmitQuiz: LiveData<SubmitQuizResponse?>? = null
    var isGetMySubmission: LiveData<GetMySubmission?>? = null
    var islsrwStudentlist: LiveData<StudentSubmissionLsrwResponse?>? = null
    var isCreateQuiz: LiveData<CreateQuizResponse?>? = null
    var isEditQuiz: LiveData<EditQuizResponse?>? = null
    var isDeleteQuiz: LiveData<DeleteQuizResponse?>? = null
    var isGetQuizExamReport: LiveData<GetQuizExamReport?>? = null
    var isGetCheckLevel: LiveData<GetCheckLevel?>? = null
    var islsrwSkillCreate: LiveData<LsrwSkillSendResponse?>? = null
    var islsrwSkillSubmit: LiveData<LSRWSkillSubmitResponse?>? = null
    var islsrwSkilllist: LiveData<LsrwSkillResponse?>? = null
    var isGetPauketPoints: LiveData<PauketPointsResponse?>? = null
    var isSpentPoints: LiveData<SpentPointsModel?>? = null
    var isAddRewardPoints: LiveData<StatusMessageModel?>? = null
    var isGetQuizQuestionReport: LiveData<GetQuizQuestionReport?>? = null
    var isGetQuizSubmissionList: LiveData<GetQuizSubmissionList?>? = null
    var isGetPickFromQBank: LiveData<GetPickFromQBank?>? = null
    var isAddQuestion: LiveData<AddQuestionResponse?>? = null
    var ispresubmission: LiveData<ProfileUpdateResponse?>? = null
    var isGetMessageStaff: LiveData<GetMessagesStaff?>? = null
    var isGetMessageStaffArchive: LiveData<GetMessagesStaff?>? = null
    var isParentprofilelist: LiveData<ProfileListResponse?>? = null
    var isSchoolprofilelist: LiveData<ProfileListResponse?>? = null
    var islsrwmysubmission: LiveData<ActivityResponse?>? = null
    var getmysubmissionedit: LiveData<MySubmissionEditResponse?>? = null
    var ismysubmissiondelete: LiveData<MySubmissionDeleteResponse?>? = null
    var getchildhomeworkstandard: LiveData<ChildStandardResponse?>? = null
    var getassignmentchildhomework: LiveData<AssignmentTargetDetailsResponse?>? = null
    var getattachmentchildhomework: LiveData<AttachmentTargetDetailResponse?>? = null
    var getdashboardnewupdates: LiveData<WhatsNewUpdateResponse?>? = null
    var isFeeInvoices: LiveData<FeeInvoiceResponse?>? = null
    var isblockstudent: LiveData<BlockApiResponse?>? = null
    var isblockstudentlist: LiveData<BlockedStudentsResponse?>? = null
    var isfrequentlyasked: LiveData<FrequentlyModelResponse?>? = null
    var getAttendanceStudentList: LiveData<GetAttendanceStudentList?>? = null
    var isdeletenotification: LiveData<DeleteNotificationResponse?>? = null
        private set
    var getNewFeature: LiveData<GetFeature?>? = null
    var getreviewlist: LiveData<ReviewResponse?>? = null
    var reviewpost: LiveData<SubmitReviewResponse?>? = null

    var getStaffWiseExam: LiveData<getStaffWisExam?>? = null
    var getSubjectWiseActivities: LiveData<getSubjectWiseACtivities?>? = null
    var isGetMarkDetails: LiveData<MarkResponse?>? = null
    var isDeleteQuizQuestion: LiveData<DeleteQuizQuestionResponse?>? = null
    var uploadmarks: LiveData<UploadMarkResponse?>? = null
    var savemarks: LiveData<SaveMarksModel?>? = null
    var isStaffLeaveApply: LiveData<StaffLeaveRequestApplyRespone?>? = null

    var isstaffleaverequestapprove: LiveData<StaffLeaveRequestStatusUpdate?>? = null
    var getStaffleaverequesthistory: LiveData<getStaffLeaveRequestHistory?>? = null
    var getHomeworkSubmissionList: LiveData<GetHomeWorkSubmissionList?>? = null
    var getHostelList: LiveData<getHostelList?>? = null
    var getHostelDashboardDetails: LiveData<getHostelDashboard?>? = null
    var getHostelAttendanceSession: LiveData<getHostelAttendanceSession?>? = null
    var getHostelAttendanceRoomStudentList: LiveData<getHostelStudentRoomAttendance?>? = null
    var hotelMarkAttendance: LiveData<hostelMarkAttendanceRespone?>? = null
    var hotelSchoolOutpassRequest: LiveData<getSchoolHostelOutpassRequest?>? = null
    var hotelSchoolAttendanceReport: LiveData<getSchoolHostelAttendanceReport?>? = null
    var schoolHostelOutpassUpdateStatus: LiveData<schoolHostelOutpassUpdateStatus?>? = null
    var parentHotelDetails: LiveData<getParentHostelDetails?>? = null
    var parentHotelDashBoard: LiveData<getParentHostelDashboard?>? = null
    var applyHostelOutpass: LiveData<applyOutpassResponse?>? = null
    var isOnlinePaymentResponse: LiveData<OnlinePaymentResponse?>? = null
    var isPaymentStatusResponse: LiveData<PaymentStatusResponse?>? = null
    var isGetBusList: LiveData<getBusList?>? = null
    var isGetLiveBusData: LiveData<getLiveBus?>? = null
    var isgetgeolocation: LiveData<GetLatestGeoLocationResponse?>? = null
    var isSectionwisesubjectsdetail: LiveData<Subjectlistresponse?>? = null
    var isClassTestResponsel: LiveData<ClassTestResponse?>? = null
    var isViewClassTestResponsel: LiveData<ClassTestMarkResponse?>? = null
    var isCreateClasstest: LiveData<CreateClassTestResponse?>? = null
    var isgetExamreportdetails: LiveData<ExamlistModelResponse?>? = null
    var isputExamDelete: LiveData<ExamTestDeleteResponse?>? = null
    var isputClassTestDelete: LiveData<DeleteClassTestResponse?>? = null
    var isexamDetailsMark: LiveData<ClassEntryMarkResponse?>? = null
    var isexamdetailsmarkpost: LiveData<UploadMarksClassEntryResponse?>? = null
    var getisInvoiceDetails: LiveData<InvoiceDetailsResponse?>? = null
    var isExamtestAnalysis: LiveData<StudentAnalysisResponse?>? = null
    var isExamtestAnalysisSets: LiveData<AnalysisSetsResponse?>? = null

    var isFirstLocationSynced = false

    fun init() {
        isDashBoardData = apiSchoolRepositories.isDashBoardLiveData
        isDashBoardCountData = apiSchoolRepositories.isDashBoardCountLiveData

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
        isGetEventCategory = apiSchoolRepositories.isGetEventCategoryLiveData
        isAssignmentDelete = apiSchoolRepositories.isDeleteAssignmentLiveData
        isEditHomeWork = apiSchoolRepositories.isUpdateHomeworkLiveData
        isEditEvent = apiSchoolRepositories.isUpdateEventLiveData
        isEditAttachment = apiSchoolRepositories.isUpdateAttachmentLiveData
        isEditNoticeBoard = apiSchoolRepositories.isUpdateNoticeBoardLiveData
        isDeleteHomeWork = apiSchoolRepositories.isDeleteHomeworkLiveData
        isDeleteAttachment = apiSchoolRepositories.isAttachmentLiveData
        isNoticeBoardReport = apiSchoolRepositories.isNoticeBoardReportLiveData
        isNoticeBoardStaffReport = apiSchoolRepositories.isNoticeBoardStaffReportLiveData
        isGetDailyCollectionReport = apiSchoolRepositories.isGetDailyCollectionReportLiveData
        isGetSchoolStrengthReport = apiSchoolRepositories.isGetSchoolStrengthReportLiveData
        isDetailedPendingReport = apiSchoolRepositories.isDetailedPendingReportLiveData
        isDetailedWisePendingReport = apiSchoolRepositories.isDetailedWisePendingReportLiveData
        isSendText = apiSchoolRepositories.isSendTextLiveData
        isSendHomeWork = apiSchoolRepositories.isSendHomeWorkLiveData
        isAssignmentSend = apiSchoolRepositories.isSendAssignmentLiveData
        isAssignmentUpdate = apiSchoolRepositories.isUpdateAssignmentLiveData
        IsGetEventReport = apiSchoolRepositories.IsGetEventReportLiveData
        IsGetEventSchoolReport = apiSchoolRepositories.IsGetEventSchoolReportLiveData
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
        isStaffWiseAttendanceReportList = apiSchoolRepositories.isGiometricStaffWiseAttendanceReportLiveDataList
        isGioStaffWiseAttendanceReportList = apiSchoolRepositories.isGioStaffAttendanceReportLiveData
        isStudentReportList = apiSchoolRepositories.isStudentReportLiveData
        isSendAbsenteeSMS = apiSchoolRepositories.isSendAbsenteeSMSLiveData

        isChildAttendanceReport = apiParentRepositories.isChildAttendanceReportLiveData
        isCertificateType = apiParentRepositories.isCertificateTypesLiveData
        isNotificationResponse = apiParentRepositories.isNotificationResponseLiveData
        isCertificateRequestList = apiParentRepositories.isCertificateRequestListLiveData
        isSendCertificateRequest = apiParentRepositories.isSendCertificateLiveData
        isTimeTabletList = apiParentRepositories.isTimeTableListLiveData
        isAttachmentResponse = apiParentRepositories.isAttachmentResponseLiveData
        isAttachmentResponseArchive = apiParentRepositories.isAttachmentResponseArchiveLiveData
        getabsenteescountbydate = apiSchoolRepositories.getabsenteescountbydateLiveData
        getabsenteesstudentbydate = apiSchoolRepositories.getabsenteesstudentbydateLiveData
        isGetStudentAttendanceReportData = apiSchoolRepositories.isStudentAttendanceReportLiveData
        sendnotice = apiSchoolRepositories.sendnoticeLiveData
        getisInvoiceDetails = apiParentRepositories.getisInvoiceDetailsLiveData
        sendevent = apiSchoolRepositories.sendeventLiveData
        isAttachmentSend = apiSchoolRepositories.sendAttachmentLiveData
        isLeaveRequest = apiParentRepositories.leaveRequestLiveData
        getleaverequest = apiSchoolRepositories.leaverequestLiveData
        isleaverequestapprove = apiSchoolRepositories.isleaverequestapproveLiveData
        isupdatelessonplan = apiSchoolRepositories.isupdatelessonplanLiveData
        iscreatelessonplan = apiSchoolRepositories.iscreatelessonplanLiveData

        getlpStaffReport = apiSchoolRepositories.isgetlpStaffReportLiveData
        getlpViewReport = apiSchoolRepositories.isgetlpViewReportLiveData
        getlpeditReport = apiSchoolRepositories.isgetlpeditReportLiveData
        getlpcreateReport = apiSchoolRepositories.isgetlpcreateReportLiveData

        islessonplandelete = apiSchoolRepositories.islessonplandeleteLiveData

        getcouponmenu = apiSchoolRepositories.getcouponmenuLiveData

        getCouponsSummary = apiSchoolRepositories.getCouponsSummaryLiveData
        getCouponsCategorySummary = apiSchoolRepositories.getCouponsCategorySummaryLiveData
        getmycouponsSummary = apiSchoolRepositories.getmycouponsSummaryLiveData
        getCouponDetails = apiSchoolRepositories.getCouponDetailsLiveData
        sendactivatecoupon = apiSchoolRepositories.sendactivatecouponLiveData
        getdetailsforchat = apiParentRepositories.getdetailsforchatLiveData
        getstaffanswers = apiParentRepositories.getstaffanswersLiveData
        getstaffquestions = apiSchoolRepositories.getstaffquestionsLiveData
        sendquestion = apiParentRepositories.sendquestionLiveData
        sendanswer = apiSchoolRepositories.sendanswerLiveData
        getexams = apiParentRepositories.getexamsLiveData
        getexamslist = apiParentRepositories.getexamslistLiveData
        getviewmarks = apiParentRepositories.getviewmarksLiveData
        isleaverequestupdate = apiParentRepositories.isleaverequestupdateLiveData
        isleaverequestdelete = apiParentRepositories.isleaverequestdeleteLiveData
        getProgressMarks = apiParentRepositories.getProgressMarksLiveData
        isHomeWorkComplete = apiParentRepositories.isUpdateCompleteHomeWorkLiveData
        getstudentdetailsforchat = apiParentRepositories.getstudentdetailsforchatLiveData
        isnoticeboarddelete = apiSchoolRepositories.isnoticeboarddeleteLiveData
        isEventDelete = apiSchoolRepositories.isEventDeleteLiveData
        isLsrwDelete = apiSchoolRepositories.isLsrwDeleteLiveData
        isAttachmentReportResponse = apiSchoolRepositories.isAttachmentResponseLiveData
        getassignmentlist = apiSchoolRepositories.getassignmentlistLiveData
        getLeaveCategories = apiParentRepositories.getLeaveCategoriesLiveData
        isAssignmentlist = apiParentRepositories.isAssignmentlistLiveData
        isSubmitAssignment = apiParentRepositories.isSubmitAssignmentLiveData
        isStudentStats = apiParentRepositories.isStudentStatsLiveData
        getassignmentmysubmission = apiParentRepositories.getassignmentmysubmissionlistLiveData
        islsrwskillsreport = apiSchoolRepositories.islsrwskillsreportLiveData
        islsrwStudentlist = apiSchoolRepositories.islsrwStudentlistLiveData
        islsrwremarkupdate = apiSchoolRepositories.islsrwremarkupdateLiveData

        isPtmSlotCreate = apiSchoolRepositories.isPtmSlotCreateLiveData
        isPtmSlotResponse = apiSchoolRepositories.isPtmSlotResponseLiveData
        isBookedSlotsData = apiSchoolRepositories.isBookedSlotResponseLiveData
        isPtmSlotCancelReOpen = apiSchoolRepositories.isPtmSlotCancelReOpenLiveData
        isPtmSlotCancelClose = apiSchoolRepositories.isPtmSlotCancelCloseLiveData
        isDateWiseSlot = apiSchoolRepositories.isDateWiseSlotLiveData
        isSlotBookingForStudent = apiParentRepositories.isSlotBookingStudentLiveData
        isStudentSlotResponse = apiParentRepositories.isStudentSlotResponseLiveData
        isSlotCountResponse = apiParentRepositories.isSlotCountResponseLiveData
        isSlotCancelByStudent = apiParentRepositories.isSlotCancelByStudentLiveData
        isSlotValidation = apiSchoolRepositories.isSlotValidationLiveData
        isSlotDetailsHistory = apiParentRepositories.isSlotDetailsHistoryLiveData
        isSubjectResponse = apiParentRepositories.isSubjectResponseLiveData
        isQuizExamList = apiParentRepositories.isQuizExamListLiveData
        isGetQuestion = apiParentRepositories.isGetQuestionLiveData
        isSubmitQuiz = apiParentRepositories.isSubmitQuizLiveData
        isGetMySubmission = apiParentRepositories.isMySubmissionLiveData
        isCreateQuiz = apiSchoolRepositories.isCreateQuizLiveData
        isGetQuizExamReport = apiSchoolRepositories.isGetQuizExamReportLiveData
        isGetCheckLevel = apiSchoolRepositories.isGetCheckLevelLiveData
        islsrwSkillCreate = apiSchoolRepositories.islsrwSkillCreateLiveData
        islsrwSkillSubmit = apiParentRepositories.islsrwSkillSubmitLiveData
        islsrwSkilllist = apiParentRepositories.islsrwSkilllistLiveData
        islsrwstats = apiSchoolRepositories.islsrwstatsLiveData
        isGetQuizQuestionReport = apiSchoolRepositories.isGetQuizQuestionReportLiveData
        isGetQuizSubmissionList = apiSchoolRepositories.isGetQuizSubmissionListLiveData
        isGetPickFromQBank = apiSchoolRepositories.isGetPickFromQBankLiveData
        isAddQuestion = apiSchoolRepositories.isAddQuestionLiveData
        ispresubmission = apiParentRepositories.ispresubmissionLiveData
        isGetMessageStaff = apiSchoolRepositories.isGetMessageStaffLiveData
        isGetMessageStaffArchive = apiSchoolRepositories.isGetMessageStaffArchiveLiveData
        isGetPauketPoints = apiParentRepositories.isGetPauketPointsLiveData
        isSpentPoints = apiParentRepositories.isSpentPointsLiveData
        isAddRewardPoints = apiParentRepositories.isAddRewardPointsLiveData
        isParentprofilelist = apiParentRepositories.isprofilelistLiveData
        isSchoolprofilelist = apiSchoolRepositories.isSchoolprofilelistLiveData
        islsrwmysubmission = apiParentRepositories.islsrwmysubmissionLiveData
        isFeeInvoices = apiParentRepositories.isFeeInvoices
        isblockstudent = apiSchoolRepositories.isblockstudentLiveData
        isblockstudentlist = apiSchoolRepositories.isblockstudentlistLiveData
        isfrequentlyasked = apiSchoolRepositories.isfrequentlyaskedLiveData
        getmysubmissionedit = apiParentRepositories.getmysubmissioneditLiveData
        ismysubmissiondelete = apiParentRepositories.ismysubmissiondeleteLiveData
        getchildhomeworkstandard = apiSchoolRepositories.getchildhomeworkstandardLiveData
        getassignmentchildhomework = apiSchoolRepositories.getassignmentchildhomeworkLiveData
        getattachmentchildhomework = apiSchoolRepositories.getattachmentchildhomeworkLiveData
        getdashboardnewupdates = apiSchoolRepositories.getdashboardnewupdatesLiveData
        getNewFeature = apiSchoolRepositories.getnewfeatureLiveData
        getAttendanceStudentList = apiSchoolRepositories.getAttendanceStudentListLiveData
        isdeletenotification = apiSchoolRepositories.isdeletenotificationLiveData
        getreviewlist = apiSchoolRepositories.getreviewlistLiveData
        reviewpost = apiSchoolRepositories.reviewpostLiveData
        getStaffWiseExam = apiSchoolRepositories.getStaffWiseExamLiveData
        getSubjectWiseActivities = apiSchoolRepositories.getSubjectWiseActivitiesLiveData
        isGetMarkDetails = apiSchoolRepositories.isGetMarkDetailsLiveData
        isDeleteQuiz = apiSchoolRepositories.isDeleteQuizLiveData
        isEditQuiz = apiSchoolRepositories.isEditQuizLiveData
        isDeleteQuizQuestion = apiSchoolRepositories.isDeleteQuizQuestionLiveData
        uploadmarks = apiSchoolRepositories.uploadmarksLiveData
        savemarks = apiSchoolRepositories.savemarksLiveData
        isStaffLeaveApply = apiSchoolRepositories.isStaffLeaveRequestLiveData
        getStaffLeaveCategories = apiSchoolRepositories.isStaffLeaveCategoriesLiveData
        isstaffleaverequestupdate = apiSchoolRepositories.isstaffleaverequestupdateLiveData
        isstaffleaverequestapprove = apiSchoolRepositories.isStaffleaverequestapproveLiveData
        getStaffleaverequesthistory = apiSchoolRepositories.isStaffleaverequestHistoryLiveData
        isStaffleaverequestdelete = apiSchoolRepositories.isStaffleaverequestdeleteLiveData
        getHomeworkSubmissionList = apiSchoolRepositoriesTwo.isHomeworkSubmissionListLiveData
        getHostelList = apiSchoolRepositoriesTwo.isHostelListLiveData
        getHostelDashboardDetails = apiSchoolRepositoriesTwo.isHostelDashboardDetailsLiveData
        getHostelAttendanceSession = apiSchoolRepositoriesTwo.isHostelAttendanceSessionLiveData
        getHostelAttendanceRoomStudentList = apiSchoolRepositoriesTwo.isHostelAttendanceRoomStudentListLiveData
        hotelMarkAttendance = apiSchoolRepositoriesTwo.isHotelMarkAttendanceLiveData
        hotelSchoolOutpassRequest = apiSchoolRepositoriesTwo.isHotelSchoolOutpassRequestLiveData
        hotelSchoolAttendanceReport = apiSchoolRepositoriesTwo.isHotelSchoolAttendanceReportLiveData
        schoolHostelOutpassUpdateStatus = apiSchoolRepositoriesTwo.isSchoolHostelOutpassUpdateStatusLiveData
        parentHotelDetails = apiSchoolRepositoriesTwo.isParentHotelDetailsLiveData
        parentHotelDashBoard = apiSchoolRepositoriesTwo.isParentHotelDashBoardLiveData
        applyHostelOutpass = apiParentRepositories.isapplyHostelOutpassLiveData
        isOnlinePaymentResponse = apiParentRepositories.isOnlinePaymentResponseLiveData
        isPaymentStatusResponse = apiParentRepositories.isPaymentStatusResponseLiveData
        isGetBusList = apiParentRepositories.isBusListLiveData
        isGetLiveBusData = apiParentRepositories.isLiveBusLiveData
        isgetgeolocation = apiParentRepositories.isgetgeolocationData
        isSectionwisesubjectsdetail = apiSchoolRepositoriesTwo.isSectionwisesubjectsdetailLiveData
        isClassTestResponsel = apiSchoolRepositoriesTwo.isClassTestResponselLiveData
        isViewClassTestResponsel = apiSchoolRepositoriesTwo.isViewClassTestResponselLiveData
        isCreateClasstest = apiSchoolRepositoriesTwo.isCreateClasstestLiveData
        isgetExamreportdetails = apiSchoolRepositoriesTwo.isgetExamreportdetailsLiveData
        isputExamDelete = apiSchoolRepositoriesTwo.isputExamDeleteLiveData
        isputClassTestDelete = apiSchoolRepositoriesTwo.isputClassTestDeleteLiveData
        isexamDetailsMark = apiSchoolRepositoriesTwo.isexamDetailsMarkLiveData
        isexamdetailsmarkpost = apiSchoolRepositoriesTwo.isexamdetailsmarkpostLiveData
        isExamtestAnalysis = apiSchoolRepositoriesTwo.isExamtestAnalysisLiveData
        isExamtestAnalysisSets = apiSchoolRepositoriesTwo.isExamtestAnalysisSetsLiveData
    }

    fun isDashBoardData(
        isToken: String,
        isMemberType: String,
        isMobileNumber: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isDashBoard(isToken, isMemberType, isMobileNumber, activity)
    }

    fun isDashBoardCountData(isToken: String, isMemberType: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isDashBoardCount(isToken, isMemberType, activity)
    }

    fun isGetAds(isToken: String, isMenuId: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetAds(isToken, isMenuId, activity)
    }

    fun isGetGlobalVariables(jsonObject: JsonObject, isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetGlobalVariables(jsonObject, isToken, activity)
    }

    fun isGetStaffList(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetStaffList(isToken, activity)
    }

    fun isGetSubjectList(
        isToken: String, isAcademicYearId: Int, isSectionId: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetSubjectList(isToken, isAcademicYearId, isSectionId, activity)
    }


    fun isGetStandardSection(isToken: String, isAcademicYearId: Int, activity: Activity) {
        Log.d("isAcademicYearIdData", isAcademicYearId.toString())
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetStandardSection(isToken, isAcademicYearId, activity)
    }

    fun isGetStudentList(
        isToken: String, isSection: String, isAcademicYearId: Int, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetStudentList(isToken, isSection, isAcademicYearId, activity)
    }

    fun isGetGroupList(isToken: String, isAcademicYearId: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetGroupList(isToken, isAcademicYearId, activity)
    }

    fun isGetCommmunicationlistload(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetCommmunicationlistload(isToken, activity)
    }


    fun isGetCommmunicationlist(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetCommmunicationlist(isToken, activity)
    }

    fun isGetHomeWorkReport(
        isToken: String, isSectionId: Int, isAcademicYearId: Int, isdate: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetHomeWorkReport(
            isToken, isSectionId, isAcademicYearId, isdate, activity
        )
    }


    fun isGetAssignmentReport(
        isToken: String, isAcademicYearId: Int, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetAssignmentReport(
            isToken, isAcademicYearId, activity
        )
    }

    fun isGetEventCategories(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isEventCategories(
            isToken, activity
        )
    }

    fun isAssignmentDelete(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isDeleteAssignment(
            isToken, jsonObject, activity
        )
    }

    fun isGetDailyCollectionReport(
        isToken: String,
        istype: String,
        isfromdate: String,
        istodate: String,
        country_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetDailyCollectionReport(
            isToken, istype, isfromdate, istodate, country_id, activity
        )
    }

    fun isDetailedPendingReport(
        isToken: String,
        isAcademicYearId: Int,
        country_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isDetailedPendingReport(
            isToken,
            isAcademicYearId,
            country_id,
            activity
        )
    }

    fun isDetailedWisePendingReport(
        isToken: String,
        isAcademicYearId: Int,
        country_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isDetailedWisePendingReport(
            isToken,
            isAcademicYearId,
            country_id,
            activity
        )
    }

    fun isGetSchoolStrengthReport(isToken: String, isAcademicYearId: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetSchoolStrengthReport(isToken, isAcademicYearId, activity)
    }

    fun isNoticeBoardReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isNoticeBoardReport(isToken, activity)
    }

    fun isNoticeBoardStaffReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isNoticeBoardStaffReport(isToken, activity)
    }

    fun IsGetEventReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.IsGetEventReport(isToken, activity)
    }

    fun IsGetEventSchoolReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.IsGetEventSchoolReport(isToken, activity)
    }

    fun IsGetHolidayReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.IsGetHolidayReport(isToken, activity)
    }

    fun isGetTextHistory(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetTextHistory(isToken, activity)
    }

    fun isGetVoiceHistory(isToken: String, isEmergency: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetVoiceHistory(isToken, isEmergency, activity)
    }

    fun isSendText(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSendText(isToken, josnObject, activity)
    }

    fun isSendHomeWork(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSendHomeWork(isToken, josnObject, activity)
    }

    fun isSendAssignment(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSendAssignment(isToken, josnObject, activity)
    }

    fun assignmentUpdate(isToken: String, josnObject: JsonObject, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.assignmentUpdate(isToken, josnObject, activity)
    }

    fun isVoiceSend(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSendVoice(isToken, josnObject, activity)
    }

    fun isUpdateStatusArchive(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories?.isUpdateStatusArchive(isToken, jsonObject, activity)
    }

    fun isGetAcademicYear(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories?.isGetAcademicYear(isToken, activity)
    }


    fun isUpdateStatusCommunication(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories?.isUpdateStatusCommunication(isToken, jsonObject, activity)
    }

    //get homework details
    fun isHomeWorkDetails(isToken: String, activity: Activity, date: String) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories?.isHomeWorkDetails(isToken, activity, date)
    }


    fun punchAttendance(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.punchAttendance(isToken, jsonObject, activity)
    }

    fun addLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.addLocation(isToken, jsonObject, activity)
    }

    fun removeLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.removeLocation(isToken, jsonObject, activity)
    }

    fun updateLocation(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.updateLocation(isToken, jsonObject, activity)
    }

    fun getStaffLocations(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getStaffLocations(isToken, activity)
    }

    fun getPunchHistory(isToken: String, isDate: String, staff_id: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getPunchHistory(isToken, isDate, staff_id, activity)
    }

    fun getLocationHistory(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getLocationHistory(isToken, activity)
    }

    fun getStaffAttendanceReport(isToken: String, attendance_dt: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getGiometricStaffAttendancereport(isToken, attendance_dt, activity)
    }


    fun getStaffWiseAttendanceReport(isToken: String, isCurrentDate: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getGiometricStaffWiseAttendancereport(
            isToken, isCurrentDate, activity
        )
    }

    fun getStaffWiseAttendanceReportList(
        isToken: String, isSelectedDate: String, isStaffId: Int, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getGiometricStaffWiseAttendancereportStaffList(
            isToken, isSelectedDate, isStaffId, activity
        )
    }

    fun getGioMetricAttendanceReport(
        isToken: String, isFromDate: String, isToDate: String, isAll: Boolean,  isStaffId: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getGiometricStaffWiseAttendancereport(
            isToken, isFromDate,isToDate,isAll, isStaffId, activity
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
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getStudentReportList(
            isToken, isAcademicYearId, class_id, section_id, activity
        )
    }

    fun isUpdateSendAbsenteeSMS(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isUpdateSendAbsenteeSMS(isToken, jsonObject, activity)
    }

    fun getChildAttendanceReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getChildAttendanceReport(isToken, activity)
    }


    fun getabsenteescountbydate(isToken: String, month_id: Int, year_id: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getabsenteescountbydate(isToken, month_id, year_id, activity)
    }

    fun getabsenteesstudentbydate(
        isToken: String,
        absent_on: String,
        standard_id: String,
        section_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getabsenteesstudentbydate(
            isToken,
            absent_on,
            standard_id,
            section_id,
            activity
        )
    }

    fun getStudentAttendanceReport(
        isToken: String,
        section_id: String,
        from_date: String,
        to_date: String,
        class_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getStudentAttendanceReportForSchool(
            isToken, section_id, from_date, to_date, class_id, activity
        )
    }

    fun getCertificateTypes(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getCertificateTypes(isToken, activity)
    }

    fun getCertificateRequestList(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getCertificateRequestList(isToken, activity)
    }

    fun sendCertificateRequest(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.sendCertificateRequest(isToken, jsonObject, activity)
    }

    fun getTimeTable(isToken: String, day_id: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getTimeTable(isToken, day_id, activity)
    }

    fun sendnotice(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.sendnotice(isToken, josnObject, activity)
    }

    fun sendevent(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.sendevent(isToken, josnObject, activity)
    }

    fun sendAttachment(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.sendAttachment(isToken, josnObject, activity)
    }

    fun getAttachment(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getAttachmentList(isToken, activity)
    }

    fun getAttachmentArchive(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.attachmentListArchive(isToken, activity)
    }

    fun isSendLeaveRequestApply(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isLeaveRequestApply(isToken, jsonObject, activity)
    }

    fun getleaverequest(isToken: String, member_type: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getleaverequest(isToken, member_type, activity)
    }


    fun getlpStaffReport(isToken: String, request_type: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getlpStaffReport(isToken, request_type, activity)
    }

    fun getAttachmentListReport(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getAttachmentReportList(isToken, activity)
    }


    fun getlpViewReport(
        isToken: String, section_subject_id: String, lesson_plan_status: Int, activity: Activity
    ) {

        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getlpViewReport(
            isToken, section_subject_id, lesson_plan_status, activity
        )
    }

    fun getlpeditReport(
        isToken: String, particular_id: String, request_type: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getlpeditReport(isToken, particular_id, request_type, activity)
    }

    fun getlpcreateReport(
        isToken: String, request_type: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getlpcreateReport(isToken, request_type, activity)
    }

    fun isleaverequestapprove(isToken: String, request: LeaveApproveRequest, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isleaverequestapprove(isToken, request, activity)
    }

    fun isupdatelessonplan(isToken: String, requestBody: RequestBody, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isupdatelessonplan(isToken, requestBody, activity)
    }


    fun iscreatelessonplan(isToken: String, requestBody: RequestBody, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.iscreatelessonplan(isToken, requestBody, activity)
    }

    fun islessonplandelete(isToken: String, requestBody: RequestBody, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.islessonplandelete(isToken, requestBody, activity)
    }

    fun getcouponmenu(parentName: String, apiKey: String) {
        apiSchoolRepositories.getcouponmenu(parentName, apiKey)
    }

    fun getCouponsSummary(mobile_no: String, parentName: String, apiKey: String) {
        apiSchoolRepositories.getCouponsSummary(mobile_no, parentName, apiKey)
    }

    fun getCouponsCategorySummary(
        category_id: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        apiSchoolRepositories.getCouponsCategorySummary(category_id, mobile_no, parentName, apiKey)
    }

    fun getmycouponsSummary(
        coupon_status: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        apiSchoolRepositories.getmycouponsSummary(coupon_status, mobile_no, parentName, apiKey)
    }

    fun getCouponDetails(
        source_link: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        apiSchoolRepositories.getCouponDetails(source_link, mobile_no, parentName, apiKey)
    }

    fun sendactivatecoupon(
        source_link: String, mobile_no: String, parentName: String, apiKey: String
    ) {
        apiSchoolRepositories.sendactivatecoupon(source_link, mobile_no, parentName, apiKey)
    }

    fun getdetailsforchat(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getdetailsforchat(isToken, activity)
    }

    fun getstudentdetailsforchat(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getstudentdetailsforchat(isToken, activity)
    }

    fun getstaffanswers(
        isToken: String,
        staff_id: String,
        subject_id: String,
        offset: Int,
        is_class_teacher: Boolean,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getstaffanswers(
            isToken, staff_id, subject_id, offset, is_class_teacher, activity
        )
    }

    fun getstaffquestions(
        isToken: String,
        is_class_teacher: Boolean,
        section_id: String,
        subject_id: String,
        offset: Int,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getstaffquestions(
            isToken, is_class_teacher, section_id, subject_id, offset
        )
    }


    fun sendquestion(
        isToken: String, request: QuestionModelRequest, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)

        apiParentRepositories.sendquestion(isToken, request)
    }

    fun sendanswer(
        isToken: String, request: AnswerModelRequest, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)

        apiSchoolRepositories.sendanswer(isToken, request)
    }

    fun getexams(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getexams(isToken)
    }

    fun getexamslist(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getexamslist(isToken)
    }


    fun getviewmarks(
        isToken: String, exam_id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getviewmarks(isToken, exam_id)
    }


    fun isleaverequestupdate(isToken: String, request: LeaveRequestUpdate, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isleaverequestupdate(isToken, request, activity)
    }

    fun isleaverequestdelete(isToken: String, request: LeaveRequestDelete, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isleaverequestdelete(isToken, request, activity)
    }


    fun isnoticeboarddelete(isToken: String, request: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isnoticeboarddelete(isToken, request, activity)
    }


    fun isEventDelete(isToken: String, request: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isEventDelete(isToken, request, activity)
    }

    fun isLsrwDelete(isToken: String, request: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isLsrwDelete(isToken, request, activity)
    }

    fun isHomeWorkComplete(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isHomeWorkComplete(isToken, jsonObject)
    }

    fun isHomeWorkUpdate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isEditHomeWork(isToken, jsonObject, activity)
    }

    fun isEventUpdate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isEditEvent(isToken, jsonObject, activity)
    }


    fun isNoticeBoardUpdate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isEditNoticeBoard(isToken, jsonObject, activity)
    }


    fun isHomeWorkDelete(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isHomeWorkDelete(isToken, jsonObject, activity)
    }

    fun isAttachmentUpdate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isEditAttachment(isToken, jsonObject, activity)
    }


    fun isAttachmentDelete(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isAttachmentDelete(isToken, jsonObject, activity)
    }

    fun getProgressMarks(
        isToken: String,jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.getProgressMarks(isToken, jsonObject)
    }

    fun getassignmentlist(
        isToken: String, id: String, type: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getassignmentlist(isToken, id, type)
    }


    fun getLeaveCategories(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getLeaveCategories(isToken)
    }


    fun isAssignmentlist(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isAssignmentlist(isToken)
    }


    fun isSubmitAssignment(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isSubmitAssignment(isToken, jsonObject, activity)
    }

    fun isStudentStats(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isStudentStats(isToken)
    }

    fun isNotificationList(token: String, deviceType: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isNotifications(token, deviceType)
    }


    fun isGetAssignmentSubList(
        isToken: String, id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getassignmentmysubmissionlist(isToken, id)

    }


    fun islsrwskillsreport(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.islsrwskillsreport(isToken)

    }

    fun islsrwStudentlist(
        isToken: String,
        id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.islsrwStudentlist(isToken, id)

    }


    fun islsrwSkillCreate(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.islsrwSkillCreate(isToken, josnObject, activity)
    }


    fun islsrwSkillSubmit(isToken: String, josnObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.islsrwSkillSubmit(isToken, josnObject, activity)
    }

    fun islsrwSkilllist(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.islsrwSkilllist(isToken)
    }


    fun islsrwstats(isToken: String, month_id: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.islsrwstats(isToken, month_id)
    }


    fun islsrwremarkupdate(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.islsrwremarkupdate(isToken, jsonObject, activity)
    }

    // PTM


    fun isSlotCreating(
        isToken: String, jsonObject: JsonArray, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isPtmSlotCreating(isToken, jsonObject)
    }

    fun isSlotForStaff(
        isToken: String, isEventDate: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isPtmSlotForStaff(isToken, isEventDate)
    }

    fun isBookedSlotsData(
        isToken: String, isEventDate: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isBookedSlot(isToken, isEventDate)
    }


    fun isSlotCancelReOpen(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSlotCancelReOpen(isToken, jsonObject)
    }

    fun isSlotCancelClose(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSlotCancelAndClose(isToken, jsonObject)
    }

    fun isSlotDateWiseSlots(
        isToken: String, isEventDate: String
    ) {
        apiSchoolRepositories.isDatewiseBookedSlots(isToken, isEventDate)
    }

    fun isSlotBookingStudent(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isSlotBookingStudent(isToken, jsonObject)
    }


    fun isSlotAvailableForStudent(
        isToken: String,
        isEventDate: String,
        isSubjectId: String,
        isClassTeacherId: String,
        isManagement: Boolean,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isSlotAvailableForStudent(
            isToken, isEventDate, isSubjectId, isClassTeacherId, isManagement
        )
    }

    fun isSlotCountByDate(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isSlotCountFromDate(isToken)
    }

    fun isSlotCancelByStudent(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isASlotCancelByStudent(isToken, jsonObject)
    }

    fun isSlotValidationForStaff(
        isToken: String, jsonObject: JsonArray, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isSlotValidationForStaff(isToken, jsonObject)
    }

    fun isSlotHistoryStudent(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isSlotHistoryForStudent(isToken)
    }

    fun isSubjectListWithClassTeacher(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isSubjectListWithClassTeacher(isToken)
    }

    fun isQuizExamList(
        isToken: String,
        type: String,
        status_type: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isQuizExamList(isToken, type, status_type)
    }

    fun isGetQuestions(
        isToken: String,
        id: String, activity: Activity


    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isGetQuestions(isToken, id)
    }

    fun isSubmitQuiz(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isSubmitQuiz(
            isToken, jsonObject,
        )
    }

    fun isGetMySubmission(
        isToken: String,
        id: String,
        student_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isGetMySubmission(isToken, id,student_id)
    }


    fun isCreateQuiz(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isSubmitQuiz(
            isToken, jsonObject,
        )
    }

    fun isEditQuiz(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isEditQuiz(
            isToken, jsonObject,
        )
    }

    fun isDeleteQuiz(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isDeleteQuiz(
            isToken, jsonObject,
        )
    }

    fun isGetQuizExamReport(
        isToken: String, type: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetQuizExamReport(
            isToken, type,
        )
    }

    fun isGetCheckLevel(
        isToken: String,
        class_id: String,
        subject_id: String,
        section_id: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetCheckLevel(
            isToken, class_id, subject_id, section_id
        )
    }

    fun isGetQuizQuestionReport(
        isToken: String, id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetQuizQuestionReport(
            isToken, id
        )
    }

    fun isGetQuizSubmissionList(
        isToken: String, id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetQuizSubmissionList(
            isToken, id
        )
    }

    fun isGetPickFromQBank(
        isToken: String, subject_id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetPickFromQBank(
            isToken, subject_id
        )
    }

    fun isQuizAddQuestion(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isQuizAddQuestion(
            isToken, jsonObject,
        )
    }

    fun isGetMessageStaff(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetMessageFromStaff(
            isToken
        )
    }

    fun isGetMessageStaffArchive(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetMessageFromStaffArchive(
            isToken
        )
    }


    fun isGetPauketPoints(
        isToken: String,
        mobile_number: Long,
        user_type: Int,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isGetPauketPoints(isToken, mobile_number, user_type)
    }

    fun isSpentPoints(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isSpentPoints(
            isToken, jsonObject,
        )
    }

    fun isAddRewardPoints(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isAddRewardPoints(
            isToken, jsonObject,
        )
    }

    fun isParentprofilelist(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isParentprofilelist(isToken)
    }


    fun isSchoolprofilelist(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isSchoolprofilelist(isToken)
    }

    fun islsrwmysubmission(isToken: String, id: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.islsrwmysubmission(isToken, id)
    }


    fun ispresubmission(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.ispresubmission(
            isToken, jsonObject, activity
        )
    }

    fun getStudentInvoices(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getStudentInvoices(isToken, activity)
    }

    fun getInvoiceDetails(isToken: String, invoiceId: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.getInvoiceDetails(isToken, invoiceId)
    }

    fun getmysubmissionedit(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.getmysubmissionedit(
            isToken, jsonObject, activity
        )
    }


    fun ismysubmissiondelete(isToken: String, request: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.ismysubmissiondelete(isToken, request, activity)
    }


    fun getchildhomeworkstandard(isToken: String, id: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getchildhomeworkstandard(isToken, id)
    }


    fun getassignmentchildhomework(isToken: String, id: Int, target_type: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getassignmentchildhomework(isToken, id, target_type)
    }


    fun getattachmentchildhomework(isToken: String, id: Int, target_type: Int, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getattachmentchildhomework(isToken, id, target_type)
    }


    fun getdashboardnewupdates(isToken: String, role_type: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getdashboardnewupdates(isToken, role_type)
    }

    fun getAttendanceStudentList(
        isToken: String,
        class_id: String,
        section_id: String,
        date: String,
        isAcademicYearId: String,
        attendance_type: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetAttendanceStudentList(
            isToken,
            class_id,
            section_id,
            date,
            isAcademicYearId,
            attendance_type
        )
    }


    fun isblockstudent(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isblockstudent(isToken, jsonObject)
    }


    fun isblockstudentlist(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isblockstudentlist(isToken)
    }


    fun isfrequentlyasked(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isfrequentlyasked(isToken)
    }

    fun isdeletenotification(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isdeletenotification(isToken, jsonObject)
    }

    fun isGetFeature() {
        apiSchoolRepositories.isgetfeature()
    }


    fun getreviewlist(
        isToken: String,
        mobile_number: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getreviewlist(isToken, mobile_number)
    }


    fun reviewpost(
        isToken: String,
        jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.reviewpost(isToken, jsonObject)
    }

    fun getStaffWiseExam(
        isToken: String,
        section_id: String,
        isAcademicYearId: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
//        val reporting_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetStaffWiseExam(isToken, section_id,isAcademicYearId)
    }

    fun getSubjectWiseActivities(
        isToken: String,
        exam_id: String,
        section_id:String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
//        val reporting_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetSubjectWiseActivities(isToken, exam_id,section_id)
    }

    fun isMarkDetails(
        isToken: String,
        jsonObject: JsonObject, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
//        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.isGetMarkDetails(isToken, jsonObject)
    }

    fun isDeleteQuizQuestion(
        isToken: String, jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isDeleteQuizQuestion(
            isToken, jsonObject,
        )
    }


    fun uploadmarks(
        part: MultipartBody.Part, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.uploadmarks(part, activity)
    }


    fun savemarks(isToken: String, jsonObject: JsonObject, activity: Activity) {

        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.savemarks(isToken, jsonObject)
    }

    fun isSendStaffLeaveRequestApply(isToken: String, jsonObject: JsonObject, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)

        apiSchoolRepositories.isStaffLeaveRequestApply(isToken, jsonObject, activity)
    }

    fun getStaffLeaveCategories(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositories.getStaffLeaveCategories(isToken)
    }

    fun isstaffleaverequestupdate(isToken: String, request: LeaveRequestUpdate, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)

        apiSchoolRepositories.isstaffleaverequestupdate(isToken, request, activity)
    }

    fun isStaffleaverequestapprove(isToken: String, request: LeaveApproveRequest, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)

        apiSchoolRepositories.isStaffleaverequestapprove(isToken, request, activity)
    }

    fun getStaffleaverequest(isToken: String, staff_id: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)

        apiSchoolRepositories.getStaffleaverequest(isToken,staff_id, activity)
    }


    fun isStaffleaverequestdelete(isToken: String, request: LeaveRequestDelete, activity: Activity) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositories.isStaffleaverequestdelete(isToken, request, activity)
    }

    fun isHomeSubmissionList(isToken: String, id: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)

        apiSchoolRepositoriesTwo.getHomeworkSubmissionList(isToken,id, activity)
    }

    fun isGetHostelList(isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.getHostelList(isToken, activity)
    }

    fun isGetHostelDashboardDetails(isToken: String, hostel_id: String, academic_year_id : String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.getHostelDashboardDetails(isToken,hostel_id,academic_year_id, activity)
    }

    fun isGetHostelAttendanceSessionDetails(isToken: String,activity: Activity) {
//        val reporting_url = SharedPreference.getReportingUrl(activity)
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositoriesTwo.getHostelAttendanceSession(isToken, activity)
    }


    fun isGetHostelAttendanceRoomStudentList(isToken: String, hostel_id : String, room_id: String, academic_year_id :String, date : String, session_type_id : String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.getHostelAttendanceRoomStudentList(isToken,hostel_id,room_id,academic_year_id,date,session_type_id, activity)
    }

    fun hostelMarkAttendance(
        isToken: String,
        jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositoriesTwo.hostelMarkAtttendance(isToken, jsonObject,activity)
    }

    fun isGetHostelSchoolOutpassRequestList(isToken: String, year_id  : String, month_id : String, hostel_id  :String, academic_year_id  : String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.getHostelSchoolOutpassRequestList(isToken,year_id,month_id,hostel_id,academic_year_id, activity)
    }


    fun isGetHostelSchoolAttendanceReport(isToken: String, hostel_id : String, date: String, academic_year_id:String,activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.getSchoolHostelAttendanceReport(isToken,hostel_id,date,academic_year_id, activity)
    }

    fun schoolHostelOutpassUpdateStatus(
        isToken: String,
        isSchoolHostelOutpassStatus: LeaveApproveRequest, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositoriesTwo.schoolHostelOutpassUpdateStatus(isToken, isSchoolHostelOutpassStatus,activity)
    }


    fun isGetParentHostelDetails(isToken: String,activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.getParentHostelDetails(isToken, activity)
    }


    fun isGetParentHostelDashboard(isToken: String,hostel_id : Int,year_id : Int,month_id  : Int,country_id   : String,activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isGetParentHostelDashboard(isToken,hostel_id,year_id,month_id,country_id,activity)
    }


    fun applyHostelOutpass(
        isToken: String,
        jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isApplyHostelOutpass(isToken, jsonObject,activity)
    }


    fun isOnlinePayment(
        isToken: String,
        activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isAllPayment(isToken,activity)
    }

    fun isPaymentStatus(
        isToken: String,
        jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiParentRepositories.isPaymentStatus(isToken, jsonObject,activity)
    }

    fun isGetBusList(
        isToken: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isBusList(isToken,activity)
    }

    fun isLiveBus(
        isToken: String,journey_type: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isLiveBus(isToken,journey_type,activity)
    }

    fun isgetgeolocation(
        isToken: String, device_id: String,vehicle_id: String, route_id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiParentRepositories.isgetgeolocation(isToken,device_id,vehicle_id,route_id,activity)
    }

    fun isSectionwisesubjectsdetail(
        isToken: String, section_ids: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isSectionwisesubjectsdetail(isToken,section_ids,activity)

    }

    fun isClassTestStudent(
        isToken: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isClassTesFortStudent(isToken,activity)

    }

    fun isViewClassTestStudent(
        isToken: String, isExamId: String, activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isViewClassTesFortStudent(isToken,isExamId,activity)

    }

    fun isCreateClasstest(
        isToken: String,
        jsonArray: JsonArray, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositoriesTwo.isCreateClasstest(isToken, jsonArray)
    }

    fun isgetExamreportdetails(
        isToken: String, class_test_id: String, exam_date: String,academic_year_id: String,activity: Activity) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isgetExamreportdetails(isToken,class_test_id,exam_date,academic_year_id,activity)

    }
    fun isputExamDelete(
        isToken: String, jsonObject: JsonObject,activity : Activity) {
        val reporting_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isputExamDelete(isToken,jsonObject,activity)

    }

    fun isputClassTestDelete(
        isToken: String, jsonObject: JsonObject,activity : Activity) {
        val reporting_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isputClassTestDelete(isToken,jsonObject,activity)

    }

    fun isexamDetailsMark(
        isToken: String, class_test_id: String,section_id : String,class_test_subject_id: String,activity : Activity) {
        val reporting_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isexamDetailsMark(isToken,class_test_id,section_id,class_test_subject_id,activity)

    }


    fun isexamdetailsmarkpost(
        isToken: String,
        jsonObject: JsonObject, activity: Activity
    ) {
        val base_url = SharedPreference.getBaseUrl(activity)
        RestClient.changeApiBaseUrl(base_url!!)
        apiSchoolRepositoriesTwo.isexamdetailsmarkpost(isToken, jsonObject)
    }


    fun isExamtestAnalysis(
        isToken: String,
        class_test_id: String,analysis_set_id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        Log.d("reporting_url",reporting_url.toString())
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isExamtestAnalysis(isToken, class_test_id,analysis_set_id,activity)
    }

    fun isExamtestAnalysisSets(
        isToken: String,
        class_id: String,section_id: String, activity: Activity
    ) {
        val reporting_url = SharedPreference.getReportingUrl(activity)
        Log.d("reporting_url",reporting_url.toString())
        RestClient.changeApiBaseUrl(reporting_url!!)
        apiSchoolRepositoriesTwo.isExamtestAnalysisSets(isToken, class_id,section_id,activity)
    }
}



