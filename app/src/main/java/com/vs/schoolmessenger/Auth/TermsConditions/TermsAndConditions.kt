package com.vs.schoolmessenger.Auth.TermsConditions

import android.net.Uri
import android.util.Log
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.BuildConfig
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
        binding.imgBack.setOnClickListener(this)

        var URL = ""
        binding.lblParentToolBar.text = when (screen_name) {
            "isTerms" -> {
                if (BuildConfig.BASE_APP) {
                    URL = BuildConfig.TERMS_URL
                } else {
                    val url = BuildConfig.TERMS_URL
                    URL = Uri.parse(url)
                        .buildUpon()
                        .appendQueryParameter("id", BuildConfig.SCHOOL_ID)
                        .build()
                        .toString()
                }
                "Terms and Conditions"
            }

            "isPrivacy" -> {
                if (BuildConfig.BASE_APP) {
                    URL = Constant.isGlobalVariableData?.privacy_policy ?: ""
                } else {
                    val url  = Constant.isGlobalVariableData?.wl_privacy?: ""
                    URL = Uri.parse(url)
                        .buildUpon()
                        .appendQueryParameter("id", BuildConfig.SCHOOL_ID)
                        .build()
                        .toString()
                }
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
        Log.d("URL",URL)
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