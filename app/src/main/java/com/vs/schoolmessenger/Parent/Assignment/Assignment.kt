package com.vs.schoolmessenger.Parent.Assignment

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentParentBinding

class Assignment : BaseActivity<AssignmentParentBinding>(), AssignmentClickListener,
    View.OnClickListener {

    override fun getViewBinding(): AssignmentParentBinding {
        return AssignmentParentBinding.inflate(layoutInflater)
    }

    var isAssignmentAdapter: AssignmentParentAdapter? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isAssignmentReportData: List<ParentAssignmentData>? = null

    lateinit var mAdapter: AssignmentAdapter
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Assignment)
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.lblStudentName.text = childDetails!!.name
        binding.toolbarLayout.lblStudentSection.text = childDetails.standard_name + " - " + childDetails.section_name

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.rcyAssignment.layoutManager = LinearLayoutManager(this)

        appViewModel?.isAssignmentlist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                isAssignmentReportData = response.data
                loadAssignmentReportData()
                binding.rcyAssignment.visibility = View.VISIBLE
                binding.lytNoDataFound.visibility = View.GONE
            } else {
                binding.rcyAssignment.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
                binding.noDataFound.text = response?.message
            }
        }

        fetchAssignmentReportData()
    }

    private fun fetchAssignmentReportData() {
        binding.rcyAssignment.visibility = View.VISIBLE
        isAssignmentAdapter = AssignmentParentAdapter(mutableListOf(), this, this, Constant.isShimmerViewShow)
        binding.rcyAssignment.adapter = isAssignmentAdapter

        appViewModel?.isAssignmentlist(isAccessToken!!)
    }

    private fun loadAssignmentReportData() {
        binding.rcyAssignment.visibility = View.VISIBLE
        isAssignmentAdapter = AssignmentParentAdapter(
            isAssignmentReportData!!.toMutableList(), this, this, Constant.isShimmerViewDisable
        )
        binding.rcyAssignment.adapter = isAssignmentAdapter
    }


    override fun onSubmittedClick(data: AssignmentData) {
    }

    override fun onEditAndDeleteClick(
        data: AssignmentData,
        anchorView: View,
        adapterPosition: Int
    ) {
    }

    override fun onNotSubmittedClick(data: AssignmentData) {
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}