package com.vs.schoolmessenger.Auth.OTP

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Build
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PassWord
import com.vs.schoolmessenger.Auth.Splash.Splash
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.MySMSBroadcastReceiver
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.OtpScreenBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.vs.schoolmessenger.Repository.APIKeyNames

class OTP : BaseActivity<OtpScreenBinding>(), View.OnClickListener {

    private val otpTimeout = 30000L
    private val otpInterval = 1000L
    override fun getViewBinding(): OtpScreenBinding {
        return OtpScreenBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    private lateinit var smsBroadcastReceiver: MySMSBroadcastReceiver


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        isToolBarWhiteTheme()
        binding.lblResend.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.lblContactUs.setOnClickListener(this)

        startSmsRetriever()
        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()
        isOtpTitleLoad()
        binding.lblContactUs.paintFlags =
            binding.lblContactUs.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        authViewModel!!.isOtpResponse?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {

//                    SharedPreference.putMobileNumberPassWord(
//                        this@OTP,
//                        Constant.isMobileNumber,
//                        password
//                    )

                    if (Constant.isForgotPassword!!) {
                        val intent = Intent(this@OTP, PasswordGeneration::class.java)
                        Constant.isPasswordCreation = false
                        startActivity(intent)
                    } else if (Constant.user_data!![0].is_password_updated) {

                        if (Constant.pageType == Constant.MobileNumberScreen) {
                            val intent = Intent(this@OTP, PassWord::class.java)
                            startActivity(intent)
                        } else {

                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@OTP, PrioritySelection::class.java)
                                startActivity(intent)

                            } else if (Constant.user_data!![0].user_details.is_staff) {

                                if (Constant.user_data!![0].user_details.staff_role.equals(Constant.isStaffRole)) {

                                    if (Constant.user_data!![0].user_details.staff_details.size > 1) {
                                        val intent =
                                            Intent(this@OTP, PrioritySelection::class.java)
                                        startActivity(intent)
                                    } else {
                                        val intent = Intent(
                                            this@OTP,
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
                                        this@OTP,
                                        SchoolDashboard::class.java
                                    )
                                    startActivity(intent)
                                }
                            } else if (Constant.user_data!![0].user_details.is_parent) {
                                Constant.isParentChoose = true
                                if (Constant.user_data!![0].user_details.child_details.size > 1) {
                                    val intent = Intent(this@OTP, PrioritySelection::class.java)
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(
                                        this@OTP,
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
                        val intent = Intent(this@OTP, PasswordGeneration::class.java)
                        Constant.isPasswordCreation = true
                        startActivity(intent)
                    }
                }

            }
        }

        authViewModel!!.isForgetPassword?.observe(this) { response ->
            if (response != null) {
                Constant.forgotData = response.data
                isOtpTitleLoad()
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        smsBroadcastReceiver = MySMSBroadcastReceiver().apply {
            otpListener = { otp ->
                runOnUiThread {
                    Log.d("YOUR OTP", otp)
                    otp.forEachIndexed { index, char ->
                        when (index) {
                            0 -> {
                                binding.txtOtp1.setText(char.toString())
                            }

                            1 -> {
                                binding.txtOtp2.setText(char.toString())
                            }

                            2 -> {
                                binding.txtOtp3.setText(char.toString())
                            }

                            3 -> {
                                binding.txtOtp4.setText(char.toString())
                            }

                            4 -> {
                                binding.txtOtp5.setText(char.toString())
                            }

                            5 -> {
                                binding.txtOtp6.setText(char.toString())
                            }
                        }

                    }
                    isOtpValidate(otp)
                }
            }
        }

        smsBroadcastReceiver = MySMSBroadcastReceiver()
        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(smsBroadcastReceiver)
    }

    private fun startSmsRetriever() {
        val client = SmsRetriever.getClient(this)
        val task = client.startSmsRetriever()

        task.addOnSuccessListener {
            Log.d("OTP", "SMS Retriever started")
        }

        task.addOnFailureListener {
            Log.e("OTP", "Failed to start SMS Retriever")
        }
    }

    private fun isOtpValidate(isOpt: String) {
        val jsonObject = JsonObject()
        val isSecureId = Constant.getAndroidSecureId(this)
        jsonObject.addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
        jsonObject.addProperty(APIKeyNames.Req_secure_id, isSecureId)
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(APIKeyNames.Req_otp, isOpt)
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
                binding.lblOtpTimer.text = String.format(Constant.time_forMate, secondsRemaining)
            }

            override fun onFinish() {
                binding.lblOtpTimer.text = Constant.time_zero
                binding.lblOtpTimer.visibility = View.GONE
                binding.lblResend.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun isForgetPassword() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
        authViewModel!!.isForgetPassword(jsonObject, this)
    }


    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.lblResend -> {
                binding.lblOtpTimer.visibility = View.VISIBLE
                binding.lblResend.visibility = View.GONE
                startOtpTimer()
                isForgetPassword()
            }

            R.id.btnNext -> {
                if (binding.txtOtp1.text.toString() != "" && binding.txtOtp2.text.toString() != "" && binding.txtOtp3.text.toString() != "" && binding.txtOtp4.text.toString() != "" && binding.txtOtp5.text.toString() != "" && binding.txtOtp6.text.toString() != "") {
                    val isOpt =
                        binding.txtOtp1.text.toString() + binding.txtOtp2.text.toString() + binding.txtOtp3.text.toString() + binding.txtOtp4.text.toString() + binding.txtOtp5.text.toString() + binding.txtOtp6.text.toString()
                    binding.isLoading.visibility = View.GONE
                    isOtpValidate(isOpt)
                } else {
                    binding.isLoading.visibility = View.GONE
                    Toast.makeText(this, R.string.EnterTheOtp, Toast.LENGTH_SHORT).show()
                }
            }

            R.id.lblContactUs -> {
                isShowContactNumber()
            }
        }
    }


    private fun isShowContactNumber() {

        var isFirstNumber = ""
        var isSecondNumber = ""

        if (Constant.isForgotPassword!!) {
            val numberList: List<String> =
                Constant.forgotData!![0].dial_numbers.split(",")
            isFirstNumber = numberList[0]
            isSecondNumber = numberList[1]

        } else {
            if (Constant.user_data!!.isNotEmpty()) {
                val numberList: List<String> =
                    Constant.user_data!![0].dial_numbers.split(",")
                isFirstNumber = numberList[0]
                isSecondNumber = numberList[1]
            }
        }

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_textview_showing, null)

        val isNumberOne = view.findViewById<TextView>(R.id.txtNumberOne)
        val isNumberTwo = view.findViewById<TextView>(R.id.txtNumberTwo)

        isNumberOne.text = isFirstNumber
        isNumberTwo.text = isSecondNumber

        // Handle click events on text views
        isNumberOne.setOnClickListener {
            Constant.redirectToDialPad(this, isNumberOne.text.toString())
            dialog.dismiss()
        }

        isNumberTwo.setOnClickListener {
            Constant.redirectToDialPad(this, isNumberTwo.text.toString())
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun isOtpTitleLoad() {
        if (Constant.isForgotPassword == true) {
            // Check if forgotData is not null or empty
            if (Constant.forgotData != null && Constant.forgotData!!.isNotEmpty()) {
                binding.lblEnter.text = Constant.forgotData!![0].forgot_otp_message
                binding.lblContactTitle.text = Constant.forgotData!![0].more_info
            } else {
                // Handle the case when forgotData is null or empty
                Log.e("OTP", "forgotData is null or empty")
            }
        } else {
            // Check if user_data is not null or empty
            if (Constant.user_data != null && Constant.user_data!!.isNotEmpty()) {
                binding.lblEnter.text = Constant.user_data!![0].message
                binding.lblContactTitle.text = Constant.user_data!![0].more_info
            } else {
                // Handle the case when user_data is null or empty
                Log.e("OTP", "user_data is null or empty")
            }
        }
    }

}