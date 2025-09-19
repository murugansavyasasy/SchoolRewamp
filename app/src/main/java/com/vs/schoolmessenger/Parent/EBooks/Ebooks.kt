package com.vs.schoolmessenger.Parent.EBooks
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.EbooksBinding
import com.vs.schoolmessenger.databinding.SchoolNeedsBinding

class Ebooks : BaseActivity<EbooksBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): EbooksBinding {
        return EbooksBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        binding.toolbarLayout.imgBack.setOnClickListener{onBackPressed()}
        binding.toolbarLayout.lblParentToolBar.text=Constant.isSchoolMenuName

        Constant.loadWebView(
            this,
            binding.webView,
            Constant.isGlobalVariableData!!.ebooks_url

        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}