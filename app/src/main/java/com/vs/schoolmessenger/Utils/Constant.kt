package com.vs.schoolmessenger.Utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.Country
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationData
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpData
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.GlobalVariableData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuCountDetail
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateListData
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.StaffDataSending
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendingData
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceSendingData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionDataSending
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.School.PTM.Activity.PTM
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

object Constant {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    var isDeviceType = "Android"
    var isVersionId = 93
    var terms_condition = "https://schoolchimes.com/vs_web/terms_conditions/"
    var isShimmerViewShow = true
    var isShimmerViewDisable = false
    var handler = Handler(Looper.getMainLooper())
    val delayTime = 1500
    var scrollX = 0
    var isSyncing = false
    var isParentChoose = false
    var country_details: Country? = null
    var user_details: UserDetails? = null
    var user_data: List<UserValidationData>? = null
    var isStaffDetails: List<StaffDetails>? = null
    var isAwsUploadedFiles: MutableList<AwsUploadedFiles> = mutableListOf()
    var isChildDetails: List<ChildDetails>? = null
    var isPasswordCreation: Boolean? = false
    var forgotData: List<ForgetOtpData>? = null
    var isForgotPassword: Boolean? = false
    var isMobileNumber: String? = ""
    var selectedFiles: MutableList<FileItem> = mutableListOf()
    var SplashScreen: Int? = 1
    var MobileNumberScreen: Int? = 2
    var SignInScreen: Int? = 3
    var PasswordScreen: Int? = 4
    var pageType: Int? = 0
    var isGroupHeadRole: String? = "p1"
    var isPrincipalRole: String? = "p2"
    var isStaffRole: String? = "p3"
    var isAdminRole: String? = "p4"
    var isNonTeachingStaffRole: String? = "p5"
    val M_ABSENTEES_REPORT = 1
    val M_ASSIGNMENT = 2

    val M_ATTENDANCE_MARKING = 3
    val M_ATTENDANCE_REPORT = 4
    val M_CERTIFICATE_REQUEST = 5
    val M_CLASS_TIME_TABLE = 6
    val M_COMMUNICATION = 7
    val M_DAILY_COLLECTION = 8
    val M_EXAM = 10
    val M_FEEDBACK = 11
    val M_FEE_DETAILS = 12
    val M_FEE_PENDING_REPORT = 14
    val M_HOMEWORK = 15
    val M_INTERACTION_WITH_STAFF = 16
    val M_INTERACTION_WITH_STUDENT = 17
    val M_LEAVE_REQUEST = 18
    val M_PARENT_LEAVE_REQUEST = 28
    val M_LESSON_PLAN = 19
    val M_LSRW = 20
    val M_MARK_YOUR_ATTENDANCE = 21
    val M_MESSAGES_FROM_MANAGEMENT = 22
    val M_NOTICEBOARD = 23
    val M_UPLOAD_MARKS = 41
    val M_ONLINE_MEETING = 24
    val M_ONLINE_TEXT_BOOK = 25
    val M_PTM = 26
    val M_QUIZ_EXAM = 27
    val M_SCHOOL_CLASS_EVENTS = 29
    val M_PARENT_CLASS_EVENTS = 9
    val M_SCHOOL_NEEDS = 30
    val M_SCHOOL_STRENGTH = 31
    val M_STAFF_WISE_ATTENDANCE_REPORT = 33
    val M_STUDENT_REPORT = 35
    val M_VERY_IMPORTANT_INFO = 36
    val M_SCHEDULE_EXAM_TEST = 38
    val M_ATTACHMENTS = 39
    val M_FINANCE = 194

    val M_COUPON_PACKET = 40

    var SELECTED_MENU_ID = 0

    var isEmergencyVoiceNoticeBoard: Boolean? = false
    var isAccessType: Int? = null

    var isNonEmergency = 100
    var isEmergency = 101
    var isFileLimit = 0

    var isSchool = 1
    var isStandard = 2
    var isSection = 3
    var isGroup = 4
    var isStudent = 5
    var isStaff = 6

    //from notification intent values

    var menu_name = "menu_name"
    var headerId = "header_id"
    var menu_id = "menu_id"
    var msg_id = "msg_id"
    var msg_info = "msg_info"
    var header_id = "header_id"
    var SplashScreen__ = "SplashScreen"
    var categories = "categories"
    var rating = "rating"
    var institute_id = "institute_id"
    var receiver_type = "receiver_type"
    var receiverid = "receiver_id"
    var fromNotification = "fromNotification"

    var school = "A"
    var Late = "P~"
    var OD = "OD"
    var P = "P"
    var standard = "C"
    var section = "S"
    var group = "G"
    var student = "student"
    var staff = "staff"
    var Staff___ = "staff"
    var isVoiceSendingData: VoiceSendingData? = null
    var isTextSendingData: TextSendingData? = null
    var commonFileList: MutableList<CommonFileData> = mutableListOf()
    var selectedFileIndex: Int = -1
    var isCommunicationType = 1
    var isVoiceType = 1
    var isQuestionLimit = -1
    var isClickEdit = false

    var mediaPlayer: MediaPlayer = MediaPlayer()


//    var MAX_FILES = 10

    var isAcademicYearList: List<AcademicYear>? = null

    // var isParentMenuName = ""
    var isSelectedMenuName = ""
    var isSchoolMenuCount = -1

    var isCompletedHomeworkId: String? = null


//    var isForward = false
    //MarkAttendanceDetails

    var isMarkAttendanceDataSending: MarkAttendanceDataSending? = null
    var isLeaveData: LeaveData? = null
    var isCertificateData: CertificateListData? = null

    //    var isMarkUploadClassSectionDetails: ClassSectionData? = null
    var isMarkUploadClassSectionDetails: StandardSection? = null
    var isMarkUploadExamListDataDetails: getStaffWisExamData? = null


    var StaffDataSending: StaffDataSending? = null
    var QuestionDataSending: QuestionDataSending? = null
    var isAbsenteesReportDataSending: ClassWise? = null

    var isParentDashBoardData: List<DashboardData>? = null
    var isSchoolDashBoardData: List<DashboardData>? = null

    var isParentContactDetails: ContactDetails? = null
    var isParentMenuDetails: List<MenuDetail>? = null
    var isParentMenuCountDetails: ArrayList<MenuCountDetail>? = null
    var FrequentParentlyUsedMenuItems: List<MenuDetail>? = null
    var isParentAdItem: List<AdItem>? = null

    var isSchoolContactDetails: ContactDetails? = null
    var isSchoolMenuDetails: List<MenuDetail>? = null
    var isSchoolMenuCountDetails: ArrayList<MenuCountDetail>? = null
    var FrequentSchoollyUsedMenuItems: List<MenuDetail>? = null
    var isSchoolAdItem: List<AdItem>? = null


    var menuNameList: MutableList<String> = mutableListOf()
    fun setMenuNames(allMenuItems: List<MenuDetail>?) {
        menuNameList.clear()
        menuNameList.add("Select the menu")

        allMenuItems?.forEach { menu ->
            menuNameList.add(menu.name)
        }
    }


    var secondHalf = "SH"
    var firstHalf = "FH"
    var fullDay = "F"
    var allPresent = "T"
    var some_Absent = "F"
    var Absent = "Absent"
    var halfDay = "H"
    var This_day_is_marked_as_a_holiday = "This day is marked as a holiday."
    var Attendance_has_not_been_taken_yet = "Attendance has not been taken yet."
    var approved = "Approved"
    var rejected = "Rejected"
    var waiting_for_approval = "Waiting for approval"
    var in_review = "In review"


