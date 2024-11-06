package com.vs.schoolmessenger.Auth.OTP

import android.content.Intent
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.OtpScreenBinding

class OTP : BaseActivity<OtpScreenBinding>(), View.OnClickListener {

    private val otpTimeout = 30000L
    private val otpInterval = 1000L

    override fun getViewBinding(): OtpScreenBinding {
        return OtpScreenBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        isToolBarWhiteTheme()
        binding.lblResend.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)


        when (Constant.isOtpRedirection) {
            1 -> {

            }
            2 -> {

            }
            3 -> {

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


    private fun setOtpInputListener(
        currentEditText: EditText,
        nextEditText: EditText?,
        previousEditText: EditText?
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
                if (binding.txtOtp1.text.toString() != "" && binding.txtOtp2.text.toString() != "" && binding.txtOtp3.text.toString() != "" &&
                    binding.txtOtp4.text.toString() != "" && binding.txtOtp5.text.toString() != "" && binding.txtOtp6.text.toString() != ""
                ) {
                    val intent = Intent(this@OTP, PasswordGeneration::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.EnterTheOtp, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}