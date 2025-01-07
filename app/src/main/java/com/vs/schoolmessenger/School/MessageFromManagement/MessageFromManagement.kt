package com.vs.schoolmessenger.School.MessageFromManagement

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding
import com.vs.schoolmessenger.databinding.MessageFromManagementBinding

class MessageFromManagement : BaseActivity<MessageFromManagementBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): MessageFromManagementBinding {
        return MessageFromManagementBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}