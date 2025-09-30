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
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Terms and Conditions"


        Constant.loadWebView(
            this,
            binding.webView,
            Constant.terms_condition
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