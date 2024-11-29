package com.vs.schoolmessenger.Dashboard.Settings.ContactUs
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ContactSupportBinding

class ContactUs : BaseActivity<ContactSupportBinding>(), View.OnClickListener {

    override fun getViewBinding(): ContactSupportBinding {
        return ContactSupportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rytPhone.setOnClickListener(this)
        binding.rytMail.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.rytPhone -> {
                Constant.redirectToDialPad(this,binding.lblContactNo.text.toString())
            }
            R.id.rytMail -> {
                Constant.redirectToMail(this,binding.lblContacttMail.text.toString())
            }
        }
    }
}