package com.vs.schoolmessenger.Dashboard.Settings.ContactUs

import android.util.Log
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
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        Log.d("Contact_us_email", Constant.isGlobalVariableData!!.support_email)

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lblContact)
        binding.lblContactNo.text = Constant.isGlobalVariableData!!.support_contact
        val list = Constant.isGlobalVariableData!!.support_email.split("/")

        val email1 = list.getOrNull(0)
        val email2 = list.getOrNull(1)
        binding.lblContacttMail.text = email1
        binding.lblContacttMail2.text = email2

        binding.rytPhone.setOnClickListener(this)
        binding.lblContacttMail.setOnClickListener(this)
        binding.lblContacttMail2.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rytPhone -> {
                Constant.redirectToDialPad(this, binding.lblContactNo.text.toString())
            }

            R.id.lblContacttMail -> {
                Constant.redirectToMail(this, binding.lblContacttMail.text.toString(), "", "")
            }

            R.id.lblContacttMail2 -> {
                Constant.redirectToMail(this, binding.lblContacttMail2.text.toString(), "", "")
            }
        }
    }
}