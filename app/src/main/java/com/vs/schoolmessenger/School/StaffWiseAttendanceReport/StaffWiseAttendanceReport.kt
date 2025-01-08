package com.vs.schoolmessenger.School.StaffWiseAttendanceReport

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.StaffAttendanceReportBinding

class StaffWiseAttendanceReport : BaseActivity<StaffAttendanceReportBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): StaffAttendanceReportBinding {
        return StaffAttendanceReportBinding.inflate(layoutInflater)
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