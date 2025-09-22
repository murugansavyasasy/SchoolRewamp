package com.vs.schoolmessenger.School.LeaveRequests

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.RequestLeave.MonthWiseLeaveData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LeaveRequests.Listener.SchoolLRClickListener
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveApproveRequest
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LeaveRequestsBinding

class LeaveRequests : BaseActivity<LeaveRequestsBinding>(),
    View.OnClickListener, SchoolLRClickListener {

    override fun getViewBinding(): LeaveRequestsBinding {
        return LeaveRequestsBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: MonthwiseLeaveAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isApproveRejectId = ""
    var isApprovedOrRejectedSuccessful = false
    private var pendingApprovalCallback: ((Boolean) -> Unit)? = null

    private var leaveRequestMonthWiseList: List<MonthWiseLeaveData>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetLeaveRequestList()
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.toolbarLayout.rytSearch.visibility == View.VISIBLE) {
                binding.toolbarLayout.rytSearch.visibility = View.GONE
            } else {
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.toolbarLayout.txtSearch.text.clear()
            }
        }

        binding.rcyleaverequest.visibility = View.VISIBLE
        binding.tabLayoutStatus.visibility = View.VISIBLE


        binding.tabLayoutStatus.removeAllTabs()

        val tabTitles = listOf("All", "Approved", "Rejected", "Waiting")

        val tabStatusMap = mapOf(
            "All" to Constant.All_,
            "Approved" to Constant.approved,
            "Rejected" to Constant.rejected,
            "Waiting" to Constant.waiting_for_approval
        )

        tabTitles.forEach { title ->
            binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText(title))
        }

        binding.tabLayoutStatus.clearOnTabSelectedListeners()

        binding.tabLayoutStatus.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val selectedTitle = tab.text.toString()
                val filterStatus = tabStatusMap[selectedTitle] ?: Constant.All_
                mAdapter.filterByStatus(filterStatus)

                if (mAdapter.itemCount == 0) {
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.VISIBLE
                    binding.rcyleaverequest.visibility = View.GONE
                } else {
                    binding.txtNoData.visibility = View.GONE
                    binding.nomessage.visibility = View.GONE
                    binding.rcyleaverequest.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.toolbarLayout.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        appViewModel?.getleaverequest?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyleaverequest.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                leaveRequestMonthWiseList = response.data
                isloadleaverequestData(leaveRequestMonthWiseList)

            } else {

                binding.tabLayoutStatus.visibility = View.GONE
                binding.rcyleaverequest.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?:getString(R.string.no_data_found)
            }
        }


        appViewModel!!.isleaverequestapprove?.observe(this) { response ->
            Constant.hideLoading(this@LeaveRequests)

            if (response != null && response.status) {
                isApprovedOrRejectedSuccessful = true
                pendingApprovalCallback?.invoke(true)
                pendingApprovalCallback = null
                Constant.showDataValidationNoDashboardRedirect(
                    getString(R.string.success),
                    response.message,
                    this
                )
            } else {
                isApprovedOrRejectedSuccessful = false
                pendingApprovalCallback?.invoke(false)
                pendingApprovalCallback = null
                Constant.showDataValidation(
                    getString(R.string.fail),
                    response?.message ?: getString(R.string.Something_went_wrong_Please_try_again),
                    this
                )
            }
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_leave_request_found)
            binding.rcyleaverequest.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyleaverequest.visibility = View.VISIBLE
        }
    }

    override fun onApproveClicked(
        data: LeaveData,
        position: Int,
        isButtonClick: Boolean,
        resultCallback: (Boolean) -> Unit
    ) {
        val request = LeaveApproveRequest(id = data.id, is_approve = true)
        isApproveRejectId = data.id
        var isMessage = ""
        if (isButtonClick) {
            isMessage = getString(R.string.Are_you_sure_you_want_to_approve_this_request)
        } else {
            isMessage = getString(R.string.Are_you_sure_you_want_to_reject_this_request)
        }
        Constant.showSendConfirmationDialog(
            this,
            getString(R.string.confirmation),
            getString(R.string.permission_ok),
            getString(R.string.Cancel),
            "",
            isMessage
        ) { confirmed ->
            if (confirmed) {
                Constant.showLoading(this)
                pendingApprovalCallback = resultCallback // store it for later
                appViewModel?.isleaverequestapprove(isAccessToken!!, request, this)
            } else {
                resultCallback(false) // user cancelled
            }
        }
    }

    override fun onUpdateStatus(leaveData: LeaveData) {
        mAdapter.notifyDataSetChanged()
    }


    private fun isloadleaverequestData(newData: List<MonthWiseLeaveData>?) {
        mAdapter = MonthwiseLeaveAdapter(
            newData, this, this, Constant.isShimmerViewDisable
        )
        binding.rcyleaverequest.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        mAdapter = MonthwiseLeaveAdapter(
            null, this, this, Constant.isShimmerViewDisable
        )
        binding.rcyleaverequest.layoutManager = LinearLayoutManager(this)
        binding.rcyleaverequest.isNestedScrollingEnabled = false
        binding.rcyleaverequest.adapter = mAdapter
        appViewModel!!.getleaverequest(
            isAccessToken!!, Constant.STAFF__, this
        )
    }
}
