package com.vs.schoolmessenger.School.Assignment

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Assignment.Model.StudentSubmission

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentStudentListReportBinding

class AssignmentStudentList : BaseActivity<AssignmentStudentListReportBinding>(),
    View.OnClickListener, AssignmentStudentListClickListener {

    override fun getViewBinding(): AssignmentStudentListReportBinding {
        return AssignmentStudentListReportBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    lateinit var assignmentstudentlistadapter: AssignmentStudentListAdapter

    private var assignmentId: String? = null
    private var submittedCount: Int = 0
    private var totalCount: Int = 0
    private var type: String? = null

    private var allStudentsList: List<StudentSubmission> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        assignmentId = intent.getStringExtra(Constant.assignment_id)
        type = intent.getStringExtra(Constant.type)
        submittedCount = intent.getIntExtra(Constant.submitted_count, 0)
        totalCount = intent.getIntExtra(Constant.Total_Count, 0)


        binding.tabLayout.apply {
            addTab(newTab().setText("All Students"))
            addTab(newTab().setText("Submitted"))
            addTab(newTab().setText("Pending"))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showAllStudents()
                    1 -> showSubmitted()
                    2 -> showPending()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        appViewModel?.getassignmentlist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                allStudentsList = response.data // store full list
                binding.rcystudentlist.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                showAllStudents()
            } else {
                binding.rcystudentlist.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
            }
        }

        isGetAssignmentStudentList()
    }

    private fun showAllStudents() {
        isloadassignmentdata(allStudentsList)
    }

    private fun showSubmitted() {
        val filteredList =
            allStudentsList.filter { it.submit_status.equals(Constant.SUBMITTED, ignoreCase = true) }
        isloadassignmentdata(filteredList)
    }

    private fun showPending() {
        val filteredList =
            allStudentsList.filter { it.submit_status.equals(Constant.NOTSUBMITTED, ignoreCase = true) }
        isloadassignmentdata(filteredList)
    }

    private fun isloadassignmentdata(newData: List<StudentSubmission>?) {
        assignmentstudentlistadapter =
            AssignmentStudentListAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcystudentlist.adapter = assignmentstudentlistadapter
    }

    private fun isGetAssignmentStudentList() {
        assignmentstudentlistadapter = AssignmentStudentListAdapter(
            null, this, this, Constant.isShimmerViewShow
        )
        binding.rcystudentlist.layoutManager = LinearLayoutManager(this)
        binding.rcystudentlist.isNestedScrollingEnabled = false
        binding.rcystudentlist.adapter = assignmentstudentlistadapter

        appViewModel!!.getassignmentlist(isAccessToken!!, assignmentId!!, type!!)
    }

    override fun onClick(v: View?) {
        // Handle clicks if needed
    }


}

