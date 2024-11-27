package com.vs.schoolmessenger.Dashboard.Settings.ContactUs

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ContactSupportBinding

class ContactUs : BaseActivity<ContactSupportBinding>(), View.OnClickListener {

    override fun getViewBinding(): ContactSupportBinding {
        return ContactSupportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}