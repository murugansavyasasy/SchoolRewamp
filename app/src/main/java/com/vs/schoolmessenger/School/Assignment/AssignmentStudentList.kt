package com.vs.schoolmessenger.School.Assignment

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Assignment.Model.StudentSubmission

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentStudentListReportBinding

class AssignmentStudentList : BaseActivity<AssignmentStudentListReportBinding>(), View.OnClickListener,
    AssignmentStudentListClickListener {

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        assignmentId = intent.getStringExtra("assignment_id")
        type = intent.getStringExtra("type")
        submittedCount = intent.getIntExtra("submitted_count", 0)
        totalCount = intent.getIntExtra("Total_Count", 0)

        Log.d("AssignmentId", "Received ID: $assignmentId")
        Log.d("AssignmentType", "Received Type: $type")

        Log.d("submittedcount", "Received ID: $submittedCount")
        Log.d("totalcount", "Received Type: $totalCount")



        binding.submittedvalue.text = submittedCount.toString()
        binding.pendinglabelvalue.text = totalCount.toString()



        appViewModel?.getassignmentlist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcystudentlist.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadassignmentdata(response.data)
            } else {
                binding.rcystudentlist.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }

        isGetAssignmentStudentList()
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
