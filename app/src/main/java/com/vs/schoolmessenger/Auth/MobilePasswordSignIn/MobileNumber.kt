package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MobileNumberNewBinding

class MobileNumber : BaseActivity<MobileNumberNewBinding>(), View.OnClickListener {

    override fun getViewBinding(): MobileNumberNewBinding {
        return MobileNumberNewBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.rytBack.setOnClickListener(this)

        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()


        binding.btnLoginContinue.setOnClickListener {
            binding.btnLoginContinue.isEnabled = false
            if (isValidMobileNumber(binding.txtMobileNumber.text.toString())) {
                Constant.isMobileNumber = binding.txtMobileNumber.text.toString()
                isValidateUser()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.enter_a_valid_mobile_number),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.btnLoginContinue.postDelayed({
                binding.btnLoginContinue.isEnabled = true
            }, 500)
        }

        binding.txtMobileNumber.hint = Constant.country_details!!.mobile_no_hint
        binding.txtMobileNumber.filters =
            arrayOf(InputFilter.LengthFilter(Constant.country_details!!.mobile_number_length))


        authViewModel!!.isUserValidation?.observe(this) { response ->
            Constant.hideLoading(this@MobileNumber)
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isValidateUser = response.data
                    Constant.user_data = isValidateUser
                    Constant.user_details = Constant.user_data!![0].user_details
                    Constant.isStaffDetails = Constant.user_data!![0].user_details.staff_details
                    Constant.isChildDetails = Constant.user_data!![0].user_details.child_details
                    SharedPreference.putUserDetails(this@MobileNumber, Constant.user_details!!)

                    if (Constant.user_data!![0].is_number_exists) {
                        if (Constant.user_data!![0].is_password_updated) {
                            if (Constant.user_data!![0].otp_sent) {
                                val intent = Intent(this@MobileNumber, OTP::class.java)
                                Constant.pageType = Constant.MobileNumberScreen
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@MobileNumber, PassWord::class.java)
                                startActivity(intent)
                            }
                        } else {
                            val intent = Intent(this@MobileNumber, OTP::class.java)
                            Constant.pageType = Constant.MobileNumberScreen
                            Constant.isPasswordCreation = true
                            startActivity(intent)
                        }
                    } else {
                        Constant.errorAlert(
                            this@MobileNumber,
                            getString(R.string.Oops), message
                        )
                    }
                } else {
                    Constant.errorAlert(
                        this@MobileNumber,
                        getString(R.string.Oops), message
                    )
                }
            }
        }
    }


    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        return mobileNumber.length == Constant.country_details!!.mobile_number_length && mobileNumber.all { it.isDigit() }
    }

    private fun isValidateUser() {
        Constant.showLoading(this@MobileNumber)
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@MobileNumber)
        jsonObject.addProperty(
            APIKeyNames.Req_mobile_number,
            binding.txtMobileNumber.text.toString()
        )
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_secure_id, isSecureId)
        authViewModel!!.isValidateUser(jsonObject, this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rytBack -> {
                onBackPressed()
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