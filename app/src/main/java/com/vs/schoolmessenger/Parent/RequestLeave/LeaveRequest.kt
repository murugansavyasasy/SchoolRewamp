package com.vs.schoolmessenger.Parent.RequestLeave

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.LeaveRequestBinding

class LeaveRequest : BaseActivity<LeaveRequestBinding>(), View.OnClickListener,
    LeaveRequestClickListener {

    override fun getViewBinding(): LeaveRequestBinding {
        return LeaveRequestBinding.inflate(layoutInflater)
    }

    private lateinit var isLeaveRequestHistoryData: List<LeaveRequestHistoryData>
    lateinit var mAdapter: LeaveRequestAdapter

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Leave_Request)
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"

        binding.toolbarLayout.lblLeftSideBar.text = resources.getText(R.string.History)
        binding.toolbarLayout.lblRightSideBar.text = resources.getText(R.string.Create)
        binding.rlaCreateLeaveRequest.visibility = View.VISIBLE
        loadData()
        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
            isBackRoundChange(binding.toolbarLayout.lblRightSideBar)
            binding.rlaCreateLeaveRequest.visibility = View.VISIBLE
            binding.rlaHistory.visibility=View.GONE
        }

        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
            binding.rlaHistory.visibility=View.VISIBLE
            binding.rlaCreateLeaveRequest.visibility = View.GONE
            isBackRoundChange(binding.toolbarLayout.lblLeftSideBar)
            loadData()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }


    private fun loadData() {

        isLeaveRequestHistoryData = listOf(
            LeaveRequestHistoryData(
                "15 Nov 2024",
                "15 Nov 2024",
                "Pending",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
            ),
            LeaveRequestHistoryData(
                "15 Nov 2024",
                "15 Nov 2024",
                "Rejected",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
            ),

            LeaveRequestHistoryData(
                "15 Nov 2024",
                "15 Nov 2024",
                "Approval",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
            ),
            LeaveRequestHistoryData(
                "15 Nov 2024",
                "15 Nov 2024",
                "Pending",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
            ),
            LeaveRequestHistoryData(
                "15 Nov 2024",
                "15 Nov 2024",
                "Rejected",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
            ),
            LeaveRequestHistoryData(
                "15 Nov 2024",
                "15 Nov 2024",
                "Approval",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
            )
        )

        mAdapter = LeaveRequestAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyLeaveRequestHistory.layoutManager = LinearLayoutManager(this)
        binding.rcyLeaveRequestHistory.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                LeaveRequestAdapter(
                    isLeaveRequestHistoryData,
                    this,
                    this,
                    Constant.isShimmerViewDisable
                )
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyLeaveRequestHistory.adapter = mAdapter
        }

    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.toolbarLayout.lblRightSideBar) {
            binding.toolbarLayout.lblLeftSideBar.background = null
            binding.toolbarLayout.lblLeftSideBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dark_blue
                )
            )
        }

        if (isClickingId == binding.toolbarLayout.lblLeftSideBar) {
            binding.toolbarLayout.lblRightSideBar.background = null
            binding.toolbarLayout.lblRightSideBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dark_blue
                )
            )
        }

        isClickingId.background =
            ContextCompat.getDrawable(this, R.drawable.bg_gradient_parent_clickbar)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }

    override fun onItemImageClick(data: LeaveRequestHistoryData) {

    }

}