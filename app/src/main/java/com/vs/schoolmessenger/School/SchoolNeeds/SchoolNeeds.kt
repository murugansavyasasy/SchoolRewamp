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
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener{onBackPressed()}
        binding.toolbarLayout.lblParentToolBar.text=Constant.isSchoolMenuName

        Constant.loadWebView(
            this,
            binding.webView,
            Constant.isGlobalVariableData!!.market_place_url

        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}