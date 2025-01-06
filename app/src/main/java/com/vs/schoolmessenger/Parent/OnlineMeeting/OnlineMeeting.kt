package com.vs.schoolmessenger.Parent.OnlineMeeting

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.OnlineMeetingBinding
import com.vs.schoolmessenger.databinding.OnlineMeetingParentBinding

class OnlineMeeting : BaseActivity<OnlineMeetingParentBinding>() {

    override fun getViewBinding(): OnlineMeetingParentBinding {
        return OnlineMeetingParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
    }
}