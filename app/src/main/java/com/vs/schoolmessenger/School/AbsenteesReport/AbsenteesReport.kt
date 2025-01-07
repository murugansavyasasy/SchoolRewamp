package com.vs.schoolmessenger.School.AbsenteesReport

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class AbsenteesReport : BaseActivity<AbsenteesReportBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AbsenteesReportBinding {
        return AbsenteesReportBinding.inflate(layoutInflater)
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