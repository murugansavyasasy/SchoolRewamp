package com.vs.schoolmessenger.School.Assignment

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionDetail
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentStudentDetailreportBinding

class AssignmentStudentListDetail : BaseActivity<AssignmentStudentDetailreportBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): AssignmentStudentDetailreportBinding {
        return AssignmentStudentDetailreportBinding.inflate(layoutInflater)
    }

    private var isStaffDetails: StaffDetails? = null
    private lateinit var submissionAdapter: AssignmentStudentListDetailAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()

        isStaffDetails = SharedPreference.getStaffDetails(this)


        val submissionList = intent.getParcelableArrayListExtra<SubmissionDetail>("submission_list")


        submissionAdapter =
            AssignmentStudentListDetailAdapter(submissionList ?: emptyList(), this, false)
        binding.rcystudentlistdetail.apply {
            layoutManager = LinearLayoutManager(this@AssignmentStudentListDetail)
            adapter = submissionAdapter
        }

        if (submissionList.isNullOrEmpty()) {

        }
    }

    override fun onClick(v: View?) {}
}

