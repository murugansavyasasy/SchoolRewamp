package com.vs.schoolmessenger.School.MarkYourAttendance

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding
import com.vs.schoolmessenger.databinding.MarkYourAttendanceBinding

class MarkYourAttendance : BaseActivity<MarkYourAttendanceBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): MarkYourAttendanceBinding {
        return MarkYourAttendanceBinding.inflate(layoutInflater)
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