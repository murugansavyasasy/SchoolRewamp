package com.vs.schoolmessenger.Auth.Splash

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.Country.CountryScreen
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.MobileNumber
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Attachment.Attachment
import com.vs.schoolmessenger.Parent.Communication.CommunicationParent
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Event
import com.vs.schoolmessenger.Parent.FeeDetails.FeeDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoard
import com.vs.schoolmessenger.Parent.PTM.PTM
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.School.Event.EventReport
import com.vs.schoolmessenger.Utils.AnimationHelper
import com.vs.schoolmessenger.Utils.AppDataCleaner
import com.vs.schoolmessenger.Utils.AppSignatureHelper
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isDeveloperModeEnabled
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.fingerPrintAunthenticateListener
import com.vs.schoolmessenger.databinding.SplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Splash : BaseActivity<SplashBinding>(), View.OnClickListener,
    fingerPrintAunthenticateListener {

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(newBase)
        val context = ChangeLanguage.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Snackbar.make(
                findViewById(android.R.id.content),
                resources.getString(R.string.Update_failed),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    var isVersionData: List<VersionData>? = null
    override fun getViewBinding(): SplashBinding {
        return SplashBinding.inflate(layoutInflater)
    }

    private var authViewModel: Auth? = null
    private var appViewModel: App? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    var noInternetalertDialog: AlertDialog? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarTheme()

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Define the callback
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if(noInternetalertDialog != null && noInternetalertDialog!!.isShowing) {
                        noInternetalertDialog!!.dismiss()
                    }
                    Log.d("goToNext","goToNext1")
                   // goToNext()
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                }
            }
        }

        // Run cleanup
