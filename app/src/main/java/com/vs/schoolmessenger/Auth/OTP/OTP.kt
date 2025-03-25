package com.vs.schoolmessenger.Auth.OTP

import android.content.Intent
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PassWord
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.OtpScreenBinding

class OTP : BaseActivity<OtpScreenBinding>(), View.OnClickListener {

    private val otpTimeout = 30000L
    private val otpInterval = 1000L
    override fun getViewBinding(): OtpScreenBinding {
        return OtpScreenBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    var screenType : Int ?= 0

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        isToolBarWhiteTheme()
        binding.lblResend.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.lnrFirstNumber.setOnClickListener(this)
        binding.lnrSecondNumber.setOnClickListener(this)

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()
        binding.btnNext.isClickable = true
        binding.btnNext.setBackgroundDrawable(resources.getDrawable(R.drawable.rect_btn_orange))

        if(Constant.isForgotPassword!!){
            binding.lblEnter.text = Constant.forgotData!![0].forgot_otp_message
            binding.lblContactTitle.text = Constant.forgotData!![0].more_info
            val numberList: List<String> =
                Constant.forgotData!![0].dial_numbers.split(",")
            val firstNumber = numberList[0]
            val secondNumber = numberList[1]
            binding.lblNumberOne.text = firstNumber
            binding.lblNumberTwo.text = secondNumber
        }
        else {

            if (Constant.user_data!!.isNotEmpty()) {
                binding.lblEnter.text = Constant.user_data!![0].message
                binding.lblContactTitle.text = Constant.user_data!![0].more_info
                val numberList: List<String> =
                    Constant.user_data!![0].dial_numbers.split(",")
                val firstNumber = numberList[0]
                val secondNumber = numberList[1]
                binding.lblNumberOne.text = firstNumber
                binding.lblNumberTwo.text = secondNumber
            }
        }


        authViewModel!!.isOtpResponse?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {

                    if(Constant.isForgotPassword!!){
                        val intent = Intent(this@OTP, PasswordGeneration::class.java)
                        Constant.isPasswordCreation = false
                        startActivity(intent)
                    }
                    else if(Constant.user_data!![0].is_password_updated){

                        if(Constant.pageType == Constant.MobileNumberScreen){
                            val intent = Intent(this@OTP, PassWord::class.java)
                            startActivity(intent)
                        }

                        else {
                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@OTP, PrioritySelection::class.java)
                                startActivity(intent)

                            } else if (Constant.user_data!![0].user_details.is_staff) {

                                val intent = Intent(
                                    this@OTP,
                                    SchoolDashboard::class.java
                                )
                                startActivity(intent)
                            } else if (Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(
                                    this@OTP,
                                    com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard::class.java
                                )
                                startActivity(intent)
                            }
                        }
                    }
                    else {
                        val intent = Intent(this@OTP, PasswordGeneration::class.java)
                        Constant.isPasswordCreation = true
                        startActivity(intent)
                    }
                }

            }
        }

        setOtpInputListener(binding.txtOtp1, binding.txtOtp2, null)
        setOtpInputListener(binding.txtOtp2, binding.txtOtp3, binding.txtOtp1)
        setOtpInputListener(binding.txtOtp3, binding.txtOtp4, binding.txtOtp2)
        setOtpInputListener(binding.txtOtp4, binding.txtOtp5, binding.txtOtp3)
        setOtpInputListener(binding.txtOtp5, binding.txtOtp6, binding.txtOtp4)
        setOtpInputListener(binding.txtOtp6, null, binding.txtOtp5)

        startOtpTimer()
    }

    private fun isOtpValidate(isOpt: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(RequestKeys.Req_otp, isOpt)
        authViewModel!!.isOtpResponse(jsonObject, this)
    }

    private fun setOtpInputListener(
        currentEditText: EditText, nextEditText: EditText?, previousEditText: EditText?
    ) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                if (s?.length == 1 && after == 0) {
                    previousEditText?.requestFocus() // Move focus to the previous EditText
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("OnTextChanged", "OnTextChanged")
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    nextEditText?.requestFocus() // Move focus to the next EditText
                }
            }
        })
    }


    private fun startOtpTimer() {

        object : CountDownTimer(otpTimeout, otpInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.lblOtpTimer.text = String.format("00:%02d", secondsRemaining)
            }

            override fun onFinish() {
                binding.lblOtpTimer.text = "00:00"
                binding.lblOtpTimer.visibility = View.GONE
                binding.lblResend.visibility = View.VISIBLE
            }
        }.start()
    }


    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.lblResend -> {
                binding.lblOtpTimer.visibility = View.VISIBLE
                binding.lblResend.visibility = View.GONE
                startOtpTimer()
            }

            R.id.btnNext -> {
                if (binding.txtOtp1.text.toString() != "" && binding.txtOtp2.text.toString() != "" && binding.txtOtp3.text.toString() != "" && binding.txtOtp4.text.toString() != "" && binding.txtOtp5.text.toString() != "" && binding.txtOtp6.text.toString() != "") {
                    val isOpt =
                        binding.txtOtp1.text.toString() + binding.txtOtp2.text.toString() + binding.txtOtp3.text.toString() + binding.txtOtp4.text.toString() + binding.txtOtp5.text.toString() + binding.txtOtp6.text.toString()
                    binding.btnNext.isClickable = false
                    binding.isLoading.visibility = View.VISIBLE
                    binding.btnNext.setBackgroundColor(resources.getColor(R.color.mild_grey0))
                    isOtpValidate(isOpt)
                } else {
                    binding.btnNext.isClickable = true
                    binding.isLoading.visibility = View.GONE
                    Toast.makeText(this, R.string.EnterTheOtp, Toast.LENGTH_SHORT).show()
                }
            }

            R.id.lnrFirstNumber -> {
                Constant.redirectToDialPad(this, binding.lblNumberOne.text.toString())
            }

            R.id.lnrSecondNumber -> {
                Constant.redirectToDialPad(this, binding.lblNumberTwo.text.toString())
            }
        }
    }
}