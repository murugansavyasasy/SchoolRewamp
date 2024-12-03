package com.vs.schoolmessenger.School.Communication

import android.graphics.Paint
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.lblHistoryList.paintFlags = binding.lblHistoryList.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.lblBackToVoiceMessage.paintFlags = binding.lblBackToVoiceMessage.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }
}