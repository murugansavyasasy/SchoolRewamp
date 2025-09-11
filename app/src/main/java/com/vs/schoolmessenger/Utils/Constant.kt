package com.vs.schoolmessenger.Utils

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
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.Country
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationData
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpData
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.ContactDetails
import com.vs.schoolmessenger.CommonScreens.MenuDetails.DashboardData
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuCountDetail
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateListData
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.StaffDataSending
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendingData
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceSendingData
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionDataSending
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import java.io.File
import java.io.FileOutputStream
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
    var isShimmerViewDisablenew = false
    var isShimmerView = false
    var handler = Handler(Looper.getMainLooper())
    val delayTime = 1500
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
    val M_FEE_PAYMENT = 13
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
    val M_ONLINE_MEETING = 24
    val M_ONLINE_TEXT_BOOK = 25
    val M_PTM = 26
    val M_QUIZ_EXAM = 27
    val M_REQUEST_LEAVE = 28
    val M_SCHOOL_CLASS_EVENTS = 29
    val M_PARENT_CLASS_EVENTS = 9
    val M_SCHOOL_NEEDS = 30
    val M_SCHOOL_STRENGTH = 31
    val M_STAFF_LIST = 32
    val M_STAFF_WISE_ATTENDANCE_REPORT = 33
    val M_STUDENT_LIST = 34
    val M_STUDENT_REPORT = 35
    val M_VERY_IMPORTANT_INFO = 36
    val M_YOUR_PROFILE = 37
    val M_SCHEDULE_EXAM_TEST = 38
    val M_ATTACHMENTS = 39
    val M_FINANCE = 194

    val M_COUPON_PACKET = 40

    var SELECTED_SCHOOL_MENU = 0
    var SELECTED_PARENT_MENU = 0

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

    var school = "A"
    var P = "P"
    var standard = "C"
    var section = "S"
    var group = "G"
    var student = "student"
    var staff = "staff"
    var isVoiceSendingData: VoiceSendingData? = null
    var isTextSendingData: TextSendingData? = null
    var commonFileList: MutableList<CommonFileData> = mutableListOf()
    var selectedFileIndex: Int = -1
    var isCommunicationType = 1
    var isVoiceType = 1
    var isQuestionLimit = -1

    var isTitleLength = 50
    var isDescriptionLength = 500
//    var MAX_FILES = 10

    var isAcademicYearList: List<AcademicYear>? = null
    var isParentMenuName = ""

    var isSchoolMenuName = ""
    var isSchoolMenuCount =-1


