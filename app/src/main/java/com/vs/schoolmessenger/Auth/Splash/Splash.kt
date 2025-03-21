package com.vs.schoolmessenger.Auth.Splash

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.Country.CountryScreen
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.Dashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SplashBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Splash : BaseActivity<SplashBinding>(), View.OnClickListener {

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(newBase)
        val context = ChangeLanguage.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
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

        if (Constant.isInternetAvailable(this)) {
            val countryId = SharedPreference.getCountryId(this)
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

        authViewModel!!.isUserValidation?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isValidateUser = response.data

                    Constant.isUserValidationData = isValidateUser

                    if (Constant.isUserValidationData!![0].user_details.is_staff && Constant.isUserValidationData!![0].user_details.is_parent) {
                        val isUserDetails = Constant.isUserValidationData!![0].user_details
                        val isStaffDetails = isUserDetails.staff_details
                        Constant.isStaffDetails = isStaffDetails
                        val isParentDetails = isUserDetails.child_details
                        Constant.isParentDetails = isParentDetails

                    } else if (Constant.isUserValidationData!![0].user_details.is_staff) {
                        val isUserDetails = Constant.isUserValidationData!![0].user_details
                        val isStaffDetails = isUserDetails.staff_details
                        Constant.isStaffDetails = isStaffDetails

                    } else if (Constant.isUserValidationData!![0].user_details.is_parent) {
                        val isUserDetails = Constant.isUserValidationData!![0].user_details
                        val isParentDetails = isUserDetails.child_details
                        Constant.isParentDetails = isParentDetails
                    }
                    isProceedNextScreen()
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

                    isValidateUser()

                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun isProceedNextScreen() {
        GlobalScope.launch {
            delay(2000) // 2-second delay
            withContext(Dispatchers.Main) {
                if (Constant.isUserValidationData!![0].otp_sent) {
                    val intent = Intent(this@Splash, OTP::class.java)
                    startActivity(intent)
                } else {
                    if (Constant.isUserValidationData!![0].user_details.is_parent && Constant.isUserValidationData!![0].user_details.is_staff) {
                        val intent = Intent(this@Splash, PrioritySelection::class.java)
                        startActivity(intent)
                    } else {
                        if (Constant.isUserValidationData!![0].user_details.is_parent) {
                            val intent = Intent(
                                this@Splash,
                                com.vs.schoolmessenger.Dashboard.Parent.Dashboard::class.java
                            )
                            startActivity(intent)
                        } else {
                            val intent = Intent(
                                this@Splash,
                                Dashboard::class.java
                            )
                            startActivity(intent)
                        }
                    }
                }
            }
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
}