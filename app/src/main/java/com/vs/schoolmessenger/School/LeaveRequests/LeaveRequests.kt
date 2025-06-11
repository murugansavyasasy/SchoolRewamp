package com.vs.schoolmessenger.School.LeaveRequests

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class LeaveRequests : BaseActivity<LeaveRequestsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): LeaveRequestsBinding {
        return LeaveRequestsBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: LeaveRequestAdapter
    private lateinit var isLeaveRequestData: List<LeaveRequestData>

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Leave Request"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
 }