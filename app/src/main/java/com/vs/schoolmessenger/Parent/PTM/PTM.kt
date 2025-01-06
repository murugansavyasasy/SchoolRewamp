package com.vs.schoolmessenger.Parent.PTM

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.PtmBinding

class PTM : BaseActivity<PtmBinding>() {

    override fun getViewBinding(): PtmBinding {
        return PtmBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}