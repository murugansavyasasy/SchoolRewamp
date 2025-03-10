package com.vs.schoolmessenger.Parent.LSRW

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.LsrwBinding

class LSRW : BaseActivity<LsrwBinding>() {

    override fun getViewBinding(): LsrwBinding {
        return LsrwBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.lblParentToolBar.text = "LSRW"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
    }
}