    // String fields
    var scaleX = "scaleX"
    var scaleY = "scaleY"
    var AM = "AM"
    var PM = "PM"
    var dd_MM_yyyy = "dd/MM/yyyy"
    var EEE_dd_MMM_yyyy = "EEE dd MMM, yyyy"
    var yyyy_MMM_dd = "yyyy MMM, dd"
    var dd_MMM_yyyy_1 = "dd MMM yyyy"
    var dd_MMM_yyyy_2 = "EEE, MMM yyyy"
    var hh_mm_a = "hh:mm a"
    var time_forMate = "00:%02d"
    var time_zero = "00:00"
    var isMailSend = """
    Dear School Chimes Team,

    Please configure communication academic year  as 20xx - 20xx for any queries contact.
    
    Your name :
    Mobile No :
    
""".trimIndent()
    var isMailTitle = "Request to configure communication academic year"
    var isAcademicYearId = "isAcademicYearId"
    var isSectionId = "isSectionId"
    var isStandardName = "isStandardName"
    var isSectionName = "isSectionName"
    var isCurrentAcademicYear = "isCurrentAcademicYear"
    var lblAcademicYear = "lblAcademicYear"
    var isSelectedId = "isSelectedId"
    var section_data = "section_data"
    var create_quiz_exam_data = "create_quiz_exam_data"
    var create_quiz_exam_data_add_now = "create_quiz_exam_data_add_now"
    var edit_quiz_exam_data = "edit_quiz_exam_data"
    var notice_data = "notice_data"
    var message = "message"
    var emergency_voice = "emergency_voice"
    var mysubmission_data = "mysubmission_data"
    var event_data = "event_data"
    var assignment_data = "assignment_data"
    var homework_data = "homework_data"
    var attachment_data = "attachment_data"
    var lsrwskill_data = "lsrwskill_data"
    var lsrwsubmitskill_data = "lsrwsubmitskill_data"
    var isFileUrl = "isFileUrl"
    var isFileType = "isFileType"
    var type_ = "type"
    var isVoiceUrlNotifi = "voice_url"
    var isWelcomeUrlNotifi = "welcome_url"
    var isTitle = "isTitle"
    var imageurl = "image_url"
    var isWebLink = "isWebLink"
    var parent = "parent"
    var staff_ = "staff"
    var en = "en"
    var Ok = "Ok"
    var Cancel = "Cancel"
    var ta = "ta"
    var th = "th"
    var hi = "hi"
    var ar = "ar"
    var Gallery = "Gallery"
    var Images = "Images"
    var images_ = "images"
    var image_ = "image"
    var ATTACHMENT = "ATTACHMENT"
    var ALL = "ALL"
    var fromArchive = "fromArchive"
    var UNREAD = "UNREAD"
    var READ = "READ"
    var VOICE = "VOICE"
    var AUDIO = "AUDIO"
    var VOICERECORD = "VOICERECORD"
    var VIDEO = "VIDEO"
    var DOCUMENT = "DOCUMENT"
    var document_ = "document"
    var VOICE_UNREAD = "VOICE_UNREAD"
    var VOICE_READ = "VOICE_READ"
    var TEXT = "TEXT"
    var TEXT_UNREAD = "TEXT_UNREAD"
    var TEXT_READ = "TEXT_READ"
    var TEXT_ALL = "TEXT_ALL"
    var VOICE_ALL = "VOICE_ALL"
    var dateForMate = "%02d:%02d"
    var isSelectedFiles = "isSelectedFiles"
    var Remaining = 10

    var IMAGE = "IMAGE"
    var M4A = "M4A"
    var PDF = "PDF"
    var XLS = "PDF"
    var DOC = "DOC"
    var DOCX = "DOCX"
    var PPT = "PPT"
    var XLSX = "PPT"
    var PPTX = "PPTX"
    var TXT = "TXT"
    var TET2 = "TEXT"
    var EXCEL = "EXCEL"
    var data = "data"
    var MGMT_MSG_TEXT = "MGMT_MSG_TEXT"
    var MGMT_MSG_VOICE = "MGMT_MSG_VOICE"
    var MGMT_MSG_ATTACHMENT = "MGMT_MSG_ATTACHMENT"
    var position = "position"
    var isAccessToken = "isAccessToken"
    var subjectName = "subjectName"
    var isText = "isText"
    var isVoice = "isVoice"
    var isVideo = "isVideo"
    var isPDF_ = "isPDF"
    var isImage = "isImage"
    var ddMMyyyy = "dd-MM-yyyy"
    var content = "content://"
    var file = "file://"
    var http = "http"
    var packagename = "package"
    var timeForMateWithAMPM = "%02d:%02d %s"
    var yyyyMMdd_HHmmss = "yyyyMMdd_HHmmss"
    var image_star = "image/*"
    var Select_images = "Select up to 5 images"
    var unknown_ = "unknown"
    var tel = "tel:"
    var mailto = "mailto:"
    var sms = "sms:"
    var manufacturer = "manufacturer"
    var model = "model"
    var device = "device"
    var brand = "brand"
    var hardware = "hardware"
    var product = "product"
    var os_version = "os_version"
    var sdk_int = "sdk_int"
    var app_version = "app_version"
    var Unknown = "Unknown"
    var dd_MMM_yyyy = "dd MMM yyyy"
    var MMMM = "MMMM"
    var EEEE = "EEEE"
    var MMMM_yyyy = "MMMM yyyy"
    var Location_1 = "Location 1"
    var Location_2 = "Location 2"
    var hasCode = "#.##"
    var VIDEO_URL = "VIDEO_URL"
    var VIDEO_TITLE = "VIDEO_TITLE"
    var isDelete = "isDelete"
    var isEdit = "isEdit"
    var role = "role"
    var menuId = "menuId"
    var welcome = "welcome"
    var school_name = "school_name"
    var member_name = "member_name"
    var NOTIFICATION_DISMISSED = "NOTIFICATION_DISMISSED"
    var Custom_Notifications = "Custom Notifications"
    var Channel_for_custom_notifications = "Channel for custom notifications"
    var Failed_to_download_image = "Failed to download image:"
    var call_title = "call_title"
    var Present = "Present"
    var googleMap = "com.google.android.apps.maps"
    var Youneedmore = "You need more points! Use the app to keep earning points"
    var Custom = "Custom"
    var isRemove = "isRemove"
    var isUpdate = "isUpdate"
    var new = "new"
    var Communication_ = "Communication_"
    var current = "current"
    var wav = ".wav"
    var wav_ = "wav"
    var isCurrentAcademicYearId = 0

    var GET_ALL_STUDENT = "GET ALL STUDENT"
    var ALL_STUDENTS = "All students"
    var STANDARD = "STANDARD"
    var STANDARD_AND_SECTION = "STANDARD AND SECTION"
    var CLASS_AND_SECTION = "Class & Section"

    var NO_DATA_FOUND = "No Data Found"
    var No_STANDARD_FOUND = "No Standard Found"

