package com.vs.schoolmessenger.School.FeePendingReport

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.FeePendingReportBinding
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class FeePendingReport : BaseActivity<FeePendingReportBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): FeePendingReportBinding {
        return FeePendingReportBinding.inflate(layoutInflater)
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