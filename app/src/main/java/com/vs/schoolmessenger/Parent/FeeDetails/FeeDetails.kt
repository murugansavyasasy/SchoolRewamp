package com.vs.schoolmessenger.Parent.FeeDetails

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.FeeDetailsBinding

class FeeDetails : BaseActivity<FeeDetailsBinding>() {

    override fun getViewBinding(): FeeDetailsBinding {
        return FeeDetailsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}