    var isCommonTitle = ""
    var isCommonDescription = ""
    var assignment_id = "assignment_id"
    var assignmentsubject = "assignmentsubject"
    var title_ = "title"
    var body_ = "body"
    var tone_ = "tone"
    var description = "description"
    var subject = "subject"
    var file_ = "file"
    var content_ = "content"
    var IMG_ = "IMG_"
    var jpg = ".jpg"
    var quiz = "quiz"
    var dd_MM_yyyy_hh_mm_ss_a = "dd-MM-yyyy hh:mm:ss a"
    var dd_MMM = "dd MMM"
    var isPreViewData = "isPreViewData"
    var MMM_ = "MMM"
    var dd = "dd"
    var day = "day"
    var weekday = "weekday"
    var monthYear = "monthYear"
    var M = "M"
    var W = "W"
    var progress = "progress"
    var setForceShowIcon = "setForceShowIcon"
    var mPopup = "mPopup"
    var x = "x"
    var X_ = "X"
    var iffin = "-"
    var slash = "/"
    var google_embredded_url = "https://drive.google.com/viewerng/viewer?embedded=true&url="
    var yyyy_MM_dd = "yyyy-MM-dd"
    var activated = "activated"
    var claimed = "claimed"
    var expired = "expired"
    var All_ = "All"
    var All_Schools = "All_Schools"
    var all__ = "all"
    var st = "st"
    var nd = "nd"
    var rd = "rd"
    var ONGOING = "ONGOING"
    var COMPLETED = "COMPLETED"
    var UPCOMING = "UPCOMING"
    var exam_title = "exam_title"
    var exam_id = "exam_id"
    var google_g_view_embedded = "https://docs.google.com/gview?embedded=true&url="
    var online_fee_payment_link =
        "https://profile.schoolchimes.com/#/online-fee-payment/13601818/6063/app"
    var isStudentID = ":student_id"
    var isSchoolID = ":school_id"
    var TOTAL = "TOTAL"
    var Listening = "Listening"
    var Speaking = "Speaking"
    var Reading = "Reading"
    var Writing = "Writing"
    var isHomeWorkDate = "isHomeWorkDate"
    var underscore = "_"
    var EEE = "EEE"
    var HOMEWORK = "HOMEWORK"
    var zero = "0"
    var zero__ = 0
    var Not_answered_yet = "Not answered yet"
    var dd_MM_yyyy_hh_mm_a = "dd-MM-yyyy hh:mm a"
    var reminder_channel = "reminder_channel"
    var Reminders = "Reminders"
    var isRSSubmittedQuizId = "isRSSubmittedQuizId"
    var isRSSubmittedSubject = "isRSSubmittedSubject"
    var isRSSubmittedSubmittedOn = "isRSSubmittedSubmittedOn"
    var N_A = "N/A"
    var isRSQuizId = "isRSQuizId"
    var one = "1"
    var two = "2"
    var three = "3"
    var time02d = "%02d"
    var STUDENT_ = "STUDENT"
    var Student__ = "student"
    var isReason = "isReason"
    var isId = "isId"
    var isLeaveTo = "isLeaveTo"
    var isLeaveFrom = "isLeaveFrom"
    var isFromSession = "isFromSession"
    var isToSession = "isToSession"
    var isLeaveType = "isLeaveType"
    var isLeaveTypeID = "isLeaveTypeID"
    var isRequestEdit = "isRequestEdit"
    var Select_a_leave_type = "Select a leave type"
    var FROM_DATE = "FROM_DATE"
    var TO_DATE = "TO_DATE"
    var EEE_comma_dd_MMM_yyyy = "EEE, dd MMM yyyy"
    var First_Half = "First Half"
    var Sunday = "Sunday"
    var Monday = "Monday"
    var Tuesday = "Tuesday"
    var Wednesday = "Wednesday"
    var Thursday = "Thursday"
    var Friday = "Friday"
    var Saturday = "Saturday"
    var EEE_comma_dd_MMM_yy = "EEE, dd MMM yy"
    var Mon = "Mon"
    var Tue = "Tue"
    var Wed = "Wed"
    var Thu = "Thu"
    var Fri = "Fri"
    var Sat = "Sat"
    var Sun = "Sun"
    var HH_mm = "HH:mm"
    var lat = "lat"
    var lng = "lng"
    var Update_Event = "Update Event"
    var type = "type"
    var SUBMITTED = "SUBMITTED"
    var NOTSUBMITTED = "NOTSUBMITTED"
    var submitted_count = "submitted_count"
    var Total_Count = "Total_Count"
    var submission_list = "submission_list"
    var created_date = "created_date"
    var sss_ = "sss_"
    var _00_30 = "00:30"
    var _03_00 = "03:00"
    var _00_00_03_00 = "00:00 / 03:00"
    var _00_00_00_30 = "00:00 / 00:30"
    var _02d__02d_s = "%02d:%02d / %s"
    var Unknown_view_type = "Unknown view type"
    var google_map_url = "https://www.google.com/maps?q="
    var STAFF__ = "STAFF"
    var section_subject_id = "section_subject_id"
    var particular_id = "particular_id"
    var request_type = "request_type"
    var subject_name = "subject_name"
    var items_completed = "items_completed"
    var completed_items = "completed_items"
    var total_items = "total_items"
    var field_id = "field_id"
    var value = "value"
    var dropdown = "dropdown"
    var text_ = "text"
    var datepicker = "datepicker"
    var allclass = "allclass"
    var myclass = "myclass"
    var _0_0 = "0 / 0"
    var Activity = "Activity"
    var Topic = "Topic"
    var Month = "Month"
    var Admin_Remarks = "Admin Remarks"
    var From_Date = "From Date"
    var To_Date = "To Date"
    var Assesment = "Assesment"
    var Active_Tasks = "Active Tasks"
    var Avg_Performance = "Avg. Performance"
    var Completed_Tasks = "Completed Tasks"
    var TASK_LIST = "TASK_LIST"
    var COMPLETED_TASK_LIST = "COMPLETED_TASK_LIST"
    var Today_Submitted = "Today Submitted"
    var Week = "Week"
    var Class = "Class"
    var double_iffin = "--"
    var geo_ = "geo:"
    var camma = ","
    var questionQEqual = "?q="
    var leftBracket = "("
    var rightBracket = ")"
    var yyyyMMdd = "yyyyMMdd"
    var quiz_Id = "quiz_Id"
    var quiz_Title = "quiz_Title"
    var limitQuestion = "limitQuestion"
    var submittedCount = "submittedCount"
    var openToStudent = "openToStudent"
    var subjectID = "subjectID"

    var isQuizReportPage=false

    var isQuizQuestionPickCount = 0
    var category_name = "category_name"
    var category = "category"
    var name__ = "name"
    var selected__ = "selected"
    var discount = "discount"
    var address = "address"
    var merchant_name = "merchant_name"
    var source_link = "source_link"
    var coupon_status = "coupon_status"
    var merchant_logo = "merchant_logo"
    var earnedPoints = "earnedPoints"
    var spentPoints = "spentPoints"
    var remainingPoints = "remainingPoints"
    var pointspercoupon = "pointspercoupon"
    var user_type = "user_type"
    var coupon_link = "coupon_link"
    var coupon_id = "coupon_id"
    var School_Chimes = "School Chimes"
    var You_have_a_new_message_from_your_school = "You have a new message from your school"
    var mobile = "mobile"
    var id_ = "id"
    var mobile_number = "mobile_number"
    var number = "number"
    var calendar = "calendar"
    var gender = "gender"
    var male = "male"
    var female = "female"
    var others = "others"
    var Others2 = "Others"
    var offer_to_show = "offer_to_show"
    var how_to_use = "how_to_use"
    var coupon_code = "coupon_code"
    var cover_image = "cover_image"
    var expiry_date = "expiry_date"
    var expiry_type = "expiry_type"
    var thumbnail = "thumbnail"
    var normal = "normal"
    var isCall = "isCall"
    var isNotificationId = "isNotificationId"
    var isVoiceUrl = "isVoiceUrl"
    var isReceiverId = "isReceiverId"
    var retrycount = "retrycount"
    var circularId = "circularId"
    var ei1 = "ei1"
    var ei2 = "ei2"
    var ei3 = "ei3"
    var ei4 = "ei4"
    var ei5 = "ei5"
    var Default = "Default"
    var location_list = "location_list"
    var qr_code = "qr_code"
    var offer = "offer"
    var redirect_url = "redirect_url"
    var isCTAvalid = "isCTAvalid"
    var CTAname = "CTAname"
    var CTAredirect = "CTAredirect"
    var Terms_and_Conditions = "Terms and Conditions"
    var offer_show = "offer_show"


    // VIMEO
    var isVimeoToken = "8d74d8bf6b5742d39971cc7d3ffbb51a"
    var isVimeoUrl = "https://api.vimeo.com/me/videos"
    const val Content_Type = "Content-Type"
    var POST = "POST"
    var application_json = "application/json"
    var Accept = "Accept"
    var application_vimeo_jsonversion = "application/vnd.vimeo.*+json;version=3.4"
    var approach = "approach"
    var tus = "tus"
    var size = "size"
    var upload = "upload"
    var view = "view"
    var attachment = "Attachment"
    var ATTACHMENT_ = "ATTACHMENT"
    var unlisted = "unlisted"
    var download = "download"
    var privacy = "privacy"
    var videoTitle = "videoTitle"
    var videoDesc = "videoDesc"
    var HEAD = "HEAD"
    var HETus_ResumableAD = "Tus-Resumable"
    var HETus_ResumableAD_Version = "1.0.0"
    var Upload_Offset = "Upload-Offset"
    var PATCH = "PATCH"

    var isGlobalVariableData: GlobalVariableData? = null

