package com.vs.schoolmessenger.School.LeaveRequests

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class LeaveRequests : BaseActivity<LeaveRequestsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): LeaveRequestsBinding {
        return LeaveRequestsBinding.inflate(layoutInflater)
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