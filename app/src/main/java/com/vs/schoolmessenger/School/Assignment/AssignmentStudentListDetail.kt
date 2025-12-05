package com.vs.schoolmessenger.School.Assignment

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionDetail
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentStudentDetailreportBinding

class AssignmentStudentListDetail : BaseActivity<AssignmentStudentDetailreportBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AssignmentStudentDetailreportBinding {
        return AssignmentStudentDetailreportBinding.inflate(layoutInflater)
    }

    private var isStaffDetails: StaffDetails? = null
    private lateinit var submissionAdapter: AssignmentStudentListDetailAdapter

    private var title: String = ""
    private var assignmentSubject: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        title = intent.getStringExtra("title") ?: ""
        assignmentSubject = intent.getStringExtra(Constant.assignmentsubject) ?: ""

        Log.d("titleAssignmentStudent", title.toString())
        Log.d("descriptionAssignmentStudent", assignmentSubject.toString())

        val submissionList =
            intent.getParcelableArrayListExtra<SubmissionDetail>(Constant.submission_list)


        submissionAdapter =
            AssignmentStudentListDetailAdapter(
                submissionList ?: emptyList(), this, false, title,
                assignmentSubject
            )
        binding.rcystudentlistdetail.apply {
            layoutManager = LinearLayoutManager(this@AssignmentStudentListDetail)
            adapter = submissionAdapter
        }

        if (submissionList.isNullOrEmpty()) {

        }
    }

    override fun onClick(v: View?) {}
}