    var application_offset_octet_stream = "application/offset+octet-stream"
    var mimeTypes = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain"
    )


    // add  reward points types
    var add_points_login = "LOGIN"
    var add_points_homework = "HOMEWORK"
    var add_points_listen_voice = "LISTEN_VOICE"
    var add_points_read_message = "READ_MESSAGE"
    var add_points_submit_assignment = "SUBMIT_ASSIGNMENT"
    var add_points_pay_fees = "PAY_ONLINE_FEES"
    var add_points_view_exam_schedule = "VIEW_EXAM_SCHUDLE"
    var add_points_view_exam_mark = "VIEW_EXAM_MARK"
    var add_points_view_progress_card = "VIEW_PROGRESS_CARD"
    var add_points_apply_leave = "APPLY_LEAVE"
    var add_points_update_profile = "UPDATE_PROFILE"
    var add_points_view_videos = "VIEW_VIDEOS"
    var add_points_view_image = "VIEW_IMAGE_PDF"
    var add_points_view_events = "VIEW_EVENTS"
    var add_points_view_noticeboard = "VIEW_NOTICE_BOARD"
    var add_points_view_holidays = "VIEW_HOLIDAYS"
    var add_points_view_attachments = "VIEW_ATTACHMENTS"
    var add_points_view_assignmnents = "VIEW_ASSIGNMENT"

    var add_points_send_voice = "SEND_VOICE"
    var add_points_send_text = "SEND_TEXT"
    var add_points_send_attachment = "SEND_ATTACHMENT"
    var add_points_send_homework = "SEND_HOMEWORK"
    var add_points_send_assignment = "SEND_ASSIGNMENT"
    var add_points_send_attendance = "SEND_ATTENDANCE"
    var add_points_edit_lesson_plan = "EDIT_LESSONPLAN"
    var add_points_mark_attendance = "MARK_ATTENDANCE"
    var add_points_send_ptm = "SEND_PTM"
    var user_type_as_parent = 1
    var user_type_as_staff = 2


    fun isInternetAvailable(activity: Activity): Boolean {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun setGridViewHeight(gridView: GridView, columns: Int) {
        val adapter = gridView.adapter ?: return
        if (columns <= 0) return

        val totalItems = adapter.count
        if (totalItems == 0) {
            gridView.layoutParams.height = 0
            gridView.requestLayout()
            return
        }

        // Calculate number of rows
        val rows = if (totalItems % columns == 0) {
            totalItems / columns
        } else {
            totalItems / columns + 1
        }

        var totalHeight = 0

        for (row in 0 until rows) {
            val index = row * columns // first item in each row
            val listItem = adapter.getView(index, null, gridView)

            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(gridView.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )

            totalHeight += listItem.measuredHeight
        }

        // Add spacing between rows
        totalHeight += gridView.verticalSpacing * (rows - 1)

        val params = gridView.layoutParams
        params.height = totalHeight
        gridView.layoutParams = params
        gridView.requestLayout()
    }

    fun redirectToDialPad(context: Context, contactNo: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(tel + contactNo) // Replace with the phone number
        context.startActivity(intent)
    }

    fun redirectToMail(context: Context, mail: String, sub: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(mailto) // Ensures only email apps handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mail)) // Recipient email address
            putExtra(Intent.EXTRA_SUBJECT, sub) // Subject
            putExtra(Intent.EXTRA_TEXT, body) // Email body
        }
        val emailApps = context.packageManager.queryIntentActivities(intent, 0)
        if (emailApps.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent)
            //   Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    fun redirectToMessage(context: Context, phoneNumber: String) {
        val smsUri = Uri.parse(sms + phoneNumber)

        val intent = Intent(Intent.ACTION_VIEW, smsUri).apply {
            putExtra("", "")
        }
        context.startActivity(intent)
    }

    fun editTextCounter(
        context: Context, editText: EditText, maxLength: Int, counterLabel: TextView
    ) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?, start: Int, count: Int, after: Int
            ) {
                // You can add logic here if needed
            }

            override fun onTextChanged(
                charSequence: CharSequence?, start: Int, before: Int, count: Int
            ) {
                // You can add logic here if needed
            }

            override fun afterTextChanged(editable: Editable?) {
                counterLabel.text = editable!!.length.toString() + " of " + maxLength.toString()
                if (editable != null && editable.length > maxLength) {
                    // Restrict to the max length by trimming the input
                    editable.delete(maxLength, editable.length)
                    // Optionally, show a Toast or error message
                }
            }
        })
    }

    fun executeAfterDelay(task: () -> Unit) {
        handler.postDelayed({
            task()
        }, delayTime.toLong())
    }

    fun stopDelay() {
        handler.removeCallbacksAndMessages(null)
    }

    fun loadWebView(context: Context, webView: WebView, url: String) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isScrollbarFadingEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading(context as Activity)
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                hideLoading(context as Activity)

            }

            override fun onPageFinished(view: WebView, url: String) {
                hideLoading(context as Activity)

            }
        }

        webView.loadUrl(url)

    }

    fun getVideoThumbnail(context: Activity, uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAndroidSecureId(activity: Activity): String {
        return Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "Empty"
    }

    fun errorAlert(activity: Activity, title: String, content: String) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.custom_error_alert, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        alertDialog.show()
        // Access views
        val titleText = dialogView.findViewById<TextView>(R.id.alertTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.alertMessage)
        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        messageText.text = content + " Please try again "
        titleText.text = if (!title.isNullOrBlank()) title else "Oops!"
        Log.d("titleText", titleText.text.toString())

        okButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun showErrorAlert(activity: Activity, title: String, content: String) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.show_error_alert, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        alertDialog.show()
        // Access views
        val titleText = dialogView.findViewById<TextView>(R.id.alertTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.alertMessage)
        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        messageText.text = content
        titleText.text = title
        okButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) == 1
    }

    fun setKeyboardListener(rootView: View, onKeyboardStateChanged: (Boolean) -> Unit) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            val isKeyboardOpened = keypadHeight > screenHeight * 0.15
            onKeyboardStateChanged(isKeyboardOpened)
        }
    }

    fun hideKeyboardIfOpen(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = activity.currentFocus

        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
            currentFocusView.clearFocus()
        }
    }


    fun isNavigation(activity: Activity) {
        val isUserDetails = SharedPreference.getUserDetails(activity)

        val isStaffRole = isUserDetails!!.staff_role
        var isMultipleSchool = false
        isMultipleSchool = isUserDetails.staff_details.size > 1
        if (isMultipleSchool) {
            if (isStaffRole.equals(isGroupHeadRole) || isStaffRole.equals(isPrincipalRole) || isStaffRole.equals(
                    isAdminRole
                )
            ) {
                val intent = Intent(activity, SchoolList::class.java)
                activity.startActivity(intent)
            } else {
                val intent = Intent(activity, RecipientActivity::class.java)
                activity.startActivity(intent)
            }
        } else {
            val intent = Intent(activity, RecipientActivity::class.java)
            activity.startActivity(intent)
        }
    }

    fun formatDatepostedby(dateStr: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

        val inputDate = inputFormat.parse(dateStr) ?: return dateStr
        return outputFormat.format(inputDate)
    }


    /**
     * Natural alphanumeric comparator that sorts mixed admission/roll numbers properly.
     * Handles:
     * - Numbers: 1, 12
     * - Alpha-number: A1, SS1, A-2
     * - Number-alpha: 12A, 7-B
     * - Mixed/symbols: SS-01, SS/1
     * - Empty or null values → sorted last
     */
    private fun naturalSortKey(value: String?): Pair<Int, List<Any>> {
        if (value.isNullOrBlank()) return Pair(4, emptyList())

        val trimmed = value.trim().replace(Regex("[^A-Za-z0-9]"), "") // remove -, /, spaces

        val category = when {
            Regex("^[0-9]+$").matches(trimmed) -> 0 // Pure number
            Regex("^[0-9]+[A-Za-z]+").matches(trimmed) -> 1 // Number-first
            Regex("^[A-Za-z]+[0-9]+").matches(trimmed) -> 2 // Alpha-first
            Regex("^[A-Za-z]+$").matches(trimmed) -> 3 // Pure alpha
            else -> 4 // invalid/mixed
        }

        val parts = Regex("(\\d+|[A-Za-z]+)").findAll(trimmed).map {
            it.value.toIntOrNull() ?: it.value.lowercase()
        }.toList()

        return Pair(category, parts)
    }

    val naturalComparator = Comparator<String?> { a, b ->
        val (catA, partsA) = naturalSortKey(a)
        val (catB, partsB) = naturalSortKey(b)

        if (catA != catB) return@Comparator catA.compareTo(catB)

        for (i in 0 until minOf(partsA.size, partsB.size)) {
            val pa = partsA[i]
            val pb = partsB[i]

            val result = when {
                pa is Int && pb is Int -> pa.compareTo(pb)
                pa is String && pb is String -> pa.compareTo(pb)
                pa is Int -> -1
                pb is Int -> 1
                else -> 0
            }

            if (result != 0) return@Comparator result
        }

        partsA.size.compareTo(partsB.size)
    }


    //"dd-MM-yyyy" to "dd MMMM, yyyy"
    fun formatDate(dateStr: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        val inputDate = inputFormat.parse(dateStr) ?: return dateStr

        Calendar.getInstance()

        // Today
        val today = Calendar.getInstance()

        // Yesterday
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        return when {
            isSameDay(calendar = today, date = inputDate) -> "Today"
            isSameDay(calendar = yesterday, date = inputDate) -> "Yesterday"
            else -> outputFormat.format(inputDate)
        }
    }

    //"dd-MM-yyyy" to "dd MMM yyyy"

    fun isFormatDate(dateStr: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val inputDate = inputFormat.parse(dateStr) ?: return dateStr

        Calendar.getInstance()

        // Today
        val today = Calendar.getInstance()

        // Yesterday
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        return when {
            isSameDay(calendar = today, date = inputDate) -> "Today"
            isSameDay(calendar = yesterday, date = inputDate) -> "Yesterday"
            else -> outputFormat.format(inputDate)
        }
    }

    fun getNameInitials(fullName: String): String {
        val prefixes = listOf("dr", "mr", "ms", "mrs", "miss")

        val parts = fullName
            .trim()
            .split("[\\s.]+".toRegex()) // split by space or dot
            .filter { part ->
                part.isNotEmpty() && !prefixes.contains(part.lowercase())
            }

        return when {
            parts.isEmpty() -> ""
            parts.size == 1 -> {
                // Single word → first two letters
                val word = parts[0]
                word.take(2).uppercase()
            }

            else -> {
                // Multiple words → still can use first letter of first + first letter of second (optional)
                val first = parts.first().first().uppercaseChar()
                val second = parts.first().drop(1).firstOrNull()?.uppercaseChar()
                    ?: parts.last().first().uppercaseChar()
                "$first$second"
            }
        }
    }


    private fun isSameDay(calendar: Calendar, date: Date): Boolean {
        val cal = Calendar.getInstance()
        cal.time = date
        return calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }


    fun showTopAlertPopup(message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            isAwsUploadedFiles.clear()
            selectedFiles.clear()
            isCommunicationType = 1
            val intent = Intent(activity, SchoolDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }

    fun showTopAlertPopup1(message: String, activity: Activity, isRedirect: Boolean) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
//            isAwsUploadedFiles.clear()
//            selectedFiles.clear()
//            isCommunicationType = 1

            if (isRedirect) {
                val intent = Intent(activity, PTM::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true

    }

    fun showDatePicker(
        context: Context, dateFormatType: Boolean, onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)

                onDateSelected(formattedDate)
            },
            year, month, day

        )
        if (dateFormatType) {
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        }
        datePickerDialog.show()
    }


    fun showDatePicker12(
        context: Context,
        dateFormatType: Boolean,
        defaultDate: Calendar? = null,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = defaultDate ?: Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis

        if (dateFormatType) {
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        }

        datePickerDialog.show()
    }

    fun DatePicker(
        context: Context,
        dateFormatType: Boolean,
        defaultDate: Calendar? = null,
        minDate: Long? = null,
        maxDate: Long? = null,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = defaultDate ?: Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = minDate ?: Calendar.getInstance().timeInMillis

        maxDate?.let {
            datePickerDialog.datePicker.maxDate = it
        }

        if (dateFormatType) {
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        }

        datePickerDialog.show()
    }


    fun covertDate(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input
        }
    }


    fun handleRestrictDatePicker(
        context: Context,
        minDate: Long? = null,
        preSelectedDateMillis: Long? = null,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        if (preSelectedDateMillis != null) {
            calendar.timeInMillis = preSelectedDateMillis
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            year, month, day
        )
        minDate?.let {
            datePickerDialog.datePicker.minDate = it
        }
        datePickerDialog.show()
    }


    fun covertDateFormate(input: String): String {
        Log.d("Input Date Format", input)
        return try {
            val inputFormat = SimpleDateFormat(dd_MM_yyyy, Locale.getDefault())
            val outputFormat = SimpleDateFormat(dd_MMM_yyyy, Locale.getDefault())
            val date = inputFormat.parse(input)
            Log.d("OutPut Date Format", date.toString())
            outputFormat.format(date!!)

        } catch (e: Exception) {
            input
        }
    }


    fun getFileSizeInMB(context: Context, filePath: String): String {
        return try {
            val sizeBytes: Long = if (filePath.startsWith("content://")) {
                val uri = Uri.parse(filePath)
                context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
            } else {
                val file = File(filePath)
                if (file.exists()) file.length() else 0
            }

            val megabytes = sizeBytes / 1024.0 / 1024.0
            String.format("%.2f MB", megabytes)
        } catch (e: Exception) {
            e.printStackTrace()
            "0.00 MB"
        }
    }


    fun showDataValidation(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            val intent = Intent(activity, SchoolDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
            closePopup()
        }
    }

    fun showParentDataValidation(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            val intent = Intent(activity, ParentDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
            closePopup()
        }
    }


    fun showRedirecttoMenu(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            val intent = Intent(activity, LeaveRequest::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
            closePopup()
        }
    }


    fun showDataValidationNoDashboardRedirect(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            closePopup()
        }
    }


    fun showValidationAlertPopup(isTitle: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        val title = view.findViewById<TextView>(R.id.alertTitle)

        messageText.text = message
        title.text = isTitle

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            closePopup()
        }

        dimView.setOnClickListener {
            closePopup()
        }
    }


    fun showSendConfirmationDialog(
        activity: Activity,
        istitle: String,
        Ok: String,
        Cancel: String,
        isSelectTarget: String,
        isMessage: String,
        onResult: (Boolean) -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.alert_popup, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val lblalertTitle = dialogView.findViewById<TextView>(R.id.alertTitle)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)

        alertMessage.text = isMessage
        okButton.text = Ok
        lblalertTitle.text = istitle
        btnCancel.text = Cancel
        lblSelectTarget.text = isSelectTarget

        if (isSelectTarget.isEmpty()) {
            lblSelectTarget.visibility = View.GONE
        }

        okButton.setOnClickListener {
            alertDialog.dismiss()
            onResult(true)
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
            onResult(false)
        }
    }



    fun showSendConfirmation(
        activity: Activity,
        istitle: String,
        Ok: String,
        Cancel: String,
        isMessage: String,
        onResult: (Boolean) -> Unit
    ) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.manually_entry_alert, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val lblalertTitle = dialogView.findViewById<TextView>(R.id.alertTitle)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)

        alertMessage.text = isMessage
        okButton.text = Ok
        lblalertTitle.text = istitle
        btnCancel.text = Cancel


        okButton.setOnClickListener {
            alertDialog.dismiss()
            onResult(true)
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
            onResult(false)
        }
    }


    fun getAudioDurationInSeconds(url: String): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(url, HashMap()) // For network sources, use empty headers map
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            ceil(durationMs / 1000.0).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        } finally {
            retriever.release()
        }
    }


    fun getAudioDurationInMinutes(url: String): String {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(url, HashMap()) // For network sources, use empty headers map
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            e.printStackTrace()
            "00:00"
        } finally {
            retriever.release()
        }
    }


    fun getDeviceDetails(context: Activity): JsonObject {
        val json = JsonObject()
        json.addProperty("manufacturer", Build.MANUFACTURER)
        json.addProperty("model", Build.MODEL)
        json.addProperty("device", Build.DEVICE)
        json.addProperty("brand", Build.BRAND)
        json.addProperty("hardware", Build.HARDWARE)
        json.addProperty("product", Build.PRODUCT)
        json.addProperty("os_version", Build.VERSION.RELEASE)
        json.addProperty("sdk_int", Build.VERSION.SDK_INT)
        json.addProperty("app_version", getAppVersion(context))
        return json
    }


    fun getAppVersion(context: Activity): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: Unknown
        } catch (e: Exception) {
            Unknown
        }
    }

    fun getCurrentTime(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern(hh_mm_a)
        return currentTime.format(formatter)
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern(ddMMyyyy)
        return currentDate.format(formatter)
    }

    //We use this to convert the Date Format 12 May 2025 to 12 Mon(we get Date And Day)
    fun getDayAndDateOnly(inputDateStr: String): Pair<String, String> {
        return try {
            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault()) // e.g., Fri
            val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault()) // e.g., 13

            val date = inputFormat.parse(inputDateStr)!!
            val dayName = dayNameFormat.format(date)     // "Fri"
            val dayNumber = dayNumberFormat.format(date) // "13"

            Pair(dayNumber, dayName)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("", "") // fallback
        }
    }

    //We use this to convert the Date Format 12 May 2025 to 12 Monday(we get Date And Day)
    fun getDayAndDateOnly2(inputDateStr: String): Pair<String, String> {
        return try {
            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) // force English
            val dayNameFormat =
                SimpleDateFormat("EEEE", Locale.getDefault())   // still localized day name
            val dayNumberFormat = SimpleDateFormat("dd", Locale.ENGLISH)        // day number

            val date = inputFormat.parse(inputDateStr)!!
            val dayName = dayNameFormat.format(date)     // localized day name
            val dayNumber = dayNumberFormat.format(date) // e.g., 13

            Pair(dayNumber, dayName)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("", "") // fallback
        }
    }

