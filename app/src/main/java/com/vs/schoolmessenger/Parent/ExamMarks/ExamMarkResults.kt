package com.vs.schoolmessenger.Parent.ExamMarks

import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Parent.ExamItem
import com.vs.schoolmessenger.Dashboard.Parent.ExamMarkAdapter
import com.vs.schoolmessenger.databinding.ExamMarkBinding
import com.vs.schoolmessenger.databinding.ExamMarkDetailBinding

class ExamMarkResults : BaseActivity<ExamMarkDetailBinding>() {


    override fun getViewBinding(): ExamMarkDetailBinding {
        return ExamMarkDetailBinding.inflate(layoutInflater)
    }
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

    }
}
