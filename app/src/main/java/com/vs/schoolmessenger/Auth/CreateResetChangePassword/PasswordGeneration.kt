package com.vs.schoolmessenger.Auth.CreateResetChangePassword

import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Combination.RoleSelection
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PasswordGenerationBinding

class PasswordGeneration : BaseActivity<PasswordGenerationBinding>(), View.OnClickListener {

    private var isPasswordVisible = false

    override fun getViewBinding(): PasswordGenerationBinding {
        return PasswordGenerationBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()


            when (Constant.isPassWordCreateType) {
                1 -> {
                    binding.lblCreatePassword.text = getString(R.string.lblCreateNewPassword)
                }

                2 -> {
                    binding.lblCreatePassword.text = getString(R.string.ResetThePassword)
                }

                3 -> {
                    binding.lblCreatePassword.text = getString(R.string.change_password)
                }
            }

        authViewModel!!.isCreateNewPassword?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    Toast.makeText(this, R.string.SuccessfullyPasswordCreation, Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@PasswordGeneration, RoleSelection::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.imgHide.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        isToolBarWhiteTheme()
//        isPasswordChange()
//        isPasswordReset()


        authViewModel!!.isPasswordReset?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {

                }
            }
        }
    }

    private fun isPasswordChange() {
        val isMobileNumber = SharedPreference.getMobileNumber(this)
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.Req_mobile_number, isMobileNumber)
        jsonObject.addProperty(RequestKeys.Req_old_password, "12345")
        jsonObject.addProperty(
            RequestKeys.Req_new_password,
            binding.txtConfirmPassword.text.toString()
        )
        authViewModel!!.isPasswordChange(jsonObject, this)
    }

    private fun isPasswordReset() {
        val isMobileNumber = SharedPreference.getMobileNumber(this)
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.Req_mobile_number, isMobileNumber)
        jsonObject.addProperty(
            RequestKeys.Req_new_password,
            binding.txtConfirmPassword.text.toString()
        )
        authViewModel!!.isPasswordReset(jsonObject, this)
    }

    private fun isCreatePassword() {
        val jsonObject = JsonObject()
        val isMobileNumber = SharedPreference.getMobileNumber(this)
        jsonObject.addProperty(RequestKeys.Req_mobile_number, isMobileNumber)
        jsonObject.addProperty(
            RequestKeys.Req_new_password,
            binding.txtConfirmPassword.text.toString()
        )
        authViewModel!!.isCreatePassword(jsonObject, this)
    }


    private fun isPasswordViewAndHide() {
        if (isPasswordVisible) {
            binding.txtConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.imgHide.setImageResource(R.drawable.password_hide)
        } else {
            binding.txtConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.imgHide.setImageResource(R.drawable.password_view)
        }
        binding.txtConfirmPassword.setSelection(binding.txtConfirmPassword.text?.length ?: 0)
        isPasswordVisible = !isPasswordVisible
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgHide -> {
                isPasswordViewAndHide()
            }

            R.id.btnCreate -> {
                if (isPassWordNotEmpty()) {
                    isCreatePassword()
                }
            }
        }
    }

    private fun isPassWordNotEmpty(): Boolean {
        var isPassWord: Boolean
        if (binding.txtCreatePassword.text.toString() != "") {
            isPassWord = false
            if (binding.txtConfirmPassword.text.toString() != ""
            ) {
                isPassWord = false
                if (binding.txtCreatePassword.text.toString() == binding.txtConfirmPassword.text.toString()) {
                    isPassWord = true;
                } else {
                    isPassWord = false
                    Toast.makeText(this, R.string.isPasswordMisMatching, Toast.LENGTH_SHORT).show()
                }
            } else {
                isPassWord = false;
                Toast.makeText(this, R.string.EnterTheConformPassword, Toast.LENGTH_SHORT).show()
            }
        } else {
            isPassWord = false;
            Toast.makeText(this, R.string.EnterTheNewPassword, Toast.LENGTH_SHORT).show()
        }
        return isPassWord
    }
}