//    fun getDayAndDateOnly2(inputDateStr: String): Pair<String, String> {
//        return try {
//            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//            val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // e.g., Fri
//            val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault()) // e.g., 13
//
//            val date = inputFormat.parse(inputDateStr)!!
//            val dayName = dayNameFormat.format(date)     // "Fri"
//            val dayNumber = dayNumberFormat.format(date) // "13"
//
//            Pair(dayNumber, dayName)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Pair("", "") // fallback
//        }
//    }


    //Convert dd-MM-YYYY to dd MMM YYYY (12-02-2025 to 12 Feb 2025)

    @SuppressLint("SimpleDateFormat")
    fun convertToReadableDate(inputDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(inputDateStr)

            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val calendar = Calendar.getInstance().apply { time = date!! }

            val isYesterday = isSameDay(calendar, yesterday)

            if (isYesterday) {
                "Yesterday"
            } else {
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                outputFormat.format(date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            inputDateStr
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun convertToReadableDate1(inputDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(inputDateStr)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            inputDateStr
        }
    }

//    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
//    }

//    fun convertToReadableDate(inputDateStr: String): String {
//        return try {
//            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
//            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
//            val date = inputFormat.parse(inputDateStr)
//            outputFormat.format(date!!)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            inputDateStr
//        }
//    }


    // Convert dd-MM-yyyy hh:mm a("16-07-2025 04:24 PM" ) to dd, MMM yyyy hh:mm a ("16, Jul 2025 04:24 PM")

    fun convertDateFormatType(inputDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd, MMM yyyy hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(inputDateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            inputDateStr
        }
    }

    // Convert dd-MM-yyyy hh:mm a("16-07-2025 04:24 PM" ) to dd, MMM yyyy hh:mm a ("16 Jul 2025 04:24 PM")

    fun convertDateFormatType2(inputDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(inputDateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            inputDateStr
        }
    }

    // Convert dd-MM-yyyy hh:mm a("16-07-2025 04:24 PM" ) to MMMM yyyy (" July 2025 ")

    fun convertDateFormatType3(inputDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(inputDateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            inputDateStr
        }
    }


    //Convert dd-MM-yyyy hh:mm a to dd MMM yyyy (12-02-2025 10:58 AM to 12 Feb 2025)
    fun convertToReadableDateformat(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            inputDate
        }
    }


    fun getCurrentDateInfo(): List<String> {
        val calendar = android.icu.util.Calendar.getInstance()

        val dayOnly = String.format("%02d", calendar.get(android.icu.util.Calendar.DAY_OF_MONTH))
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
        val fullDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        val slashDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
        val customFormat =
            SimpleDateFormat("EEE, MMM yyyy", Locale.getDefault()).format(calendar.time)

        return listOf(dayOnly, dayOfWeek, fullDate, slashDate, customFormat)
    }

    fun getCurrentDateInfo2(): List<String> {
        val calendar = android.icu.util.Calendar.getInstance()

        val dayOnly = String.format("%02d", calendar.get(android.icu.util.Calendar.DAY_OF_MONTH))
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        val fullDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        val slashDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
        val customFormat =
            SimpleDateFormat("EEE, MMM yyyy", Locale.getDefault()).format(calendar.time)

        return listOf(dayOnly, dayOfWeek, fullDate, slashDate, customFormat)
    }


    fun convertDateFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat(dd_MMM_yyyy_1, Locale.getDefault())
            val outputFormat = SimpleDateFormat(ddMMyyyy, Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // return original if there's a parsing error
        }

    }

    fun convertDateFormat1(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat(dd_MMM_yyyy_2, Locale.getDefault())
            val outputFormat = SimpleDateFormat(ddMMyyyy, Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // return original if there's a parsing error
        }

    }


    fun formatChatDate(createdOn: String): String {
        if (createdOn.isBlank()) return ""

        val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
        val date: Date = try {
            inputFormat.parse(createdOn) ?: return createdOn
        } catch (e: ParseException) {
            return createdOn
        }

        val now = Calendar.getInstance()
        val messageCal = Calendar.getInstance().apply { time = date }

        return if (
            now.get(Calendar.YEAR) == messageCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == messageCal.get(Calendar.DAY_OF_YEAR)
        ) {
            // Same day → show only time
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        } else {
            // Different day → show both date + time
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
        }
    }


    fun CustomisedconvertDateTimeFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input
        }
    }


    fun CustomisedconvertDateAndTimeFormat(input: String?): String {
        if (input.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(input)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun formatCreatedDate(input: String?): String {
        if (input.isNullOrEmpty()) return "--"

        return try {
            val inputFormatFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val date = try {
                inputFormatFull.parse(input)
            } catch (e: Exception) {
                val inputFormatDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                inputFormatDate.parse(input)
            }

            date?.let { outputFormat.format(it) } ?: "--"
        } catch (e: Exception) {
            "--"
        }
    }


    // yyyy-MM-dd → dd/MM/yyyy
    fun formatToUi(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "--"
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            val localDate = LocalDate.parse(dateStr, inputFormatter)
            localDate.format(outputFormatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    // yyyy-MM-dd → dd-MM-yyyy
    fun formatToUi2(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "--"
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
            val localDate = LocalDate.parse(dateStr, inputFormatter)
            localDate.format(outputFormatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    // yyyy-MM-dd → Monday, October 12 2025
    fun formatToPretty(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "--"
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val outputFormatter =
                DateTimeFormatter.ofPattern("EEE, MMM dd yyyy", Locale.getDefault())
            val localDate = LocalDate.parse(dateStr, inputFormatter)
            localDate.format(outputFormatter)
        } catch (e: Exception) {
            dateStr
        }
    }


    fun convertSubmittedDateAssignment(input: String?): String {
        if (input.isNullOrEmpty()) return "--"

        return try {

            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())


            val outputFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

            val date = inputFormat.parse(input)
            date?.let { outputFormat.format(it) } ?: "--"
        } catch (e: Exception) {
            "--"
        }
    }


//    fun convertDateTimeFormat(input: String): String {
//        return try {
//            val inputFormat = SimpleDateFormat(ddMMyyyy, Locale.getDefault())
//            val outputFormat = SimpleDateFormat(dd_MMM_yyyy, Locale.getDefault())
//            val date = inputFormat.parse(input)
//            outputFormat.format(date!!)
//        } catch (e: Exception) {
//            input // return original if there's a parsing error
//        }
//
//    }

    fun convertDateAndTimeFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy h.mm a", Locale.getDefault())
            val date = inputFormat.parse(input)
            if (date != null) outputFormat.format(date) else input
        } catch (e: Exception) {
            input
        }
    }


    fun getTimeAfter20Minutes(): String {
        val dateFormat = SimpleDateFormat(hh_mm_a, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 20)
        return dateFormat.format(calendar.time)
    }

    fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun hideLoading(context: Activity) {
        val rootView = context.findViewById<ViewGroup>(android.R.id.content)
        val loader = rootView.findViewById<View>(R.id.loader_root)
        loader?.let { rootView.removeView(it) }
    }

    fun showLoading(context: Activity) {
        val rootView = context.findViewById<ViewGroup>(android.R.id.content)
        val loaderView =
            LayoutInflater.from(context).inflate(R.layout.lottie_loader, rootView, false)
        rootView.addView(loaderView)

    }

    fun getDateDetails(input: String): Triple<String, Int, String> {
        val sdf = SimpleDateFormat(ddMMyyyy, Locale.getDefault())
        val date = sdf.parse(input) ?: return Triple("", -1, "")

        val calendar = Calendar.getInstance().apply { time = date }

        val month = SimpleDateFormat(MMMM, Locale.getDefault()).format(date) // "April"
        val day = calendar.get(Calendar.DAY_OF_MONTH) // 29
        val dayOfWeek = SimpleDateFormat(EEEE, Locale.getDefault()).format(date) // "Tuesday"
        val shortDay = dayOfWeek.take(3) // First 3 characters


        return Triple(month, day, shortDay)
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    //Return the suffix of the Day like 11 means th,1 means st,2 means nd,3 means rd etc
    fun getDaySuffix(day: Int): String {
        return if (day in 11..13) "th" else when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    fun getCurrentDateDetails(): Map<String, String> {
        val calendar = Calendar.getInstance()

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedDay = if (day < 10) "0$day" else day.toString()

        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        return mapOf(
            "day" to formattedDay,
            "weekday" to dayOfWeek,
            "monthYear" to monthYear
        )
    }


    fun getVideoSizeInMB(videoPath: String): Long {
        val file = File(videoPath)
        return file.length() / (1024 * 1024)  // Convert bytes to MB
    }


    fun Context.safeOpenInputStream(path: String): InputStream? {
        return try {
            val uri = Uri.parse(path)
            when (uri.scheme) {
                "content" -> contentResolver.openInputStream(uri)
                "file" -> FileInputStream(File(uri.path!!))
                else -> FileInputStream(File(path))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun compressImageFilesOnly(
        context: Context,
        files: List<FileItem>,
        outputDir: String,
        format: Bitmap.CompressFormat,
        quality: Int,
        maxWidth: Int,
        maxHeight: Int,
        onEachProcessed: (original: FileItem, outputPath: String?, success: Boolean) -> Unit,
        onComplete: () -> Unit
    ) {
        Thread {
            val newList = mutableListOf<FileItem>()

            for (fileItem in files.toList()) {
                try {
                    val uri = Uri.parse(fileItem.path)
                    val mimeType = context.contentResolver.getType(uri)

                    val isImage = mimeType?.startsWith("image/") == true ||
                            fileItem.path.endsWith(".jpg", true) ||
                            fileItem.path.endsWith(".jpeg", true) ||
                            fileItem.path.endsWith(".png", true)

                    if (!isImage) {
                        onEachProcessed(fileItem, fileItem.path, true)
                        continue
                    }

                    // ✅ ONLY THIS LINE IS CHANGED
                    val inputStream = context.safeOpenInputStream(fileItem.path)

                    val bitmap = inputStream?.use {
                        BitmapFactory.decodeStream(it)
                    }

                    if (bitmap != null) {
                        val scaledBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

                        val compressedFile = File(
                            outputDir,
                            "IMG_${System.currentTimeMillis()}.jpg"
                        )

                        FileOutputStream(compressedFile).use { out ->
                            scaledBitmap.compress(format, quality, out)
                            out.flush()
                        }

                        onEachProcessed(fileItem, compressedFile.absolutePath, true)
                        newList.add(fileItem)
                    } else {
                        Log.e("Compressor", "❌ Failed to decode: ${fileItem.path}")
                        onEachProcessed(fileItem, null, false)
                    }

                } catch (e: Exception) {
                    Log.e("Compressor", "❌ Exception compressing ${fileItem.path}", e)
                    onEachProcessed(fileItem, null, false)
                }
            }

            Handler(Looper.getMainLooper()).post {
                onComplete()
            }
        }.start()
    }


//    fun compressImageFilesOnly(
//        context: Context,
//        files: List<FileItem>,
//        outputDir: String,
//        format: Bitmap.CompressFormat,
//        quality: Int,
//        maxWidth: Int,
//        maxHeight: Int,
//        onEachProcessed: (original: FileItem, outputPath: String?, success: Boolean) -> Unit,
//        onComplete: () -> Unit
//    ) {
//        Thread {
//            val newList = mutableListOf<FileItem>()
//
//            for (fileItem in files.toList()) {
//                try {
//                    val uri = Uri.parse(fileItem.path)
//                    val mimeType = context.contentResolver.getType(uri)
//
//                    val isImage = mimeType?.startsWith("image/") == true ||
//                            fileItem.path.endsWith(".jpg", true) ||
//                            fileItem.path.endsWith(".jpeg", true) ||
//                            fileItem.path.endsWith(".png", true)
//
//                    if (!isImage) {
//                        onEachProcessed(fileItem, fileItem.path, true)
//                        continue
//                    }
//
//                    val inputStream = context.contentResolver.openInputStream(uri)
//                    val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }
//
//                    if (bitmap != null) {
//                        val scaledBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)
//
//                        val compressedFile = File(
//                            outputDir,
//                            "IMG_${System.currentTimeMillis()}.jpg"
//                        )
//                        FileOutputStream(compressedFile).use { out ->
//                            scaledBitmap.compress(format, quality, out)
//                            out.flush()
//                        }
//
//                        onEachProcessed(fileItem, compressedFile.absolutePath, true)
//                        newList.add(fileItem)
//                    } else {
//                        Log.e("Compressor", "❌ Failed to decode: ${fileItem.path}")
//                        onEachProcessed(fileItem, null, false)
//                    }
//                } catch (e: Exception) {
//                    Log.e("Compressor", "❌ Exception compressing ${fileItem.path}", e)
//                    onEachProcessed(fileItem, null, false)
//                }
//            }
//
//            Handler(Looper.getMainLooper()).post {
//                onComplete()
//            }
//        }.start()
//    }


    fun quizCompressImageFilesOnly(
        context: Context,
        files: String,
        outputDir: String,
        format: Bitmap.CompressFormat,
        quality: Int,
        maxWidth: Int,
        maxHeight: Int,
        onEachProcessed: (outputPath: String?, success: Boolean) -> Unit,
        onComplete: () -> Unit
    ) {
        Thread {

//            for (fileItem in files.toList()) {
            try {
                val uri = Uri.parse(files)
                onEachProcessed(files, true)

                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }

                if (bitmap != null) {
                    val scaledBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

                    val compressedFile = File(
                        outputDir,
                        "IMG_${System.currentTimeMillis()}.jpg"
                    )
                    FileOutputStream(compressedFile).use { out ->
                        scaledBitmap.compress(format, quality, out)
                        out.flush()
                    }

                    onEachProcessed(compressedFile.absolutePath, true)
//                        newList.add(fileItem)
                } else {
//                        Log.e("Compressor", "❌ Failed to decode: ${fileItem.isUrl}")
                    onEachProcessed(null, false)
                }
            } catch (e: Exception) {
//                    Log.e("Compressor", "❌ Exception compressing ${fileItem.isUrl}", e)
                onEachProcessed(null, false)
            }
            //  }

            Handler(Looper.getMainLooper()).post {
                onComplete()
            }
        }.start()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio = width.toFloat() / height
        val targetWidth: Int
        val targetHeight: Int

        if (maxWidth / ratio <= maxHeight) {
            targetWidth = maxWidth
            targetHeight = (maxWidth / ratio).toInt()
        } else {
            targetHeight = maxHeight
            targetWidth = (maxHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }


    fun isDeveloperModeEnabled(context: Context): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) == 1
    }


    fun showNotificationPermissionDialog(
        packageName: String, activity: Activity, isTitle: String, isContent: String
    ) {
        AlertDialog.Builder(activity).setTitle(isTitle)
            .setMessage(isContent)
            .setPositiveButton("Go to Settings") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                activity.startActivity(intent)

            }.setCancelable(false).show()
    }

    fun checkBiometricSupport(activity: Activity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val fingerPrintEnabled = SharedPreference.isFingerprintEnabled(activity)
                Log.d("fingerPrintEnabled", fingerPrintEnabled.toString())
                if (!fingerPrintEnabled) {

                    val fingerPrintSkipped = SharedPreference.isFingerPrintSkipped(activity)
                    if (!fingerPrintSkipped) {
                        val dialogView = LayoutInflater.from(activity)
                            .inflate(R.layout.enable_fingerprint_popup, null)
                        val builder = AlertDialog.Builder(activity)
                        builder.setView(dialogView)
                        val alertDialog = builder.create()
                        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
                        alertDialog.show()
                        // Access views
                        val titleText = dialogView.findViewById<TextView>(R.id.alertTitle)
                        val messageText = dialogView.findViewById<TextView>(R.id.alertMessage)
                        val lblYes = dialogView.findViewById<TextView>(R.id.lblYes)
                        val lblNo = dialogView.findViewById<TextView>(R.id.lblNo)
                        messageText.text =
                            "Would you like to enable fingerprint authentication for faster and secure access in the future?"
                        titleText.text = "Enable Fingerprint Login?"

                        lblYes.setOnClickListener {
                            SharedPreference.setFingerprintEnabled(activity, true)
                            alertDialog.dismiss()
                        }
                        lblNo.setOnClickListener {
                            SharedPreference.setFingerPrintSkipped(activity, true)
                            SharedPreference.setFingerprintEnabled(activity, false)
                            alertDialog.dismiss()
                        }
                    }


                }
                true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                SharedPreference.setFingerprintEnabled(activity, false)
//                Toast.makeText(activity, "No biometric features available on this device.", Toast.LENGTH_LONG).show()
                false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                SharedPreference.setFingerprintEnabled(activity, false)
//                Toast.makeText(activity, "Biometric features are currently unavailable.", Toast.LENGTH_LONG).show()
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                SharedPreference.setFingerprintEnabled(activity, false)
                false
            }

            else -> false
        }
    }

    fun authenticate(activity: Activity) {
        biometricPrompt.authenticate(promptInfo)
    }

    fun setupBiometricPrompt(
        activity: FragmentActivity,
        listener: fingerPrintAunthenticateListener,
        isSplash: Boolean
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        biometricPrompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    listener.onAuthenticate("Authentication succeeded!", true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    listener.onAuthenticate("Authentication error: $errString", false)
                    Log.d("errorCodeValue", errorCode.toString())
                    when (errorCode) {
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            // Automatically redirect to passcode screen
                        }

                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            val builder = AlertDialog.Builder(activity)
                                .setTitle("School Chimes is locked")
                                .setMessage("Authentication is required to access the School Chimes")
                                .setCancelable(false) // optional, prevents closing by tapping outside

                            val dialog = builder.create()
                            dialog.setOnShowListener {
                                // "Unlock now" button
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
                                    authenticate(activity)
                                }
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                                    if (isSplash) {
                                        val intent = Intent(activity, Login::class.java)
                                        activity.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        dialog.cancel() // ✅ safely cancels here
                                    }
                                }
                            }
                            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Unlock now") { _, _ -> }
                            dialog.setButton(
                                AlertDialog.BUTTON_POSITIVE,
                                "Proceed with credentials"
                            ) { _, _ -> }

                            dialog.show()
                        }

                        else -> {
                            Toast.makeText(
                                activity,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    listener.onAuthenticate("Authentication failed", false)
                }
            })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setSubtitle("Use your fingerprint to access the app")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
//            .setNegativeButtonText("Cancel")
                .build()
        } else {
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setSubtitle("Use your fingerprint to access the app")
                .setDeviceCredentialAllowed(true)
//            .setNegativeButtonText("Cancel")
                .build()
        }
    }

    fun formatDateSmart(dateStr: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

        val inputDate: Date = inputFormat.parse(dateStr) ?: return dateStr

        val calendarInput = Calendar.getInstance().apply { time = inputDate }
        val calendarToday = Calendar.getInstance()
        val calendarYesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        return when {
            isSameDay(calendarInput, calendarToday) -> "Today"
            isSameDay(calendarInput, calendarYesterday) -> "Yesterday"
            else -> displayFormat.format(inputDate)
        }
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun showDatePickerNormal(
        context: Context,
        preSelectedDate: String? = null,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        // If a previously selected date exists, use it
        if (!preSelectedDate.isNullOrBlank()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = sdf.parse(preSelectedDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val pickedCalendar = Calendar.getInstance()
                pickedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(pickedCalendar.time)

                // Return selected date
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    fun convertDateTimeFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // fallback if parsing fails
        }
    }


    fun convertEventDateTimeFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // fallback if parsing fails
        }
    }


    fun convertDateTimeFormat2(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // fallback if parsing fails
        }
    }

