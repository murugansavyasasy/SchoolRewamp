package com.vs.schoolmessenger.Parent.Noticeboard

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.NoticeBoardBinding

class NoticeBoard : BaseActivity<NoticeBoardBinding>() {

    override fun getViewBinding(): NoticeBoardBinding {
        return NoticeBoardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}