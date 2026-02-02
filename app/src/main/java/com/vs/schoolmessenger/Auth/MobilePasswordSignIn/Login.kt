package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.graphics.Paint
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.fingerPrintAunthenticateListener
import com.vs.schoolmessenger.databinding.LoginNewBinding

class Login : BaseActivity<LoginNewBinding>(), View.OnClickListener,
    fingerPrintAunthenticateListener {

    private var isPasswordVisible = false
    override fun getViewBinding(): LoginNewBinding {
        return LoginNewBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    var Mobile_Number: String? = ""
    var Password: String? = ""

    private var appViewModel: App? = null


    override fun setupViews() {
        super.setupViews()
        binding.imgHide.setOnClickListener(this)
        binding.btnLoginContinue.setOnClickListener(this)
        binding.lblForgetPassword.setOnClickListener(this)
        binding.rytFingerPrint.setOnClickListener(this)
        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        val mobile_number = SharedPreference.getMobileNumber(this)
        val password = SharedPreference.getPassWord(this)
        if (SharedPreference.isFingerprintEnabled(this)) {
            if (!mobile_number.equals("") && !password.equals("")) {
                binding.rytFingerPrint.visibility = View.VISIBLE

//                Constant.setupBiometricPrompt(this, this, false)
//                Constant.authenticate(this)

            } else {
                binding.rytFingerPrint.visibility = View.GONE
            }
        } else {
            binding.rytFingerPrint.visibility = View.GONE
        }
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        binding.lblForgetPassword.paintFlags =
            binding.lblForgetPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        binding.txtMobileNumber.hint = Constant.country_details!!.mobile_no_hint
        binding.txtMobileNumber.filters =
            arrayOf(InputFilter.LengthFilter(Constant.country_details!!.mobile_number_length))


        binding.txtPassword.setOnFocusChangeListener { _, hasFocus ->
            binding.rytPassword.isSelected = hasFocus
        }

        binding.imgHide.setOnFocusChangeListener { _, hasFocus ->
            binding.rytPassword.isSelected = hasFocus
        }


        authViewModel!!.isUserValidation?.observe(this) { response ->
            Constant.hideLoading(this@Login)
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {

                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobile_number)
                        addProperty(APIKeyNames.activity, Constant.add_points_login)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints("", jsonObject, this)

                    val isValidateUser = response.data
                    Constant.user_data = isValidateUser
                    Constant.user_details = Constant.user_data!![0].user_details
                    Constant.isStaffDetails = Constant.user_data!![0].user_details.staff_details
                    Constant.isChildDetails = Constant.user_data!![0].user_details.child_details
                    SharedPreference.putUserDetails(this@Login, Constant.user_details!!)

                    SharedPreference.putMobileNumberPassWord(
                        this@Login,
                        Mobile_Number,
                        Password
                    )

                    if (Constant.user_data!![0].is_number_exists) {
                        if (isValidateUser[0].is_password_updated) {
                            if (isValidateUser[0].otp_sent) {
                                val intent = Intent(this@Login, OTP::class.java)
                                Constant.pageType = Constant.SignInScreen
                                startActivity(intent)
                            } else {
                                SharedPreference.setLoggedIn(this, true)
                                if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                    val intent = Intent(this@Login, PrioritySelection::class.java)
                                    startActivity(intent)

                                } else if (Constant.user_data!![0].user_details.is_staff) {

                                    if (Constant.user_data!![0].user_details.staff_details.size > 1) {
                                        val intent =
                                            Intent(this@Login, PrioritySelection::class.java)
                                        startActivity(intent)
                                    } else {
                                        val intent = Intent(
                                            this@Login, SchoolDashboard::class.java
                                        )
                                        SharedPreference.putStaffDetails(
                                            this,
                                            Constant.user_data!![0].user_details.staff_details[0]
                                        )
                                        startActivity(intent)
                                    }
                                } else if (Constant.user_data!![0].user_details.is_parent) {
                                    if (Constant.user_data!![0].user_details.child_details.size > 1) {
                                        val intent =
                                            Intent(this@Login, PrioritySelection::class.java)
                                        startActivity(intent)
                                    } else {

                                        val intent = Intent(
                                            this@Login,
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
                            val intent = Intent(this@Login, OTP::class.java)
                            Constant.pageType = Constant.SignInScreen
                            Constant.isPasswordCreation = true
                            startActivity(intent)
                        }
                    } else {
                        Constant.errorAlert(this@Login, "", message)

                    }
                } else {
                    Constant.errorAlert(this@Login, "", message)
                }
            }
        }
        authViewModel!!.isForgetPassword?.observe(this) { response ->
            Constant.hideLoading(this@Login)
            if (response != null) {
                val status = response.status
                val message = response.message
                Constant.forgotData = response.data
                if (status) {
                    Constant.isMobileNumber = binding.txtMobileNumber.text.toString()
                    val intent = Intent(this@Login, OTP::class.java)
                    Constant.isForgotPassword = true
                    startActivity(intent)
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

//        Constant.setupEditTextWithScroll(
//            this,
//            binding.scrollRoot,
//            binding.txtPassword
//        )

    }

    private fun isValidMobileNumber(mobileNumber: String, password: String): Boolean {
        return mobileNumber.length == Constant.country_details!!.mobile_number_length.toInt() && mobileNumber.all { it.isDigit() } && !password.equals(
            ""
        )
    }

    private fun isForgetPassword() {
        Constant.showLoading(this@Login)
        val jsonObject = JsonObject()
        jsonObject.addProperty(
            APIKeyNames.Req_mobile_number, binding.txtMobileNumber.text.toString()
        )
        authViewModel!!.isForgetPassword(jsonObject, this)
    }


    private fun isPasswordViewAndHide() {
        if (isPasswordVisible) {
            binding.txtPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.imgHide.setImageResource(R.drawable.password_hide)
        } else {
            binding.txtPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.imgHide.setImageResource(R.drawable.password_view)
        }
        binding.txtPassword.setSelection(binding.txtPassword.text?.length ?: 0)
        isPasswordVisible = !isPasswordVisible
    }

    private fun isValidateUser(mobileNumber: String, password: String) {
        Constant.showLoading(this@Login)
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@Login)

        Constant.isMobileNumber = mobileNumber
        jsonObject.addProperty(
            APIKeyNames.Req_mobile_number, mobileNumber
        )
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_secure_id, isSecureId)
        jsonObject.addProperty(APIKeyNames.Req_password, password)

        authViewModel!!.isValidateUser(jsonObject, this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgHide -> {
                isPasswordViewAndHide()
            }

            R.id.btnLoginContinue -> {
                if (isUserNamePasswordValidation()) {
                    binding.btnLoginContinue.isEnabled = false
                    Mobile_Number = binding.txtMobileNumber.text.toString()
                    Password = binding.txtPassword.text.toString()
                    isValidateUser(Mobile_Number!!, Password!!)
                    binding.btnLoginContinue.postDelayed({
                        binding.btnLoginContinue.isEnabled = true
                    }, 500)
                }
            }


            R.id.lblForgetPassword -> {
                if (binding.txtMobileNumber.text.toString() != "" && binding.txtMobileNumber.text.toString().length == Constant.country_details!!.mobile_number_length.toInt()) {
                    isForgetPassword()
                } else {
                    Toast.makeText(this, R.string.EnterTheMobileNumber, Toast.LENGTH_SHORT).show()
                }
            }

            R.id.rytFingerPrint -> {
                Constant.setupBiometricPrompt(this, this, false)
                Constant.authenticate(this)
            }
        }
    }

    private fun isUserNamePasswordValidation(): Boolean {
        var isValidation = false
        if (isValidMobileNumber(
                binding.txtMobileNumber.text.toString(),
                binding.txtPassword.text.toString()
            )
        ) {
            isValidation = true
        } else {
            if (binding.txtMobileNumber.text.toString().length != Constant.country_details!!.mobile_number_length) {
                binding.txtMobileNumber.error =
                    resources.getString(R.string.Enter_the) + " " + Constant.country_details!!.mobile_number_length + " " + resources.getString(
                        R.string.digit_mobile_number
                    )
            } else if (binding.txtPassword.text.toString().equals("")) {
                Toast.makeText(this, getString(R.string.password_is_required), Toast.LENGTH_SHORT)
                    .show()
            }
            isValidation = false
        }
        return isValidation
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onAuthenticate(message: String, status: Boolean) {
        Log.d("athentication_status", message)
        if (status) {
            //go to dashboard
            Mobile_Number = SharedPreference.getMobileNumber(this)
            Password = SharedPreference.getPassWord(this)
            isValidateUser(Mobile_Number!!, Password!!)
        }


    }
}