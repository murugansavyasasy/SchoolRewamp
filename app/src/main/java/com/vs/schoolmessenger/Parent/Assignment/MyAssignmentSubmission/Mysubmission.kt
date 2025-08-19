package com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Assignment.AssignmentClickListener
import com.vs.schoolmessenger.Parent.Assignment.AssignmentParentAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MysubmissionAssignmentBinding

class Mysubmission : BaseActivity<MysubmissionAssignmentBinding>(), AssignmentClickListener,
    View.OnClickListener {

    override fun getViewBinding(): MysubmissionAssignmentBinding {
        return MysubmissionAssignmentBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private var assignmentId: String? = null
    private var titleName: String? = null
    private var subjectName: String? = null


    lateinit var mAdapter: MySubmissionAdapter
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Assignment)
        binding.toolbarLayout.rytSearch.visibility = View.GONE


        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        assignmentId = intent.getStringExtra("assignment_id")
        titleName = intent.getStringExtra("title")
        subjectName = intent.getStringExtra("subject")


        binding.rcyAssignment.layoutManager = LinearLayoutManager(this)

        appViewModel?.getassignmentmysubmission?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyAssignment.visibility = View.VISIBLE
            } else {
                showEmptyState(response?.message ?: getString(R.string.no_data_found))
            }
        }
        fetchAssignmentReportData()
    }

    private fun fetchAssignmentReportData() {
        binding.rcyAssignment.visibility = View.VISIBLE
        mAdapter = MySubmissionAdapter(
            mutableListOf(),
            this,
            this,
            Constant.isShimmerViewShow,
            titleName,
            subjectName
        )

        binding.rcyAssignment.adapter = mAdapter

        if (!assignmentId.isNullOrEmpty() && !isAccessToken.isNullOrEmpty()) {
            appViewModel?.isGetAssignmentSubList(isAccessToken!!, assignmentId!!)
        } else {
            Log.d("Assignment Id", "Issue in API Call")
        }

    }


    private fun showEmptyState(message: String) {
        binding.rcyAssignment.visibility = View.GONE
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
    }


    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onSubmittedClick(data: AssignmentData) {
        TODO("Not yet implemented")
    }

    override fun onEditAndDeleteClick(
        data: AssignmentData,
        anchorView: View,
        adapterPosition: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onNotSubmittedClick(data: AssignmentData) {
        TODO("Not yet implemented")
    }
}