package com.vs.schoolmessenger.School.SchoolNeeds

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SchoolNeedsBinding

class SchoolNeeds : BaseActivity<SchoolNeedsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): SchoolNeedsBinding {
        return SchoolNeedsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.imgBack.setOnClickListener(this)


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