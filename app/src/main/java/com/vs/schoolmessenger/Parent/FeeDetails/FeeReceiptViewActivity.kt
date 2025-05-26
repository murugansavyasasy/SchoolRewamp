package com.vs.schoolmessenger.Parent.FeeDetails

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeeReceiptViewActivityBinding

class FeeReceiptViewActivity: BaseActivity<FeeReceiptViewActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeeReceiptViewActivityBinding {
        return FeeReceiptViewActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblParentToolBar.text = "Fee Details"
        binding.toolbarLayout.lblStudentSection.text = isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
