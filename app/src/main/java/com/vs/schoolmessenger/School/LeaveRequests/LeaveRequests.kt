package com.vs.schoolmessenger.School.LeaveRequests

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Utils.Constant
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

        loadData()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }

    fun loadData() {
        isLeaveRequestData = listOf(

            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),

            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ), LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            ),
            LeaveRequestData(
                "Apr 1 2025",
                "11:11 AM",
                "Sathish",
                "XII - B",
                "Mar 31 2025",
                "Apr 2 2025",
                "The reason for my leave is [brief reason for leave, such as personal reasons, family emergency, etc.]. During my absence, I will ensure that all pending tasks are completed or delegated to a colleague. In case of any urgent matters, I can be reached at [your contact details]. I kindly request you to approve my leave.",
                "1"
            )
        )


        mAdapter = LeaveRequestAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAttendanceReport.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                LeaveRequestAdapter(isLeaveRequestData, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyAttendanceReport.adapter = mAdapter
        }
    }
}