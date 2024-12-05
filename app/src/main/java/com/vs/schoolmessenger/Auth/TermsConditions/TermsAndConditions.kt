package com.vs.schoolmessenger.Auth.TermsConditions
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.TermsAndConditionsBinding

class TermsAndConditions  : BaseActivity<TermsAndConditionsBinding>(), View.OnClickListener {

    override fun getViewBinding(): TermsAndConditionsBinding {
        return TermsAndConditionsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        setupToolbar()
        // Enable JavaScript
        binding.imgBack.setOnClickListener(this)

        Constant.loadWebView(
            this,
            binding.webView,
            "https://schoolchimes.com/vs_web/terms_conditions/"
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