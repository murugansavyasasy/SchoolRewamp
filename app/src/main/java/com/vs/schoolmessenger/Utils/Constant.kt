package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.app.AlertDialog
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
import com.vs.schoolmessenger.Auth.Country.Country
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationData
import com.vs.schoolmessenger.Auth.OTP.ForgetOtpData
import com.vs.schoolmessenger.CommonScreens.SchoolList.SchoolList
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.Communication.TextSendingData
import com.vs.schoolmessenger.School.Communication.VoiceSendingData
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object Constant {

    var isDeviceType = "Android"
    var isVersionId = 93
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
    var isChildDetails: List<ChildDetails>? = null
    var isPasswordCreation: Boolean? = false
    var forgotData: List<ForgetOtpData>? = null
    var isForgotPassword: Boolean? = false
    var isMobileNumber: String? = ""

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

    val STU_COMMUNICATION = 7
    val STU_HOMEWORK = 3
    val STU_EXAM = 5
    val STU_NOTICEBOARD = 7
    val STU_EVENTS = 8
    val STU_ATTENDANCE_REPORT = 9
    val STU_LEAVE_REQUEST = 10
    val STU_FEE_DETAILS = 11
    val STU_INTERACTION_WITH_STAFF = 14
    val STU_ASSIGNMENT = 18
    val STU_ONLINE_MEETING = 20
    val STU_QUIZ = 21
    val STU_LSRW = 22
    val STU_TIME_TABLE = 23
    val STU_CERTIFICATE_REQUEST = 25


    val SH_COMMUNICATION = 7
    val SH_ASSIGNMENT = 22
    val SH_HOMEWORK = 9
    val SH_ATTENDANCE_MARKING = 12
    val SH_ABSENTEEISM_REPORT = 6
    val SH_SCHOOL_STRENGTH = 7
    val SH_NOTICE_BOARD = 3
    val SH_EVENTS = 4
    val SH_SCHEDULE_EXAM_TEST = 11
    val SH_MESSAGES_FROM_MANAGEMENT = 13
    val SH_CONFERENCE_CALL_WITH_TEACHERS = 16
    val SH_ONLINE_MEETING = 26
    val SH_DAILY_COLLECTION = 28
    val SH_STUDENT_REPORT = 29
    val SH_LESSON_PLAN = 30
    val SH_FEEDBACK = 14
    val SH_IMPORTANT_INFO = 21
    val SH_ATTACHMENTS = 0
    val SH_SCHOOL_NEEDS = 0
    val SH_FEE_PENDING_REPORT = 0
    val SH_MARK_GEOMETRIC_ATTENDANCE = 21
    val SH_STAFF_WISE_GEOMETRIC_ATTENDANCE_REPORT = 1022
    val SH_PTM = 0
    val SH_INTERACTION_WITH_STUDENT = 16



    var SELECTED_SCHOOL_MENU = 0
    var SELECTED_PARENT_MENU = 0

    var isEmergencyVoiceNoticeBoard: Boolean? = false
    var isAccessType: Int? = null

    var isNonEmergency = 100
    var isEmergency = 101
    var isText = 102

    var isSchool = 1
    var isStandard = 2
    var isSection = 3
    var isGroup = 4
    var isStudent = 5
    var isStaff = 6

    var school = "A"
    var standard = "C"
    var section = "S"
    var group = "G"
    var student = "student"
    var staff = "staff"

    var isCommunication = "isCommunication"
    var isGioMetric = "isGioMetric"

    var isVoiceFile: String? = null
    var isVoiceSendingData: VoiceSendingData? = null
    var isTextSendingData: TextSendingData? = null
    var isClickType = 1
    var isVoiceType = 1

    var isBioMetricEnable: Int = -1


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
        intent.data = Uri.parse("tel:" + contactNo) // Replace with the phone number
        context.startActivity(intent)
    }

    fun redirectToMail(context: Context, mail: String,sub : String , body : String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Ensures only email apps handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mail)) // Recipient email address
            putExtra(Intent.EXTRA_SUBJECT, sub) // Subject
            putExtra(Intent.EXTRA_TEXT, body) // Email body
        }
// Verify that there is an email app to handle the intent
        val emailApps = context.packageManager.queryIntentActivities(intent, 0)
        if (emailApps.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent)
            //   Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    fun redirectToMessage(context: Context, phoneNumber: String) {
        val smsUri = Uri.parse("sms:" + phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW, smsUri).apply {
            putExtra("", "")
        }
        context.startActivity(intent)
    }

    fun editTextCounter(
        context: Context,
        editText: EditText,
        maxLength: Int,
        counterLabel: TextView
    ) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // You can add logic here if needed
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
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
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
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
    fun showTopAlertPopup(message: String, isType: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
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
            if (isType == isCommunication) {
                val intent = Intent(activity, CommunicationSchool::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            } else if (isType == isGioMetric) {
                val intent = Intent(activity, MarkYourAttendance::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
            closePopup()
        }
        dimView.setOnClickListener {
            closePopup()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun showValidationAlertPopup(message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
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

    fun getAudioDurationInSeconds(url: String): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(url, HashMap()) // For network sources, use empty headers map
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
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
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
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

    fun getDeviceDetails(context: Activity) : String{
        val deviceDetails = mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "device" to Build.DEVICE,
            "brand" to Build.BRAND,
            "hardware" to Build.HARDWARE,
            "product" to Build.PRODUCT,
            "os_version" to Build.VERSION.RELEASE,
            "sdk_int" to Build.VERSION.SDK_INT.toString(),
            "app_version" to getAppVersion(context)
        )
        return deviceDetails.toString()
    }


    fun getAppVersion(context: Activity): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTime(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a") // or "hh:mm a" for AM/PM
        return currentTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // or "dd/MM/yyyy", etc.
        return currentDate.format(formatter)
    }

    fun convertDateTimeFormat(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input // return original if there's a parsing error
        }

    }
    fun getTimeAfter20Minutes(): String {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
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
        val loaderView = LayoutInflater.from(context).inflate(R.layout.lottie_loader, rootView, false)
        rootView.addView(loaderView)

    }

    fun getDateDetails(input: String): Triple<String, Int, String> {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = sdf.parse(input) ?: return Triple("", -1, "")

        val calendar = Calendar.getInstance().apply { time = date }

        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date) // "April"
        val day = calendar.get(Calendar.DAY_OF_MONTH) // 29
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date) // "Tuesday"

        return Triple(month, day, dayOfWeek)
    }


}