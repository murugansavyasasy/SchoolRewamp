package com.vs.schoolmessenger.Auth.Splash

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.vs.schoolmessenger.Auth.Introduction.Introduction
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.MobileNumber
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.RateUsListener
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.RateUsDialog
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Attachment.Attachment
import com.vs.schoolmessenger.Parent.Communication.CommunicationParent
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Event
import com.vs.schoolmessenger.Parent.FeeDetails.FeeDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.InteractionWithStaff.InteractionWithStaff
import com.vs.schoolmessenger.Parent.LSRW.LSRW
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoard
import com.vs.schoolmessenger.Parent.PTM.PTM
import com.vs.schoolmessenger.Parent.QuizExam.Quiz
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.School.Assignment.AssignmentReport
import com.vs.schoolmessenger.School.InteractionWithStudent.InteractionWithStudent
import com.vs.schoolmessenger.School.LeaveRequests.LeaveRequests
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.Utils.AppSignatureHelper
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.fingerPrintAunthenticateListener
import com.vs.schoolmessenger.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Splash : BaseActivity<ActivitySplashBinding>(), View.OnClickListener,
    fingerPrintAunthenticateListener {

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(newBase)
        val context = ChangeLanguage.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    private lateinit var llBottomText: View
    private lateinit var underline: View
    private lateinit var txtEmpowering: View
    private lateinit var txtSchoolCount: View
    private lateinit var txtConnecting: View

    private val confettiColors = listOf(
        Color.parseColor("#FF6B9D"), Color.parseColor("#4ECDC4"), Color.parseColor("#FFD93D"),
        Color.parseColor("#A8E6CF"), Color.parseColor("#B4A7D6"), Color.parseColor("#F7CAC9"),
        Color.parseColor("#6C5CE7"), Color.parseColor("#74B9FF"), Color.parseColor("#FFA502"),
        Color.parseColor("#55E6C1"), Color.parseColor("#FDA7DF"), Color.parseColor("#95E1D3"),
        Color.parseColor("#F38181"), Color.parseColor("#AA96DA"), Color.parseColor("#FCBAD3"),
        Color.parseColor("#FFB6C1"), Color.parseColor("#87CEEB"), Color.parseColor("#98D8C8"),
        Color.parseColor("#F7DC6F"), Color.parseColor("#BB8FCE")
    )

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
    override fun getViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    private var authViewModel: Auth? = null
    private var appViewModel: App? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    var noInternetalertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        // 🚀 Android 12+ instant splash
        installSplashScreen()

        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarNoticeCallTheme()
        llBottomText = binding.llBottomText
        underline = binding.underline
        txtEmpowering = binding.txtEmpowering
        txtSchoolCount = binding.txtSchoolCount
        txtConnecting = binding.txtConnecting
        txtConnecting.alpha = 0f
        binding.dot1.alpha = 0f
        binding.dot2.alpha = 0f
        binding.dot3.alpha = 0f
        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 1f
        binding.imgLogo.scaleY = 1f
        binding.bigCard.scaleX = 0.9f
        binding.bigCard.scaleY = 0.9f
        binding.bigCard.alpha = 0f
        llBottomText.alpha = 0f
        underline.alpha = 0f
        startVideoStyleAnimation()

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        // Define the callback
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if (noInternetalertDialog != null && noInternetalertDialog!!.isShowing) {
                        noInternetalertDialog!!.dismiss()
                    }
                    Log.d("goToNext", "goToNext1")
                    // goToNext()
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                }
            }
        }

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("goToNext", "goToNext2")

                goToNext()
                Log.d("PermissionResult", "✅ User clicked ALLOW for notification permission")
            } else {
                Log.d("goToNext", "goToNext3")

                goToNext()
                Log.d(
                    "PermissionResult",
                    "❌ User clicked DENY or DISMISSED the notification permission dialog"
                )
            }
        }

        val fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)
        Log.d("fromNotification", fromNotification.toString())

        if (fromNotification) {
            handleNotificationIntent(intent)
        } else {
            //Run cleanup
//            val cleaned = AppDataCleaner.clearOldDataIfNeeded(this)
//            if (cleaned) {
//                Log.d("Cleanup", "cleaned")
//                // Optional: show a loading indicator since cleanup might take time
//                Handler(Looper.getMainLooper()).postDelayed({
//                    askNotificationPermission()
//                }, 1500) // small delay after cleanup
//            } else {
            askNotificationPermission()
            // }

        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appSignatureHelper = AppSignatureHelper(this)
        val appSignatures = appSignatureHelper.getAppSignatures()

        for (signature in appSignatures) {
            Log.d("AppHash", signature)
        }


//        askNotificationPermission()
        authViewModel!!.isUserValidation?.observe(this) { response ->
            Constant.hideLoading(this@Splash)
            if (response != null) {
                val status = response.status
                response.message
                if (status) {


                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_login)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
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
                } else {
                    val intent = Intent(this@Splash, Login::class.java)
                    startActivity(intent)
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

                    val isRateUs = true
                    val isMobileNumber = SharedPreference.getMobileNumber(this)

                    if (isRateUs && isMobileNumber!!.isNotEmpty()) {
                        val dialog = RateUsDialog(
                            fromScreen = Constant.SplashScreen__,
                            listener = object : RateUsListener {
                                override fun onRateUsCompleted(isSuccess: Boolean) {
                                    if (isVersionData!![0].update_available) {
                                        isShowUpdateAvailable(isVersionData!!)
                                    } else {
                                        autoLoginFlowCheck(isVersionData!!)
                                    }
                                }
                            }
                        )
                        dialog.isCancelable = false
                        dialog.show(supportFragmentManager, "RateUsDialog")

                    } else {
                        if (isVersionData!![0].update_available) {
                            isShowUpdateAvailable(isVersionData!!)
                        } else {
                            autoLoginFlowCheck(isVersionData!!)
                        }
                    }


                }
            }
        }
    }

    private fun startVideoStyleAnimation() {
        animateCardEntrance()
        animateFirstDotWithEmphasis()
        binding.confettiContainer.post { playBubbleAnimation() }
        //animateRemainingDots()
//
//        binding.root.postDelayed({
//            finish()
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//        }, 3000)
    }

    private fun playBubbleAnimation() {
        val container = binding.confettiContainer
        val centerX = container.width / 2f
        val centerY = container.height / 2f
        val bubbleCount = 40
        val bubbleDuration = 1000L
        val bubbleDelay = 25L

        repeat(bubbleCount) { i ->
            val isLastBubble = i == bubbleCount - 1
            container.postDelayed({
                createBubble(container, centerX, centerY, bubbleDuration, isLastBubble)
            }, i * bubbleDelay)
        }
    }

    private fun createBubble(
        container: FrameLayout,
        centerX: Float,
        centerY: Float,
        duration: Long,
        isLastBubble: Boolean
    ) {
        val bubble = View(this)
        val size = (10..15).random()
        bubble.layoutParams = FrameLayout.LayoutParams(size, size)
        bubble.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(confettiColors.random())
        }
        val (startX, startY) = when ((1..4).random()) {
            1 -> 0f to (0..container.height).random().toFloat()
            2 -> container.width.toFloat() to (0..container.height).random().toFloat()
            3 -> (0..container.width).random().toFloat() to 0f
            else -> (0..container.width).random().toFloat() to container.height.toFloat()
        }
        bubble.x = startX
        bubble.y = startY
        container.addView(bubble)
        val clusterSpread = 70
        val finalX = centerX + (-clusterSpread..clusterSpread).random() - size / 2
        val finalY = centerY + (-clusterSpread..clusterSpread).random() - size / 2
        bubble.animate().x(finalX).y(finalY).alpha(0f).setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator()).withEndAction {
            container.removeView(bubble)
            if (isLastBubble) {
                startWaveAnimation()
                container.visibility = View.GONE
                startAllSplashAnimations()

            }
        }
            .start()

    }

    private fun startWaveAnimation() {
        val waves = listOf(binding.wave1, binding.wave2, binding.wave3)
        waves.forEachIndexed { index, wave ->
            wave.scaleX = 0f
            wave.scaleY = 0f
            wave.alpha = 0f
            wave.visibility = View.VISIBLE
            wave.animate().alpha(0.4f).scaleX(1.7f).scaleY(1.7f).setStartDelay(index * 200L)
                .setDuration(1000).withEndAction {
                wave.animate().alpha(0f).scaleX(2.2f).scaleY(2.2f).setDuration(600).start()
            }.start()
        }
    }

    private fun startAllSplashAnimations() {
        animateTopText()
        animateBottomText()
        animateRemainingDots()
        animateLogoPulse()
        binding.imgLogo.visibility = View.VISIBLE
        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 0f
        binding.imgLogo.scaleY = 0f
        binding.imgLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500)
            .setInterpolator(OvershootInterpolator(1.4f)).start()
    }

    private fun animateTopText() {
        txtConnecting.translationY = -50f
        txtConnecting.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(txtConnecting, "alpha", 0f, 1f)
        val slideDown = ObjectAnimator.ofFloat(txtConnecting, "translationY", -60f, 0f)
        AnimatorSet().apply {
            playTogether(fadeIn, slideDown)
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateBottomText() {
        llBottomText.translationY = 50f
        llBottomText.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(llBottomText, "alpha", 0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(llBottomText, "translationY", 50f, 0f)
        AnimatorSet().apply {
            playTogether(fadeIn, slideUp)
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        underline.alpha = 1f
        underline.scaleX = 0f
        underline.pivotX = underline.width / 2f

        val expand = ObjectAnimator.ofFloat(underline, "scaleX", 1f)
        expand.duration = 1500
        expand.interpolator = AccelerateDecelerateInterpolator()
        expand.start()
    }

    private fun animateCardEntrance() {
        val scaleX = ObjectAnimator.ofFloat(binding.bigCard, "scaleX", 0.9f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.bigCard, "scaleY", 0.9f, 1f)
        val alpha = ObjectAnimator.ofFloat(binding.bigCard, "alpha", 0f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateFirstDotWithEmphasis() {
        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot1, "alpha", 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(binding.dot1, "scaleX", 0f, 2.5f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.dot1, "scaleY", 0f, 2.5f, 1f)
            val rotation = ObjectAnimator.ofFloat(binding.dot1, "rotation", 0f, 360f)

            AnimatorSet().apply {
                playTogether(alpha, scaleX, scaleY, rotation)
                duration = 600
                interpolator = OvershootInterpolator(1.5f)
                start()
            }

            binding.dot1.postDelayed({
                val scaleX = ObjectAnimator.ofFloat(binding.dot1, "scaleX", 1f, 1.4f)
                scaleX.repeatCount = ObjectAnimator.INFINITE
                scaleX.repeatMode = ObjectAnimator.REVERSE
                val scaleY = ObjectAnimator.ofFloat(binding.dot1, "scaleY", 1f, 1.4f)
                scaleY.repeatCount = ObjectAnimator.INFINITE
                scaleY.repeatMode = ObjectAnimator.REVERSE
                AnimatorSet().apply {
                    playTogether(scaleX, scaleY)
                    duration = 300
                    start()
                }
            }, 300)
        }, 500)
    }

    private fun animateRemainingDots() {
        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot2, "alpha", 0f, 1f)
            alpha.duration = 300
            alpha.start()
            val scaleX = ObjectAnimator.ofFloat(binding.dot2, "scaleX", 1f, 1.4f)
            scaleX.repeatCount = ObjectAnimator.INFINITE
            scaleX.repeatMode = ObjectAnimator.REVERSE
            val scaleY = ObjectAnimator.ofFloat(binding.dot2, "scaleY", 1f, 1.4f)
            scaleY.repeatCount = ObjectAnimator.INFINITE
            scaleY.repeatMode = ObjectAnimator.REVERSE
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
        }, 1000)

        binding.root.postDelayed({
            val alpha = ObjectAnimator.ofFloat(binding.dot3, "alpha", 0f, 1f)
            alpha.duration = 300
            alpha.start()
            val scaleX = ObjectAnimator.ofFloat(binding.dot3, "scaleX", 1f, 1.4f)
            scaleX.repeatCount = ObjectAnimator.INFINITE
            scaleX.repeatMode = ObjectAnimator.REVERSE
            val scaleY = ObjectAnimator.ofFloat(binding.dot3, "scaleY", 1f, 1.4f)
            scaleY.repeatCount = ObjectAnimator.INFINITE
            scaleY.repeatMode = ObjectAnimator.REVERSE
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
        }, 1250)
    }

    private fun animateLogoPulse() {
//        binding.root.postDelayed({
        val pulseScaleX = ObjectAnimator.ofFloat(binding.imgLogo, "scaleX", 1f, 1.1f, 1f)
        val pulseScaleY = ObjectAnimator.ofFloat(binding.imgLogo, "scaleY", 1f, 1.1f, 1f)
        AnimatorSet().apply {
            playTogether(pulseScaleX, pulseScaleY)
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
//        }, 3000)
    }

    override fun isToolBarNoticeCallTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.light_sky_blue_color)
            window.navigationBarColor = this.resources.getColor(R.color.bpWhite)
            window.setBackgroundDrawableResource(R.drawable.gradient_theme_parent)

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
        var instituteId: String? = null
        var receiverType: String? = null
        var receiverId: String? = null
        if (fromNotification) {
            menu_name = intent.getStringExtra(Constant.menu_name)
            headerId = intent.getStringExtra(Constant.header_id)
            instituteId = intent.getStringExtra(Constant.institute_id)
            receiverType = intent.getStringExtra(Constant.receiver_type)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_id = intent.getIntExtra(Constant.menu_id, 0)
            msg_id = intent.getIntExtra(Constant.msg_id, 0)
        }

        if (!SharedPreference.isLoggedIn(this)) {
            // Redirect to login
            val loginIntent = Intent(this, Login::class.java).apply {
                putExtra(Constant.menu_name, menu_name)
                putExtra(Constant.header_id, headerId)
                putExtra(Constant.receiver_type, receiverType)
                putExtra(Constant.receiverid, receiverId)
                putExtra(Constant.menu_id, menu_id)
                putExtra(Constant.msg_id, msg_id)
                putExtra(Constant.fromNotification, fromNotification)
            }
            startActivity(loginIntent)
            return
        }

        // Open the target screen only if launched from notification
        if (fromNotification) {
            when (true) {

                (menu_id == Constant.M_INTERACTION_WITH_STUDENT && receiverType == Constant.Staff___) -> {
                    val detailIntent = Intent(this, InteractionWithStudent::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.institute_id, instituteId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(InteractionWithStudent::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_LEAVE_REQUEST && receiverType == Constant.Staff___) -> {
                    val detailIntent = Intent(this, LeaveRequests::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.institute_id, instituteId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(LeaveRequests::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }


                (menu_id == Constant.M_MESSAGES_FROM_MANAGEMENT && receiverType == Constant.Staff___) -> {
                    val detailIntent = Intent(this, MessageFromManagement::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.institute_id, instituteId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(MessageFromManagement::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }


                (menu_id == Constant.M_ASSIGNMENT && receiverType == Constant.Staff___) -> {
                    val detailIntent = Intent(this, AssignmentReport::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.institute_id, instituteId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(MessageFromManagement::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }


                (menu_id == Constant.M_PTM && receiverType == Constant.Staff___) -> {
                    val detailIntent = Intent(
                        this,
                        com.vs.schoolmessenger.School.PTM.Activity.PTM::class.java
                    ).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.institute_id, instituteId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(com.vs.schoolmessenger.School.PTM.Activity.PTM::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    pendingIntent?.send()
                }

                // Student Notification Redirection

                (menu_id == Constant.M_COMMUNICATION && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, CommunicationParent::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
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


                (menu_id == Constant.M_HOMEWORK && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, HomeWork::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
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

                (menu_id == Constant.M_NOTICEBOARD && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, NoticeBoard::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
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

                (menu_id == Constant.M_LSRW && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, LSRW::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(LSRW::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_ASSIGNMENT && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, Assignment::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
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

                (menu_id == Constant.M_ATTACHMENTS && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, Attachment::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
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

                (menu_id == Constant.M_PARENT_CLASS_EVENTS && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, Event::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }

                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(Event::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_PTM && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, PTM::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(PTM::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_ATTENDANCE_REPORT && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, LeaveRequest::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(LeaveRequest::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_FEE_DETAILS && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, FeeDetails::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
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

                (menu_id == Constant.M_INTERACTION_WITH_STAFF && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, InteractionWithStaff::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(InteractionWithStaff::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_QUIZ_EXAM && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, Quiz::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(Quiz::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                (menu_id == Constant.M_MESSAGES_FROM_MANAGEMENT && receiverType == Constant.Student__) -> {
                    val detailIntent = Intent(this, MessageFromManagement::class.java).apply {
                        putExtra(Constant.menu_name, menu_name)
                        putExtra(Constant.header_id, headerId)
                        putExtra(Constant.receiver_type, receiverType)
                        putExtra(Constant.receiverid, receiverId)
                        putExtra(Constant.menu_id, menu_id)
                        putExtra(Constant.msg_id, msg_id)
                        putExtra(Constant.fromNotification, fromNotification)
                    }
                    // Build proper back stack
                    val pendingIntent = TaskStackBuilder.create(this).apply {
                        addParentStack(MessageFromManagement::class.java)
                        addNextIntent(detailIntent)
                    }.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.send()
                }

                else -> {
                    askNotificationPermission()
                }
            }
        } else {
            askNotificationPermission()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("goToNext", "goToNext4")

                goToNext()
            }
        } else {
            Log.d("goToNext", "goToNext5")

            goToNext()
        }
    }


    private fun isInterNetChecking() {
        lifecycleScope.launch {
            delay(4000) // 2-second delay
            withContext(Dispatchers.Main) {
                if (Constant.isInternetAvailable(this@Splash)) {
                    val countryId = SharedPreference.getCountryId(this@Splash)
                    Log.d("countryId", countryId.toString())
                    if (countryId != 0) {
                        isVersionCheck()
                    } else {
                        val isIntroductionSkip = SharedPreference.getIntroductionSkip(this@Splash)
                        if (isIntroductionSkip!!) {
                            startActivity(Intent(this@Splash, CountryScreen::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this@Splash, Introduction::class.java))
                            finish()
                        }
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
        val dialogView = layoutInflater.inflate(R.layout.update_available_popup, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            .setCancelable(false) // Prevent dismissing by clicking outside

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Remove default background
        alertDialog.show()

        val btnUpdateButton = dialogView.findViewById<TextView>(R.id.btnUpdate)
        val btnLater = dialogView.findViewById<TextView>(R.id.btnLater)

        if (versionData[0].force_update) {
            btnUpdateButton.visibility = View.VISIBLE
            btnLater.visibility = View.GONE
        } else {
            btnUpdateButton.visibility = View.VISIBLE
            btnLater.visibility = View.VISIBLE
        }

        btnUpdateButton.setOnClickListener {
            alertDialog.dismiss() // Close popup
            //startInAppUpdate()
            openPlayStore()
        }

        btnLater.setOnClickListener {
            alertDialog.dismiss() // Close popup
            autoLoginFlowCheck(isVersionData!!)

        }
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: Exception) {
            // Play Store not installed → open in browser
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
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