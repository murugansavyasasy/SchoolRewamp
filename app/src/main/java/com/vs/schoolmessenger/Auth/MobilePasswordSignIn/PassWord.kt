package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.graphics.Paint
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Auth.Splash.Splash
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PassWordBinding

class PassWord : BaseActivity<PassWordBinding>(), View.OnClickListener {

    private var isPasswordVisible = false
    override fun getViewBinding(): PassWordBinding {
        return PassWordBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        binding.imgHide.setOnClickListener(this)
        binding.btnLoginContinue.setOnClickListener(this)
        binding.lblForgetPassword.setOnClickListener(this)
        isToolBarWhiteTheme()
        binding.lblForgetPassword.paintFlags =
            binding.lblForgetPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()


        authViewModel!!.isUserValidation?.observe(this) { response ->
            Constant.hideLoading(this@PassWord)
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isValidateUser = response.data
                    Constant.user_data = isValidateUser
                    Constant.user_details = Constant.user_data!![0].user_details
                    Constant.isStaffDetails = Constant.user_data!![0].user_details.staff_details
                    Constant.isChildDetails = Constant.user_data!![0].user_details.child_details

                    SharedPreference.putUserDetails(this@PassWord, Constant.user_details!!)
                    SharedPreference.putMobileNumberPassWord(
                        this@PassWord,
                        Constant.isMobileNumber,
                        binding.txtPassword.text.toString()
                    )
                    if (isValidateUser[0].is_password_updated) {

                        SharedPreference.putMobileNumberPassWord(
                            this@PassWord,
                            Constant.isMobileNumber,
                            binding.txtPassword.text.toString()
                        )

                        if (isValidateUser[0].otp_sent) {
                            val intent = Intent(this@PassWord, OTP::class.java)
                            Constant.pageType = Constant.PasswordScreen
                            startActivity(intent)
                        } else {
                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@PassWord, PrioritySelection::class.java)
                                startActivity(intent)

                            } else if (Constant.user_data!![0].user_details.is_staff) {

                                if (Constant.user_data!![0].user_details.staff_role.equals(Constant.isStaffRole)) {

                                    if (Constant.user_data!![0].user_details.staff_details.size > 1) {
                                        val intent =
                                            Intent(this@PassWord, PrioritySelection::class.java)
                                        startActivity(intent)
                                    } else {
                                        val intent = Intent(
                                            this@PassWord,
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
                                        this@PassWord,
                                        SchoolDashboard::class.java
                                    )
                                    startActivity(intent)
                                }
                            } else if (Constant.user_data!![0].user_details.is_parent) {
                                if (Constant.user_data!![0].user_details.child_details.size > 1) {
                                    val intent =
                                        Intent(this@PassWord, PrioritySelection::class.java)
                                    startActivity(intent)
                                } else {
                                    Constant.isParentChoose = true
                                    val intent = Intent(
                                        this@PassWord,
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
                    }


                }
            }
        }
        authViewModel!!.isForgetPassword?.observe(this) { response ->
            Constant.hideLoading(this@PassWord)
            if (response != null) {
                val status = response.status
                val message = response.message
                Constant.forgotData = response.data

                if (status) {
                    val intent = Intent(this@PassWord, OTP::class.java)
                    Constant.isForgotPassword = true
                    startActivity(intent)
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun isForgetPassword() {
        Constant.showLoading(this@PassWord)
        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
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
        Constant.showLoading(this@PassWord)
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@PassWord)
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_secure_id, isSecureId)
        jsonObject.addProperty(APIKeyNames.Req_password, binding.txtPassword.text.toString())

        authViewModel!!.isValidateUser(jsonObject, this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgHide -> {
                isPasswordViewAndHide()
            }

            R.id.btnLoginContinue -> {

                if (binding.lblPassword.text.toString() != "") {
                    isValidateUser()
                } else {
                    Toast.makeText(
                        this, resources.getString(R.string.EnterThePassWord), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            R.id.lblForgetPassword -> {
                isForgetPassword()
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}