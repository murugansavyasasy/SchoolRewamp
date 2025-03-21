package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MobileNumberBinding

class MobileNumber : BaseActivity<MobileNumberBinding>(), View.OnClickListener {

    override fun getViewBinding(): MobileNumberBinding {
        return MobileNumberBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()

        binding.btnLoginContinue.setOnClickListener(this)
        isToolBarWhiteTheme()

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        binding.txtMobileNumber.hint = Constant.country_details!!.mobile_no_hint

        binding.btnLoginContinue.isClickable = true
        binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))

        authViewModel!!.isUserValidation?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isValidateUser = response.data
//                    Constant.isUserValidationData = isValidateUser

                    binding.isLoading.visibility = View.GONE
                    binding.btnLoginContinue.isClickable = true
                    binding.btnLoginContinue.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))

                    if (Constant.isUserValidationData!![0].is_password_updated) {
                        val intent = Intent(this@MobileNumber, PassWord::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@MobileNumber, OTP::class.java)
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
    }

    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        return mobileNumber.length == Constant.country_details!!.mobile_number_length && mobileNumber.all { it.isDigit() }
    }

    private fun isValidateUser() {
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this@MobileNumber)
        jsonObject.addProperty(
            RequestKeys.Req_mobile_number,
            binding.txtMobileNumber.text.toString()
        )
        jsonObject.addProperty(RequestKeys.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(RequestKeys.Req_secure_id, isSecureId)
        authViewModel!!.isValidateUser(jsonObject, this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnLoginContinue -> {
                if (isValidMobileNumber(binding.txtMobileNumber.text.toString())) {
                    isValidateUser()
                }

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