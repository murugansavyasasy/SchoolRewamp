package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
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
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Country.Country
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationData
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpData
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.Attachment.Attachment
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.Communication.DataClass.TextSendingData
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceSendingData
import com.vs.schoolmessenger.School.Homework.HomeWork
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object Constant {

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
    val M_EVENTS_HOLIDAYS = 9
    val M_EXAM = 10
    val M_FEEDBACK = 11
    val M_FEE_DETAILS = 12
    val M_FEE_PAYMENT = 13
    val M_FEE_PENDING_REPORT = 14
    val M_HOMEWORK = 15
    val M_INTERACTION_WITH_STAFF = 16
    val M_INTERACTION_WITH_STUDENT = 17
    val M_LEAVE_REQUEST = 18
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


    var SELECTED_SCHOOL_MENU = 0
    var SELECTED_PARENT_MENU = 0

    var isEmergencyVoiceNoticeBoard: Boolean? = false
    var isAccessType: Int? = null

    var isNonEmergency = 100
    var isEmergency = 101

    var isSchool = 1

    var isStandard = 2
    var isSection = 3
    var isGroup = 4
    var isStudent = 5
    var isStaff = 6

    var school = "A"
    var P="P"
    var MarkAllPresent="Mark all as present!"
    var standard = "C"
    var section = "S"
    var group = "G"
    var student = "student"
    var staff = "staff"

    var isCommunication = "isCommunication"
    var isHomeWork = "isHomeWork"
    var isGioMetric = "isGioMetric"

    //    var isVoiceFile: String? = null
    var isVoiceSendingData: VoiceSendingData? = null
    var isTextSendingData: TextSendingData? = null
    var commonFileList: List<CommonFileData> = emptyList()
    var selectedFileIndex: Int = -1
    var isCommunicationType = 1
    var isVoiceType = 1

    var isBioMetricEnable: Int = -1

    //MarkAttendanceDetails

    var isMarkAttendanceDataSending: MarkAttendanceDataSending? = null
    var isAbsenteesReportDataSending: ClassWise? = null

    var secondHalf = "SH"
    var firstHalf = "FH"
    var fullDay = "F"
    var allPresent = "T"
    var some_Absent = "F"
    var Absent = "Absent"
    var halfDay = "H"


    // String fields
    var scaleX = "scaleX"
    var scaleY = "scaleY"
    var AM = "AM"
    var PM = "PM"
    var dd_MM_yyyy = "dd/MM/yyyy"
    var EEE_dd_MMM_yyyy = "EEE dd MMM, yyyy"
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
    var isFileUrl = "isFileUrl"
    var isFileType = "isFileType"
    var isTitle = "isTitle"
    var isWebLink = "isWebLink"
    var parent = "parent"
    var isDocument = "isDocument"
    var staff_ = "staff"
    var en = "en"
    var Ok = "Ok"
    var Cancel="Cancel"
    var ta = "ta"
    var th = "th"
    var hi = "hi"
    var ar = "ar"
    var Gallery = "Gallery"
    var Images = "Images"
    var images_ = "images"
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
    var PDF = "PDF"
    var DOC = "DOC"
    var DOCX = "DOCX"
    var PPT = "PPT"
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
    var Custom = "Custom"
    var isRemove = "isRemove"
    var isUpdate = "isUpdate"
    var new = "new"
    var current = "current"

    var GET_ALL_STUDENT = "GET ALL STUDENT"
    var STANDARD = "STANDARD"
    var STANDARD_AND_SECTION = "STANDARD & SECTION"

    var NO_DATA_FOUND = "No Data Found"
    var No_STANDARD_FOUND = "No Standard Found"

    var isCommonTitle = ""
    var isCommonDescription = ""


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
                counterLabel.setText(editable!!.length.toString() + " of " + maxLength.toString())
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
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
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
        messageText.text = content
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
        if (isUserDetails.staff_details.size > 1) {
            isMultipleSchool = true
        } else {
            isMultipleSchool = false
        }
        if (isMultipleSchool) {
            if (isStaffRole.equals(Constant.isGroupHeadRole) || isStaffRole.equals(Constant.isPrincipalRole) || isStaffRole.equals(
                    Constant.isAdminRole
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

    @RequiresApi(Build.VERSION_CODES.O)
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
            if (SELECTED_SCHOOL_MENU == M_COMMUNICATION) {
                val intent = Intent(activity, CommunicationSchool::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_HOMEWORK) {
                val intent = Intent(activity, HomeWork::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            } else if (SELECTED_SCHOOL_MENU == M_ATTACHMENTS) {
                val intent = Intent(activity, Attachment::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true

    }

    public fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
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
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    fun covertDateFormate(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat(dd_MM_yyyy, Locale.getDefault())
            val outputFormat = SimpleDateFormat(EEE_dd_MMM_yyyy, Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // return original if there's a parsing error
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            if (user_details!!.staff_details.size == 1) {
                val intent = Intent(activity, SchoolDashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            } else {
                val intent = Intent(activity, SchoolList::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }

            closePopup()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun showSendConfirmationDialog(
        activity: Activity,
        istitle:String,
        Ok:String,
        Cancel:String,
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
        lblalertTitle.text=istitle
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
            (durationMs / 1000).toInt()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern(hh_mm_a) // or "hh:mm a" for AM/PM
        return currentTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern(ddMMyyyy) // or "dd/MM/yyyy", etc.
        return currentDate.format(formatter)
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

        return Triple(month, day, dayOfWeek)
    }

    fun getDeviceName(): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }


}