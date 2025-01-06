package com.vs.schoolmessenger.Parent.ExamTest

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.ExamTestParentBinding

class ExamTest : BaseActivity<ExamTestParentBinding>() {

    override fun getViewBinding(): ExamTestParentBinding {
        return ExamTestParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}