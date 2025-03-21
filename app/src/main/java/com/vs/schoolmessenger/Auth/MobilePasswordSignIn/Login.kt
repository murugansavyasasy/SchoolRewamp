package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.Dashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LoginBinding

class Login : BaseActivity<LoginBinding>(), View.OnClickListener {

    private var isPasswordVisible = false
    override fun getViewBinding(): LoginBinding {
        return LoginBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        binding.imgHide.setOnClickListener(this)
        binding.btnLoginContinue.setOnClickListener(this)
        binding.lblForgetPassword.setOnClickListener(this)
        isToolBarWhiteTheme()

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        binding.btnLoginContinue.isClickable = true
        binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))
        binding.txtMobileNumber.hint = Constant.country_details!!.mobile_no_hint

        authViewModel!!.isUserValidation?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isValidateUser = response.data
                    Constant.user_data = isValidateUser
                    Constant.user_details = Constant.user_data!![0].user_details
                    Constant.isStaffDetails= Constant.user_data!![0].user_details.staff_details
                    Constant.isChildDetails= Constant.user_data!![0].user_details.child_details

                    if(isValidateUser[0].is_password_updated) {

                        if (isValidateUser[0].otp_sent) {
                            val intent = Intent(this@Login, OTP::class.java)
                            startActivity(intent)
                        } else {

                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@Login, PrioritySelection::class.java)
                                startActivity(intent)

                            } else if (Constant.user_data!![0].user_details.is_staff) {

                                val intent = Intent(
                                    this@Login,
                                    Dashboard::class.java
                                )
                                startActivity(intent)
                            } else if (Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(
                                    this@Login,
                                    com.vs.schoolmessenger.Dashboard.Parent.Dashboard::class.java
                                )
                                startActivity(intent)

                            }
                        }
                    }

                    binding.isLoading.visibility = View.GONE
                    binding.btnLoginContinue.isClickable = true
                    binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))
                } else {
                    binding.isLoading.visibility = View.GONE
                    binding.btnLoginContinue.isClickable = true
                    binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))
                }
            } else {
                binding.isLoading.visibility = View.GONE
                binding.btnLoginContinue.isClickable = true
                binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))
            }
        }
        authViewModel!!.isForgetPassword?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                Constant.forgotData = response.data
                if (status) {
                    val intent = Intent(this@Login, OTP::class.java)
                    Constant.isForgotPassword = true
                    startActivity(intent) }
                else
                {

                }
            }
        }
    }

    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        return mobileNumber.length == Constant.country_details!!.mobile_number_length.toInt() && mobileNumber.all { it.isDigit() }
    }

    private fun isForgetPassword() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(
            RequestKeys.Req_mobile_number, binding.txtMobileNumber.text.toString()
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

    private fun isValidateUser() {
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@Login)

        val isMobileNumber = SharedPreference.getMobileNumber(this)
        jsonObject.addProperty(RequestKeys.Req_mobile_number, isMobileNumber)
        jsonObject.addProperty(RequestKeys.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(RequestKeys.Req_secure_id, isSecureId)
        jsonObject.addProperty(RequestKeys.Req_password, binding.txtPassword.text.toString())

        authViewModel!!.isValidateUser(jsonObject, this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgHide -> {
                isPasswordViewAndHide()
            }

            R.id.btnLoginContinue -> {
                if (isUserNamePasswordValidation()) {
                    binding.btnLoginContinue.isClickable = false
                    binding.isLoading.visibility = View.VISIBLE
                    binding.btnLoginContinue.setBackgroundColor(resources.getColor(R.color.mild_grey0))
                    isValidateUser()
                }
            }

            R.id.lblForgetPassword -> {
                if (binding.txtMobileNumber.text.toString() != "" && binding.txtMobileNumber.text.toString().length == Constant.country_details!!.mobile_number_length.toInt()) {
                    isForgetPassword()
                } else {
                    Toast.makeText(this, R.string.EnterTheMobileNumber, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isUserNamePasswordValidation(): Boolean {
        var isValidation = false
        if (isValidMobileNumber(binding.txtMobileNumber.text.toString())) {
            isValidation = true
        } else {
            binding.txtMobileNumber.error =
                "Enter the " + Constant.country_details!!.mobile_number_length + " digit's mobile number"
            isValidation = false
        }
        return isValidation
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}