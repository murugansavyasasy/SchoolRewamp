package com.vs.schoolmessenger.Parent.ExamMarks

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.ExamMarkBinding

class ExamMark : BaseActivity<ExamMarkBinding>() {

    override fun getViewBinding(): ExamMarkBinding {
        return ExamMarkBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}