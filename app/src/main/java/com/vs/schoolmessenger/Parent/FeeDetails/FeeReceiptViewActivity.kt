package com.vs.schoolmessenger.Parent.FeeDetails

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
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
        binding.toolbarLayout.lblParentToolBar.text = "Fee Receipt"
        binding.toolbarLayout.lblStudentSection.text = isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        val pdfUrl = "https://schoolchimes-fee-receipts.s3.ap-south-1.amazonaws.com/undefined/fee_receipt/PDF_1748065242703.pdf"
        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$pdfUrl"
        Constant.loadWebView(
            this,
            binding.feeReceiptWebview,
            googleDocsUrl
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