//        val cleaned = AppDataCleaner.clearOldDataIfNeeded(this)
//        if (cleaned) {
//            Log.d("Cleanup","cleaned")
//            // Optional: show a loading indicator since cleanup might take time
//            Handler(Looper.getMainLooper()).postDelayed({
////                goToNextScreen()
//            }, 1500) // small delay after cleanup
//        }
//        else {
////            goToNextScreen()
//        }


        val fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)
        Log.d("fromNotification", fromNotification.toString())

        if (fromNotification) {
            handleNotificationIntent(intent)
        } else {
            //  normal process
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appSignatureHelper = AppSignatureHelper(this)
        val appSignatures = appSignatureHelper.getAppSignatures()

        for (signature in appSignatures) {
            Log.d("AppHash", signature)
        }
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("goToNext","goToNext2")

                goToNext()
                Log.d("PermissionResult", "✅ User clicked ALLOW for notification permission")
            } else {
                Log.d("goToNext","goToNext3")

                goToNext()
                Log.d(
                    "PermissionResult",
                    "❌ User clicked DENY or DISMISSED the notification permission dialog"
                )
            }
        }

        askNotificationPermission()
        authViewModel!!.isUserValidation?.observe(this) { response ->
            Constant.hideLoading(this@Splash)
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {


                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_login)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_SCHOOL_MENU)
                    }
                    appViewModel?.isAddRewardPoints("" ?: "", jsonObject)

                    val isValidateUser = response.data
                    Constant.user_data = isValidateUser
                    Constant.user_details = Constant.user_data!![0].user_details
                    Constant.isStaffDetails = Constant.user_data!![0].user_details.staff_details
                    Constant.isChildDetails = Constant.user_data!![0].user_details.child_details
                    SharedPreference.putUserDetails(this@Splash, Constant.user_details!!)

                    val mobile_number = SharedPreference.getMobileNumber(this)
                    val password = SharedPreference.getPassWord(this)
                    Constant.isMobileNumber = mobile_number
                    if (isValidateUser[0].is_password_updated) {
                        if (isValidateUser[0].otp_sent) {
                            val intent = Intent(this@Splash, OTP::class.java)
                            Constant.pageType = Constant.SplashScreen
                            startActivity(intent)
                        } else {
                            SharedPreference.setLoggedIn(this, true)
                            SharedPreference.putMobileNumberPassWord(
                                this@Splash,
                                mobile_number,
                                password
                            )
                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@Splash, PrioritySelection::class.java)
                                startActivity(intent)
                            } else if (Constant.user_data!![0].user_details.is_staff) {


                                if (Constant.user_data!![0].user_details.staff_details.size > 1) {
                                    val intent =
                                        Intent(this@Splash, PrioritySelection::class.java)
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(
                                        this@Splash,
                                        SchoolDashboard::class.java
                                    )
                                    SharedPreference.putStaffDetails(
                                        this,
                                        Constant.user_data!![0].user_details.staff_details[0]
                                    )
                                    startActivity(intent)
                                }

                            } else if (Constant.user_data!![0].user_details.is_parent) {
                                Constant.isParentChoose = true
                                if (Constant.user_data!![0].user_details.child_details.size > 1) {
                                    val intent = Intent(this@Splash, PrioritySelection::class.java)
                                    startActivity(intent)
                                } else {

                                    val intent = Intent(
                                        this@Splash,
                                        com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard::class.java
                                    )
                                    SharedPreference.putChildDetails(
                                        this,
                                        Constant.user_data!![0].user_details.child_details[0]
                                    )
                                    startActivity(intent)
                                }
                            }
                        }
                    } else {
                        val intent = Intent(this@Splash, OTP::class.java)
                        Constant.pageType = Constant.SplashScreen
                        Constant.isPasswordCreation = true
                        startActivity(intent)
                    }
                }
                else{
                    Constant.errorAlert(this@Splash, "", message)
                }
            }
        }

        authViewModel!!.isVersionCheck?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    val isVersionCheckData = response.data
                    isVersionData = isVersionCheckData
                    Constant.country_details = isVersionData!![0].country_details
                    SharedPreference.putCountryId(this, Constant.country_details!!.id)
                    SharedPreference.putBaseUrl(this, Constant.country_details!!.base_url)
                    RestClient.changeApiBaseUrl(Constant.country_details!!.base_url)
                    if (isVersionData!![0].update_available) {
                        isShowUpdateAvailable(isVersionData!!)
                    } else {
                        autoLoginFlowCheck(isVersionData!!)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val fromNotification = intent?.getBooleanExtra(Constant.fromNotification, false) ?: false
        if (fromNotification) {
            handleNotificationIntent(intent)
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent ?: return
        val fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)
        var menu_name: String? = null
        var menu_id: Int = 0
        var msg_id: Int = 0
        var headerId: String? = null
        var receiverId: String? = null
        if (fromNotification) {
            menu_name = intent.getStringExtra(Constant.menu_name)
            headerId = intent.getStringExtra("header_id")
            receiverId = intent.getStringExtra("receiverid")
            menu_id = intent.getIntExtra(Constant.menu_id, 0)
            msg_id = intent.getIntExtra(Constant.msg_id, 0)
        }

        if (!SharedPreference.isLoggedIn(this)) {
            // Redirect to login
            val loginIntent = Intent(this, Login::class.java).apply {
                putExtra(Constant.menu_name, menu_name)
                putExtra("header_id", headerId)  // Use "header_id" consistently
                putExtra("receiverId", receiverId)  // Use "header_id" consistently
                putExtra(Constant.menu_id, menu_id)
                putExtra(Constant.msg_id, msg_id)
                putExtra(Constant.fromNotification, fromNotification)  // Fixed: was msg_id
            }
            startActivity(loginIntent)
            return
        }

        // Open the target screen only if launched from notification
        if (fromNotification) {
            when (menu_id) {
                Constant.M_COMMUNICATION -> {
                    val detailIntent = Intent(this, CommunicationParent::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(CommunicationParent::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_HOMEWORK -> {
                    val detailIntent = Intent(this, HomeWork::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(CommunicationParent::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_NOTICEBOARD -> {
                    val detailIntent = Intent(this, NoticeBoard::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra("header_id", headerId)    // ← String
                        putExtra("receiverid", receiverId)
                        // ← String
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(NoticeBoard::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_ASSIGNMENT -> {
                    val detailIntent = Intent(this, Assignment::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(Assignment::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_ATTACHMENTS -> {
                    val detailIntent = Intent(this, Attachment::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(Attachment::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_SCHOOL_CLASS_EVENTS -> {
                    val detailIntent = Intent(this, Event::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(EventReport::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_PARENT_CLASS_EVENTS -> {
                    val detailIntent = Intent(this, Event::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(Event::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_PTM -> {
                    val detailIntent = Intent(this, PTM::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(CommunicationParent::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                Constant.M_FEE_DETAILS -> {
                    val detailIntent = Intent(this, PTM::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(FeeDetails::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                else -> {
                    // default behavior
                }
            }
        } else {
            // usual process
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("goToNext","goToNext4")

                goToNext()
            }
        } else {
            Log.d("goToNext","goToNext5")

            goToNext()
        }
    }


    private fun isInterNetChecking() {
        lifecycleScope.launch {
            delay(2000) // 2-second delay
            withContext(Dispatchers.Main) {
                if (Constant.isInternetAvailable(this@Splash)) {
                    val countryId = SharedPreference.getCountryId(this@Splash)
                    Log.d("countryId", countryId.toString())
                    if (countryId != 0) {
                        isVersionCheck()
                    } else {
                        startActivity(Intent(this@Splash, CountryScreen::class.java))
                        finish()
                    }
                } else {
                    Log.e("Network Error", "No Internet Connection")
                    isNoInterNet()
                }
            }
        }
    }

    private fun autoLoginFlowCheck(isVersionData: List<VersionData>) {
        val mobile_number = SharedPreference.getMobileNumber(this)
        val password = SharedPreference.getPassWord(this)
        Log.d("mobile_number", mobile_number.toString())
        Log.d("password", password.toString())
        if (!mobile_number.equals("") && !password.equals("")) {
            if (SharedPreference.isFingerprintEnabled(this)) {
                if (SharedPreference.isLoggedIn(this)) {
                    Constant.setupBiometricPrompt(this, this, true)
                    Constant.authenticate(this)
                } else {
                    val intent = Intent(this@Splash, Login::class.java)
                    startActivity(intent)
                }
            } else {
                if (SharedPreference.isLoggedIn(this)) {
                    isValidateUser()
                } else {
                    val intent = Intent(this@Splash, Login::class.java)
                    startActivity(intent)
                }
            }
        } else {
            val isLogout = SharedPreference.getLogout(this)
            if (isLogout!!) {
                val intent = Intent(this@Splash, Login::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@Splash, MobileNumber::class.java)
                startActivity(intent)
            }
        }
    }

    fun showSecurityAlert(context: Context) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet, null)
        dialog.setContentView(view)
        val btnClose = view.findViewById<RelativeLayout>(R.id.btnClose)
        btnClose.setOnClickListener {
            finish()
        }
        dialog.show()
    }

    private fun isValidateUser() {
        Constant.showLoading(this@Splash)
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@Splash)
        val isMobileNumber = SharedPreference.getMobileNumber(this)
        val isPassWord = SharedPreference.getPassWord(this)
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, isMobileNumber)
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_secure_id, isSecureId)
        jsonObject.addProperty(APIKeyNames.Req_password, isPassWord)
        Log.d("jsonObject", jsonObject.toString())
        authViewModel!!.isValidateUser(jsonObject, this)
    }


    private fun isVersionCheck() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_version_code, Constant.isVersionId)
        jsonObject.addProperty(
            APIKeyNames.Req_country_id,
            SharedPreference.getCountryId(this)
        )
        authViewModel!!.isVersionCheck(jsonObject, this)
    }

    private fun isToolBarTheme() {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.white)
        window.navigationBarColor = this.resources.getColor(R.color.white)
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    private fun isShowUpdateAvailable(versionData: List<VersionData>) {
        val dialogView = layoutInflater.inflate(R.layout.whats_new_popup, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            .setCancelable(false) // Prevent dismissing by clicking outside

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Remove default background
        alertDialog.show()

        val btnUpdateButton = dialogView.findViewById<TextView>(R.id.btnUpdate)
        val btnNotNow = dialogView.findViewById<TextView>(R.id.btnNotNow)
        val lblNewVersionCode = dialogView.findViewById<TextView>(R.id.lblNewVersionCode)
        val lblYourAppVersionCode = dialogView.findViewById<TextView>(R.id.lblYourAppVersionCode)
        val pInfo =this.packageManager.getPackageInfo(this.packageName, 0)
        val versionName = pInfo.versionName
        lblNewVersionCode.setText(versionData[0].new_version)
        lblYourAppVersionCode.setText(versionName)

        if (versionData[0].force_update) {
            btnUpdateButton.visibility = View.VISIBLE
            btnNotNow.visibility = View.GONE
        } else {
            btnUpdateButton.visibility = View.VISIBLE
            btnNotNow.visibility = View.VISIBLE
        }

        btnUpdateButton.setOnClickListener {
            alertDialog.dismiss() // Close popup
            startInAppUpdate()
        }

        btnNotNow.setOnClickListener {
            alertDialog.dismiss() // Close popup
            autoLoginFlowCheck(isVersionData!!)

        }
    }

    private fun startInAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("isNotAvailable", e.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the callback
        val request = android.net.NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // Unregister callback to avoid memory leaks
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun goToNext() {
//        if (isDeveloperModeEnabled(this)) {
//            showSecurityAlert(this)
//        } else {
        isInterNetChecking()
        // }

    }


    fun isNoInterNet() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.no_internet_connection, null)
        dialogView.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnCreate = dialogView.findViewById<RelativeLayout>(R.id.btnCreate)

        noInternetalertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCreate.setOnClickListener {
            noInternetalertDialog!!.dismiss()
            finish()
        }
        noInternetalertDialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        noInternetalertDialog!!.show()
    }

    override fun onAuthenticate(message: String, status: Boolean) {
        Log.d("athentication_status", message)
        if (status) {
            isValidateUser()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}