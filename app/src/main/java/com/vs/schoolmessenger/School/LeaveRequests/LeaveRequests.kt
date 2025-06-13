package com.vs.schoolmessenger.School.LeaveRequests

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
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

    lateinit var mAdapter: LeaveRequestAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isGetLeaveRequestList()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Leave Request"
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

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
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadleaverequestData(response.data)
            } else {
                binding.rcyleaverequest.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }


        appViewModel!!.isleaverequestapprove?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@LeaveRequests)
                    Log.d("isSendleaverequestmarking", response.message)
                    Constant.showDataValidation(resources.getString(R.string.success), response.message, this)
                } else {
                    Constant.showDataValidation(resources.getString(R.string.fail), response.message, this)
                }
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
            binding.txtNoData.text = "No Leave Request found"
            binding.rcyleaverequest.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyleaverequest.visibility = View.VISIBLE
        }
    }



    override fun onApproveClicked(data: LeaveData, position: Int) {
        val request = LeaveApproveRequest(
            id = data.id,
            is_approve = "true"
        )
        appViewModel?.isleaverequestapprove(isAccessToken!!, request, this)
    }


    override fun onRejectClicked(data: LeaveData, position: Int) {
        val request = LeaveApproveRequest(
            id = data.id,
            is_approve = "false"
        )
        appViewModel?.isleaverequestapprove(isAccessToken!!, request, this)
    }



    private fun isloadleaverequestData(newData: List<LeaveData>?) {
        mAdapter =
            LeaveRequestAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyleaverequest.adapter = mAdapter
    }

    private fun isGetLeaveRequestList() {
        mAdapter = LeaveRequestAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyleaverequest.layoutManager = LinearLayoutManager(this)
        binding.rcyleaverequest.isNestedScrollingEnabled = false
        binding.rcyleaverequest.adapter = mAdapter
        appViewModel!!.getleaverequest(
            isAccessToken!!, "STAFF", this
        )
    }
 }