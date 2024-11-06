package com.vs.schoolmessenger.Auth.CreateResetChangePassword

import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Combination.RoleSelection
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.PasswordGenerationBinding

class PasswordGeneration : BaseActivity<PasswordGenerationBinding>(), View.OnClickListener {

    private var isPasswordVisible = false

    override fun getViewBinding(): PasswordGenerationBinding {
        return PasswordGenerationBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID

        when (Constant.isOtpRedirection) {
            1 -> {

            }

            2 -> {
                binding.lblCreatePassword.text = getString(R.string.ResetThePassword)
            }

            3 -> {

            }
        }

        binding.imgHide.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        isToolBarWhiteTheme()


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
                    Toast.makeText(this, R.string.SuccessfullyPasswordCreation, Toast.LENGTH_SHORT)
                        .show()

                    val intent = Intent(this@PasswordGeneration, RoleSelection::class.java)
                    startActivity(intent)

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