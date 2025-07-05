package com.vs.schoolmessenger.Parent.Assignment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AssignmentParentBinding

class Assignment : BaseActivity<AssignmentParentBinding>(), View.OnClickListener
     {

    override fun getViewBinding(): AssignmentParentBinding {
        return AssignmentParentBinding.inflate(layoutInflater)
    }

    private lateinit var isAssignmentData: List<AssignmentData>
    lateinit var mAdapter: AssignmentAdapter
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Assignment)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        loadData()
    }

    override fun onClick(p0: View?) {

    }

    private fun loadData() {

//
//        mAdapter = AssignmentAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.rcyAssignment.layoutManager = LinearLayoutManager(this)
//        binding.rcyAssignment.adapter = mAdapter
//
//        Constant.executeAfterDelay {
//            // Once data is loaded, stop shimmer and pass the actual data
//            mAdapter =
//                AssignmentAdapter(isAssignmentData, this, this, Constant.isShimmerViewDisable)
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyAssignment.adapter = mAdapter
//        }
    }
}