//    var isForward = false
    //MarkAttendanceDetails

    var isMarkAttendanceDataSending: MarkAttendanceDataSending? = null
    var isLeaveData: LeaveData? = null
    var isCertificateData: CertificateListData? = null

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


    var secondHalf = "SH"
    var firstHalf = "FH"
    var fullDay = "F"
    var allPresent = "T"
    var some_Absent = "F"
    var Absent = "Absent"
    var halfDay = "H"
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
    var notice_data = "notice_data"
    var event_data = "event_data"
    var assignment_data = "assignment_data"
    var lsrwskill_data = "lsrwskill_data"
    var lsrwsubmitskill_data = "lsrwsubmitskill_data"
    var isFileUrl = "isFileUrl"
    var isFileType = "isFileType"
    var isTitle = "isTitle"
    var isWebLink = "isWebLink"
    var parent = "parent"
    var isDocument = "isDocument"
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
    var ATTACHMENT = "ATTACHMENT"
    var ALL = "ALL"
    var fromArchive = "fromArchive"
    var UNREAD = "UNREAD"
    var READ = "READ"
    var VOICE = "VOICE"
    var AUDIO = "AUDIO"
    var VIDEO = "VIDEO"
    var DOCUMENT = "DOCUMENT"
    var VOICE_UNREAD = "VOICE_UNREAD"
    var VOICE_READ = "VOICE_READ"
    var TEXT = "TEXT"
    var TEXT_UNREAD = "TEXT_UNREAD"
    var TEXT_READ = "TEXT_READ"
    var TEXT_ALL = "TEXT_ALL"
    var VOICE_ALL = "VOICE_ALL"
    var dateForMate = "%02d:%02d"
    var isSelectedFiles = "isSelectedFiles"

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
    var EXCEL = "EXCEL"
    var data = "data"
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
    var Present = "Present"
    var googleMap = "com.google.android.apps.maps"
    var Youneedmore = "You need more points! Use the app to keep earning points"
    var Custom = "Custom"
    var isRemove = "isRemove"
    var isUpdate = "isUpdate"
    var new = "new"
    var current = "current"

    var GET_ALL_STUDENT = "GET ALL STUDENT"
    var STANDARD = "STANDARD"
    var STANDARD_AND_SECTION = "STANDARD AND SECTION"

    var NO_DATA_FOUND = "No Data Found"
    var No_STANDARD_FOUND = "No Standard Found"

    var isCommonTitle = ""
    var isCommonDescription = ""
    var assignment_id = "assignment_id"
    var title_ = "title"
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
    var all__ = "all"
    var st = "st"
    var nd = "nd"
    var rd = "rd"
    var ONGOING = "ONGOING"
    var COMPLETED = "COMPLETED"
    var UPCOMING = "UPCOMING"


     var category_name = "category_name"
     var discount = "discount"
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
    var id = "id"
    var mobile_number = "mobile_number"
    var offer_to_show = "offer_to_show"
    var how_to_use = "how_to_use"
    var coupon_code = "coupon_code"
    var cover_image = "cover_image"
    var expiry_date = "expiry_date"
    var expiry_type = "expiry_type"
    var thumbnail = "thumbnail"
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


    fun isInternetAvailable(activity: Activity): Boolean {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    fun setGridViewHeight(gridView: GridView, columns: Int) {
        val adapter = gridView.adapter ?: return  // Check if adapter is not null

        var totalHeight = 0
        val items = adapter.count
        val rows = (items + columns - 1) / columns  // Calculate the number of rows

        // Loop through each item to get its height
        for (i in 0 until items) {
            val listItem = adapter.getView(i, null, gridView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(gridView.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += listItem.measuredHeight
        }

        // Calculate total height by adding row heights and spacing between rows
        totalHeight += (gridView.verticalSpacing * (rows - 1))

        val params = gridView.layoutParams
        params.height = totalHeight
        gridView.layoutParams = params
        gridView.requestLayout()  // Request layout update
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

    fun editTitleTextCounter(
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
        titleText.text = "Oops! Wrong Password"
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


    //"dd-MM-yyyy" to "dd MMMM, yyyy"
    fun formatDate(dateStr: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        val inputDate = inputFormat.parse(dateStr) ?: return dateStr

        val calendar = Calendar.getInstance()

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

        val calendar = Calendar.getInstance()

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
                // Only one word → just first letter
                parts[0].first().uppercaseChar().toString()
            }
            else -> {
                val first = parts.first().first().uppercaseChar()
                val last = parts.last().last().uppercaseChar()
                "$first$last"
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


    //Leave Request
    fun handleRestrictDatePicker(
        context: Context,
        minDate: Long? = null,
        preSelectedDateMillis: Long? = null,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        // Use pre-selected date if it is selected
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


    //Convert dd-MM-YYYY to dd MMM YYYY (12-02-2025 to 12 Feb 2025)
    fun convertToReadableDate(inputDateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(inputDateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            inputDateStr
        }
    }


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
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

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


    fun convertSubmittedDateAssignment(input: String?): String {
        if (input.isNullOrEmpty()) return "--"

        return try {

            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())


            val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

            val date = inputFormat.parse(input)
            date?.let { outputFormat.format(it) } ?: "--"
        } catch (e: Exception) {
            "--"
        }
    }



    fun convertDateTimeFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat(ddMMyyyy, Locale.getDefault())
            val outputFormat = SimpleDateFormat(dd_MMM_yyyy, Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // return original if there's a parsing error
        }

    }

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


    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            newWidth = maxWidth
            newHeight = (maxWidth / ratio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
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
                if (!fingerPrintEnabled) {
                    val fingerPrintSkipped = SharedPreference.isFingerPrintSkipped(activity)
                    if (!fingerPrintSkipped) {
                        AlertDialog.Builder(activity)
                            .setTitle("Enable Fingerprint Login?")
                            .setMessage("Would you like to enable fingerprint authentication for faster and secure access in the future?")
                            .setPositiveButton("Yes") { _, _ ->
                                SharedPreference.setFingerprintEnabled(activity, true)
                            }
                            .setNegativeButton("No") { _, _ ->
                                SharedPreference.setFingerPrintSkipped(activity, true)
                                SharedPreference.setFingerprintEnabled(activity, false)

                            }
                            .show()
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
        listener: fingerPrintAunthenticateListener
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
                            AlertDialog.Builder(activity)
                                .setTitle("School Chimes is locked")
                                .setMessage("Authentication is required to access the School Chimes")
                                .setPositiveButton("Unlock now") { _, _ ->
                                    authenticate(activity)
                                }
                                .show()
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

//    fun showEnrollDialog(activity: Activity) {
//        AlertDialog.Builder(activity)
//            .setTitle("Fingerprint not set up")
//            .setMessage("To use fingerprint login, please add at least one fingerprint in your device settings.")
//            .setPositiveButton("Go to Settings") { _, _ ->
//                // Open biometric enrollment screen
//                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
//                    putExtra(
//                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
//                    )
//                }
//                activity.startActivity(enrollIntent)
//            }
//            .setNegativeButton("SKIP") { _, _ ->
//            }
//            .show()
//    }

    fun showDatePickerNormal(
        context: Context,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val pickedCalendar = Calendar.getInstance()
                pickedCalendar.set(selectedYear, selectedMonth, selectedDay)

                // Format date as dd-MM-yyyy
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

}