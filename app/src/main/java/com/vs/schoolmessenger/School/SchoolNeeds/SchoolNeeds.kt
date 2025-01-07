package com.vs.schoolmessenger.School.SchoolNeeds

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding
import com.vs.schoolmessenger.databinding.SchoolNeedsBinding

class SchoolNeeds : BaseActivity<SchoolNeedsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): SchoolNeedsBinding {
        return SchoolNeedsBinding.inflate(layoutInflater)
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