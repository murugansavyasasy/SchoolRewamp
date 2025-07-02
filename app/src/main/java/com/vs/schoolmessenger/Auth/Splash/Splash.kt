package com.vs.schoolmessenger.Auth.Splash

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
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
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.AppSignatureHelper
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
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

    override fun setupViews() {
        super.setupViews()
        isToolBarTheme()

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()

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
                isInterNetChecking()
                Log.d("PermissionResult", "✅ User clicked ALLOW for notification permission")
            } else {
                isInterNetChecking()
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
                            SharedPreference.putMobileNumberPassWord(
                                this@Splash,
                                mobile_number,
                                password
                            )
                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@Splash, PrioritySelection::class.java)
                                startActivity(intent)
                            } else if (Constant.user_data!![0].user_details.is_staff) {

                                if (Constant.user_data!![0].user_details.staff_role.equals(Constant.isStaffRole)) {

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
                                } else {
                                    val intent = Intent(
                                        this@Splash,
                                        SchoolDashboard::class.java
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
            }
        }

        authViewModel!!.isVersionCheck?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                isInterNetChecking()
            }
        } else {
            isInterNetChecking()
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

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        resources.getString(R.string.Authentication_Successful),
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigate to the next screen after authentication
                    startActivity(Intent(this@Splash, CountryScreen::class.java))
                    finish()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication Error: $errString",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext,
                        resources.getString(R.string.Authentication_Failed),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(resources.getString(R.string.biometric_authentications))
            .setSubtitle(resources.getString(R.string.fingerprint_authenticate))
            .setNegativeButtonText(resources.getString(R.string.Cancel))
            .build()

        biometricPrompt.authenticate(promptInfo);
    }

    private fun autoLoginFlowCheck(isVersionData: List<VersionData>) {
        val mobile_number = SharedPreference.getMobileNumber(this)
        val password = SharedPreference.getPassWord(this)
        Log.d("mobile_number", mobile_number.toString())
        Log.d("password", password.toString())
        if (!mobile_number.equals("") && !password.equals("")) {
            isValidateUser()
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

    fun showBottomPopup(context: Context) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet, null)
        dialog.setContentView(view)
        val btnClose = view.findViewById<CardView>(R.id.btnClose)
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


    fun isNoInterNet() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.no_internet_connection, null)
        val lottieView =
            dialogView.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnCreate = dialogView.findViewById<CardView>(R.id.btnCreate)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCreate.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            this.startActivity(intent)
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    override fun onAuthenticate(message: String, status: Boolean) {
        Log.d("athentication_status", message)
        if (status) {
            //go to dashboard
            isValidateUser()
        } else {
//            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}