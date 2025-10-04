package com.vs.schoolmessenger.Parent.EBooks
import android.util.Log
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.EbooksBinding

class Ebooks : BaseActivity<EbooksBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): EbooksBinding {
        return EbooksBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener{onBackPressed()}

        if (Constant.isParentMenuName.isNullOrEmpty()){
            binding.toolbarLayout.lblParentToolBar.text=Constant.isSchoolMenuName
        }
        else{
            binding.toolbarLayout.lblParentToolBar.text=Constant.isParentMenuName
        }


        Constant.loadWebView(
            this,
            binding.webView,
            Constant.isGlobalVariableData!!.ebooks_url

        )
        Log.d("EbooksUrl",Constant.isGlobalVariableData!!.ebooks_url)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}