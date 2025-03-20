package com.vs.schoolmessenger.School.PTM

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding
import com.vs.schoolmessenger.databinding.PtmStaffBinding

class PTM : BaseActivity<PtmStaffBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): PtmStaffBinding {
        return PtmStaffBinding.inflate(layoutInflater)
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