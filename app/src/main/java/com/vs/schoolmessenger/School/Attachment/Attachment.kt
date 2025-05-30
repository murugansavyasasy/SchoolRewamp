package com.vs.schoolmessenger.School.Attachment

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AttachmentBinding

class Attachment : BaseActivity<AttachmentBinding>() {
    override fun getViewBinding(): AttachmentBinding {
        return AttachmentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.attachmentValue.setText("Still in Progress,Need to Develop");
    }
}