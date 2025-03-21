package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.text.InputType
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
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

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()


        binding.btnLoginContinue.isClickable = true
        binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))

        authViewModel!!.isUserValidation?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isValidateUser = response.data

//                    SharedPreference.(this,binding.txtPassword.text.toString())

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

                    binding.isLoading.visibility = View.GONE
                    binding.btnLoginContinue.isClickable = true
                    binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))

                    if (!Constant.isUserValidationData!![0].otp_sent) {
                        val intent = Intent(this@PassWord, PrioritySelection::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@PassWord, OTP::class.java)
                        startActivity(intent)
                    }

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
                if (status) {
                    val intent = Intent(this@PassWord, OTP::class.java)
                    Constant.isPassWordCreateType = 2
                    startActivity(intent)
                }
            }
        }
    }


    private fun isForgetPassword() {
        val jsonObject = JsonObject()
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
        val isSecureId = Constant.getAndroidSecureId(this@PassWord)
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
                isValidateUser()
            }

            R.id.lblForgetPassword -> {
                    isForgetPassword()
//                } else {
//                    Toast.makeText(this, R.string.EnterTheMobileNumber, Toast.LENGTH_SHORT).show()
//                }
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