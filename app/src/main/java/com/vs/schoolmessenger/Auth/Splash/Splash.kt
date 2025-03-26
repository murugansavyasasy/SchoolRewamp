package com.vs.schoolmessenger.Auth.Splash

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.Country.CountryScreen
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability


class Splash : BaseActivity<SplashBinding>(), View.OnClickListener {

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(newBase)
        val context = ChangeLanguage.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Update failed!",
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

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        appUpdateManager = AppUpdateManagerFactory.create(this)


        GlobalScope.launch {
            delay(2000) // 2-second delay
            withContext(Dispatchers.Main) {
                if (Constant.isInternetAvailable(this@Splash)) {
                    val countryId = SharedPreference.getCountryId(this@Splash)
                    Log.d("countryId", countryId.toString())
                    if (!countryId.equals("")) {
                        isVersionCheck()
                    } else {
                        val intent = Intent(this@Splash, CountryScreen::class.java)
                        startActivity(intent)
                    }
                } else {
                    Log.e("Network Error", "No Internet Connection")
                }
            }
        }

        authViewModel!!.isUserValidation?.observe(this) { response ->
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

                    if (isValidateUser[0].is_password_updated) {
                        if (isValidateUser[0].otp_sent) {
                            val intent = Intent(this@Splash, OTP::class.java)
                            Constant.pageType = Constant.SplashScreen
                            startActivity(intent)
                        } else {
                            val mobile_number = SharedPreference.getMobileNumber(this)
                            val password = SharedPreference.getPassWord(this)
                            Constant.isMobileNumber = mobile_number
                            SharedPreference.putMobileNumberPassWord(
                                this@Splash,
                                mobile_number,
                                password
                            )
                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@Splash, PrioritySelection::class.java)
                                startActivity(intent)

                            } else if (Constant.user_data!![0].user_details.is_staff) {
                                val intent = Intent(
                                    this@Splash,
                                    SchoolDashboard::class.java
                                )
                                startActivity(intent)
                            } else if (Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(
                                    this@Splash,
                                    com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard::class.java
                                )
                                startActivity(intent)
                            }
                        }
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
                    SharedPreference.putCountryId(this, Constant.country_details!!.id.toString())
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

    private fun autoLoginFlowCheck(isVersionData: List<VersionData>) {
        val mobile_number = SharedPreference.getMobileNumber(this)
        val password = SharedPreference.getPassWord(this)
        if (!mobile_number.equals("") && !password.equals("")) {
            isValidateUser()
        } else {
            val intent = Intent(this@Splash, Login::class.java)
            startActivity(intent)
        }
    }

    private fun isValidateUser() {
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@Splash)
        val isMobileNumber = SharedPreference.getMobileNumber(this)
        jsonObject.addProperty(RequestKeys.Req_mobile_number, isMobileNumber)
        jsonObject.addProperty(RequestKeys.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(RequestKeys.Req_secure_id, isSecureId)
        val isPassWord = SharedPreference.getPassWord(this)
        jsonObject.addProperty(RequestKeys.Req_password, isPassWord)
        authViewModel!!.isValidateUser(jsonObject, this)
    }

    private fun isVersionCheck() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(RequestKeys.Req_version_code, Constant.isVersionId)
        jsonObject.addProperty(RequestKeys.Req_country_id, SharedPreference.getCountryId(this))
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
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
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
}