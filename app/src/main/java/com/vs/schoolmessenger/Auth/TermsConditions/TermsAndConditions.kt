package com.vs.schoolmessenger.Auth.TermsConditions

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.TermsAndConditionsBinding

class TermsAndConditions : BaseActivity<TermsAndConditionsBinding>(), View.OnClickListener {

    override fun getViewBinding(): TermsAndConditionsBinding {
        return TermsAndConditionsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        // Enable JavaScript
        val screen_name = intent.getStringExtra("screen_name") ?: ""
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        var URL = ""
        binding.toolbarLayout.lblParentToolBar.text = when (screen_name) {
            "isTerms" -> {
                URL = Constant.terms_condition
                "Terms and Conditions"
            }

            "isPrivacy" -> {
                URL = Constant.isGlobalVariableData?.privacy_policy ?: ""
                "Privacy Policy"
            }

            "isAboutTheApp" -> {
                URL = Constant.isGlobalVariableData?.about_the_app ?: ""
                "About the App"
            }

            "HowToUse" -> {
                URL = Constant.isGlobalVariableData?.how_to_use ?: ""
                "How to Use?"
            }

            else -> ""
        }
        Constant.loadWebView(
            this,
            binding.webView,
            URL
        )

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                finish()
            }
        }
    }
}