//    fun showDatePickerNormal(
//        context: Context,
//        onDateSelected: (String) -> Unit
//    ) {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePicker = DatePickerDialog(
//            context,
//            { _, selectedYear, selectedMonth, selectedDay ->
//                val pickedCalendar = Calendar.getInstance()
//                pickedCalendar.set(selectedYear, selectedMonth, selectedDay)
//
//                // Format date as dd-MM-yyyy
//                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//                val formattedDate = sdf.format(pickedCalendar.time)
//
//                // Return selected date
//                onDateSelected(formattedDate)
//            },
//            year,
//            month,
//            day
//        )
//
//        datePicker.show()
//    }


    fun setupEditTextWithScroll(context: Context, scrollView: ScrollView, editText: EditText) {
        val delayMillis = 300L
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.postDelayed({
                    scrollView.smoothScrollTo(0, v.bottom)
                }, delayMillis)
            }
        }

        editText.addTextChangedListener {
            scrollView.postDelayed({
                scrollView.smoothScrollTo(0, editText.bottom)
            }, delayMillis)
        }

        editText.apply {
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE

            imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            setSingleLine(false)
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_ALWAYS
        }
    }

    suspend fun convertToWav(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)

            var audioTrack = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrack = i
                    break
                }
            }
            if (audioTrack < 0) return@withContext null

            extractor.selectTrack(audioTrack)
            val format = extractor.getTrackFormat(audioTrack)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return@withContext null
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val outputDir = File(context.cacheDir, "wav_output")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "audio_${System.currentTimeMillis()}.wav")

            val outStream = FileOutputStream(outputFile)
            val header = ByteArray(44)
            outStream.write(header)

            val bufferInfo = MediaCodec.BufferInfo()
            var totalBytes = 0
            var end = false

            while (!end) {
                val inBuff = decoder.dequeueInputBuffer(10000)
                if (inBuff >= 0) {
                    val buffer = decoder.getInputBuffer(inBuff)!!
                    val size = extractor.readSampleData(buffer, 0)
                    if (size < 0) {
                        decoder.queueInputBuffer(
                            inBuff,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        end = true
                    } else {
                        decoder.queueInputBuffer(inBuff, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }

                val outBuff = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outBuff >= 0) {
                    val buffer = decoder.getOutputBuffer(outBuff)!!
                    val chunk = ByteArray(bufferInfo.size)
                    buffer.get(chunk)
                    outStream.write(chunk)
                    totalBytes += chunk.size
                    decoder.releaseOutputBuffer(outBuff, false)
                }
            }

            decoder.stop()
            decoder.release()
            extractor.release()

            // Write WAV header
            writeWavHeader(RandomAccessFile(outputFile, "rw"), totalBytes, sampleRate, channels)
            return@withContext outputFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun writeWavHeader(out: RandomAccessFile, pcmSize: Int, sampleRate: Int, channels: Int) {
        out.seek(0)
        val totalData = pcmSize + 36
        val byteRate = sampleRate * channels * 16 / 8

        val h = ByteArray(44)

        h[0] = 'R'.code.toByte()
        h[1] = 'I'.code.toByte()
        h[2] = 'F'.code.toByte()
        h[3] = 'F'.code.toByte()

        writeInt(h, 4, totalData)
        h[8] = 'W'.code.toByte()
        h[9] = 'A'.code.toByte()
        h[10] = 'V'.code.toByte()
        h[11] = 'E'.code.toByte()

        h[12] = 'f'.code.toByte()
        h[13] = 'm'.code.toByte()
        h[14] = 't'.code.toByte()
        h[15] = ' '.code.toByte()

        writeInt(h, 16, 16)
        writeShort(h, 20, 1)
        writeShort(h, 22, channels.toShort())
        writeInt(h, 24, sampleRate)
        writeInt(h, 28, byteRate)
        writeShort(h, 32, (channels * 2).toShort())
        writeShort(h, 34, 16)
        h[36] = 'd'.code.toByte()
        h[37] = 'a'.code.toByte()
        h[38] = 't'.code.toByte()
        h[39] = 'a'.code.toByte()
        writeInt(h, 40, pcmSize)

        out.write(h)
    }

    fun writeInt(h: ByteArray, offset: Int, value: Int) {
        h[offset] = (value and 0xff).toByte()
        h[offset + 1] = ((value shr 8) and 0xff).toByte()
        h[offset + 2] = ((value shr 16) and 0xff).toByte()
        h[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    fun writeShort(h: ByteArray, offset: Int, value: Short) {
        h[offset] = (value.toInt() and 0xff).toByte()
        h[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }
}