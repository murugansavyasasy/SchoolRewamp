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
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.PassWord
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.MySMSBroadcastReceiver
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.OtpNewBinding

class OTP : BaseActivity<OtpNewBinding>(), View.OnClickListener {

    private val otpTimeout = 30000L
    private val otpInterval = 1000L
    override fun getViewBinding(): OtpNewBinding {
        return OtpNewBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    private lateinit var smsBroadcastReceiver: MySMSBroadcastReceiver


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryThemePassword(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.lblResend.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.lblContactUs.setOnClickListener(this)
        binding.rytHeader.setOnClickListener(this)

        startSmsRetriever()
        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()
        isOtpTitleLoad()
        binding.lblContactUs.paintFlags =
            binding.lblContactUs.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        setupOtp()

        authViewModel!!.isOtpResponse?.observe(this) { response ->
            Constant.hideLoading(this@OTP)
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    if (Constant.isForgotPassword!!) {
                        val intent = Intent(this@OTP, PasswordGeneration::class.java)
                        intent.putExtra("type", "forgot")                 // Int
                        Constant.isPasswordCreation = false
                        startActivity(intent)
                    } else if (Constant.user_data!![0].is_password_updated) {

                        if (Constant.pageType == Constant.MobileNumberScreen) {
                            val intent = Intent(this@OTP, PassWord::class.java)
                            startActivity(intent)
                        } else {
                            SharedPreference.setLoggedIn(this, true)
                            if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                                val intent = Intent(this@OTP, PrioritySelection::class.java)
                                startActivity(intent)

                            } else if (Constant.user_data!![0].user_details.is_staff) {

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
                        intent.putExtra("type", "new")                 // Int
                        Constant.isPasswordCreation = true
                        startActivity(intent)
                    }
                }

            }
        }

        authViewModel!!.isForgetPassword?.observe(this) { response ->
            Constant.hideLoading(this@OTP)
            if (response != null) {
                Constant.forgotData = response.data
                isOtpTitleLoad()
            }
        }
        startOtpTimer()
    }

    private fun setupOtp() {
        val boxes = listOf(binding.txtOtp1, binding.txtOtp2, binding.txtOtp3, binding.txtOtp4, binding.txtOtp5, binding.txtOtp6)

        boxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    if (text.length == 1) {
                        // Single digit entered: move to next field
                        if (index < boxes.size - 1) {
                            boxes[index + 1].requestFocus()
                        }
                        // Check if all filled
                        val allFilled = boxes.all { it.text.length == 1 }
                        if (allFilled) {
                            editText.clearFocus()
                            val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        }
                    } else if (text.isEmpty()) {
                        // Backspace on empty field: move to previous field
                        if (index > 0) {
                            boxes[index - 1].requestFocus()
                        }
                    } else if (text.length > 1) {
                        // Multi-character paste detected: distribute digits
                        val digitsOnly = text.filter { it.isDigit() }
                        if (digitsOnly.isNotEmpty()) {
                            s?.replace(0, s.length, digitsOnly[0].toString()) // Keep first digit in current field
                            var remaining = digitsOnly.substring(1).take(6 - index) // Limit to remaining fields
                            var j = index + 1
                            while (remaining.isNotEmpty() && j < boxes.size) {
                                val nextEditText = boxes[j]
                                if (nextEditText.text.isEmpty()) {
                                    nextEditText.setText(remaining[0].toString())
                                    remaining = remaining.substring(1)
                                }
                                j++
                            }
                            // Move focus to the last filled or first empty field
                            val focusIndex = minOf(index + digitsOnly.length, boxes.size - 1)
                            boxes[focusIndex].requestFocus()
                            // Check if all filled after paste
                            val allFilled = boxes.all { it.text.length == 1 }
                            if (allFilled) {
                                boxes[focusIndex].clearFocus()
                                val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                            }
                        } else {
                            s?.clear() // Clear invalid paste
                        }
                    }
                }
            })

            // Backspace handling for empty field
            editText.setOnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && (v as EditText).text.isEmpty()) {
                    if (index > 0) {
                        boxes[index - 1].apply {
                            requestFocus()
                            setSelection(text.length)
                        }
                        return@setOnKeyListener true
                    }
                }
                false
            }

            // Ensure cursor is always at the end when the field gains focus
            editText.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    (v as EditText).setSelection(v.text.length)
                }
            }
        }

        binding.txtOtp1.requestFocus()
    }


    private fun getOtp(): String = listOf(binding.txtOtp1, binding.txtOtp2, binding.txtOtp3, binding.txtOtp4, binding.txtOtp5, binding.txtOtp6).joinToString("") { it.text.toString() }


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
        Constant.showLoading(this@OTP)
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
        Constant.showLoading(this@OTP)
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

            R.id.rytHeader -> {
                onBackPressed()
            }

            R.id.btnNext -> {
                val isOpt = getOtp()
                if (isOpt.length == 6) {
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