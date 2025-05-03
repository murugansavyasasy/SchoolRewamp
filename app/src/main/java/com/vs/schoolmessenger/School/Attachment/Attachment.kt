package com.vs.schoolmessenger.School.Attachment

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.TimeSelectedListener
import com.vs.schoolmessenger.databinding.AssignmentBinding
import com.vs.schoolmessenger.databinding.AttachmentBinding

class Attachment: BaseActivity<AttachmentBinding>()
{
    override fun getViewBinding(): AttachmentBinding {
        return AttachmentBinding.inflate(layoutInflater)
    }
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.attachmentValue.setText("Still in Progress,Need to Develop");
}}