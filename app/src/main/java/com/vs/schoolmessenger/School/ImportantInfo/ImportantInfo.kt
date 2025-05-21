package com.vs.schoolmessenger.School.ImportantInfo

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ImportantInfoBinding

class ImportantInfo : BaseActivity<ImportantInfoBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ImportantInfoBinding {
        return ImportantInfoBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setUpGradientSchool()

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