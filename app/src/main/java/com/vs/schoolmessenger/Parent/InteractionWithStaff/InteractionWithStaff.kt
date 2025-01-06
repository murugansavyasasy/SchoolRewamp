package com.vs.schoolmessenger.Parent.InteractionWithStaff

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.IntectionWithStaffBinding

class InteractionWithStaff : BaseActivity<IntectionWithStaffBinding>() {

    override fun getViewBinding(): IntectionWithStaffBinding {
        return IntectionWithStaffBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}