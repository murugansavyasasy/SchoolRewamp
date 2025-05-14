package com.vs.schoolmessenger.Auth.CreateResetChangePassword

import android.content.Intent
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.OTP.OTP
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.PasswordGenerationBinding

class PasswordGeneration : BaseActivity<PasswordGenerationBinding>(), View.OnClickListener {

    private var isPasswordVisible = false
    private var isCreatePasswordVisible = false

    override fun getViewBinding(): PasswordGenerationBinding {
        return PasswordGenerationBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        isToolBarWhiteTheme()
        binding.imgHide.setOnClickListener(this)
        binding.imgHide1.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        if (Constant.isPasswordCreation!!) {
            binding.lblCreatePassword.text = getString(R.string.lblCreateNewPassword)
        } else {
            binding.lblCreatePassword.text = getString(R.string.ResetThePassword)
        }

        authViewModel!!.isCreateNewPassword?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT)
                        .show()

                    SharedPreference.putMobileNumberPassWord(
                        this@PasswordGeneration,
                        Constant.isMobileNumber,
                        binding.txtConfirmPassword.text.toString()
                    )

                    if (Constant.user_data!![0].user_details.is_staff && Constant.user_data!![0].user_details.is_parent) {
                        val intent = Intent(this@PasswordGeneration, PrioritySelection::class.java)
                        startActivity(intent)

                    } else if (Constant.user_data!![0].user_details.is_staff) {

                        if (Constant.user_data!![0].user_details.staff_role.equals(Constant.isStaffRole)) {

                            if (Constant.user_data!![0].user_details.staff_details.size > 1) {
                                val intent =
                                    Intent(this@PasswordGeneration, PrioritySelection::class.java)
                                startActivity(intent)
                            } else {
                                val intent = Intent(
                                    this@PasswordGeneration,
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
                                this@PasswordGeneration,
                                SchoolDashboard::class.java
                            )
                            startActivity(intent)
                        }
                    } else if (Constant.user_data!![0].user_details.is_parent) {
                        if (Constant.user_data!![0].user_details.child_details.size > 1) {
                            val intent =
                                Intent(this@PasswordGeneration, PrioritySelection::class.java)
                            startActivity(intent)
                        } else {
                            Constant.isParentChoose = true
                            val intent = Intent(
                                this@PasswordGeneration,
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

        authViewModel!!.isPasswordReset?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@PasswordGeneration, Login::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun isPasswordReset() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(
            APIKeyNames.Req_new_password,
            binding.txtConfirmPassword.text.toString()
        )
        authViewModel!!.isPasswordReset(jsonObject, this)
    }

    private fun isCreatePassword() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.Req_mobile_number, Constant.isMobileNumber)
        jsonObject.addProperty(
            APIKeyNames.Req_new_password,
            binding.txtConfirmPassword.text.toString()
        )
        authViewModel!!.isCreatePassword(jsonObject, this)
    }

    private fun isPasswordViewAndHide1() {
        if (isCreatePasswordVisible) {
            binding.txtCreatePassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.imgHide1.setImageResource(R.drawable.password_hide)
        } else {
            binding.txtCreatePassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.imgHide1.setImageResource(R.drawable.password_view)
        }
        binding.txtCreatePassword.setSelection(binding.txtCreatePassword.text?.length ?: 0)
        isCreatePasswordVisible = !isCreatePasswordVisible
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

            R.id.imgHide1 -> {
                isPasswordViewAndHide1()
            }

            R.id.btnCreate -> {
                if (Constant.isPasswordCreation!!) {
                    if (isPassWordNotEmpty()) {
                        isCreatePassword()
                    }
                } else {
                    if (isPassWordNotEmpty()) {
                        isPasswordReset()
                    }
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