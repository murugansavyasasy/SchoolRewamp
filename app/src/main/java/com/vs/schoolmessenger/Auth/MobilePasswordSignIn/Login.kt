package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.LoginBinding

class Login : BaseActivity<LoginBinding>(), View.OnClickListener {

    private var isPasswordVisible = false


    override fun getViewBinding(): LoginBinding {
        return LoginBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        binding.imgHide.setOnClickListener(this)
        binding.btnLoginContinue.setOnClickListener(this)
        binding.lblForgetPassword.setOnClickListener(this)
        isToolBarWhiteTheme()

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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgHide -> {
                isPasswordViewAndHide()
            }

            R.id.btnLoginContinue -> {
                if (isUserNamePasswordValidation()) {
                    val intent = Intent(this@Login, OTP::class.java)
                    startActivity(intent)
                }
            }

            R.id.lblForgetPassword -> {
                if (binding.txtMobileNumber.text.toString() != "" && binding.txtMobileNumber.text.toString().length == 10) {
                    val intent = Intent(this@Login, OTP::class.java)
                    Constant.isOtpRedirection = 2
                    startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.EnterTheMobileNumber, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isUserNamePasswordValidation(): Boolean {

        var isPassWord: Boolean
        if (binding.txtMobileNumber.text.toString() != "" && binding.txtMobileNumber.text.toString().length == 10) {
            isPassWord = false
            if (binding.txtPassword.text.toString() != ""
            ) {
                isPassWord = true
            } else {
                isPassWord = false
                Toast.makeText(this, R.string.EnterThePassWord, Toast.LENGTH_SHORT).show()
            }
        } else {
            isPassWord = false
            Toast.makeText(this, R.string.EnterTheMobileNumber, Toast.LENGTH_SHORT).show()
        }
        return isPassWord
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}