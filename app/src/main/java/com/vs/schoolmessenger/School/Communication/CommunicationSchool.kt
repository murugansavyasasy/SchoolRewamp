package com.vs.schoolmessenger.School.Communication

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
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }
}