package com.vs.schoolmessenger.School.StudentReport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.StudentReportBinding

class StudentReport : BaseActivity<StudentReportBinding>(), View.OnClickListener,StudentReportClickListener {

    lateinit var mAdapter: StudentReportAdapter
    private lateinit var isStudentReportData: List<StudentReportData>

    override fun getViewBinding(): StudentReportBinding {
        return StudentReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        loadData()

    }

    override fun onClick(p0: View?) {

    }


    private fun loadData() {
        isStudentReportData = listOf(
            StudentReportData(
                "4783567951",
"Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            ),
            StudentReportData(
                "4783567951",
                "Male",                "Apr 1 , 2001","Sathish","Ganesan","Abi","6382677672","sathishg079@gmail.com",
            )
        )

        mAdapter = StudentReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyStudentReport.layoutManager = LinearLayoutManager(this)
        binding.rcyStudentReport.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                StudentReportAdapter(isStudentReportData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyStudentReport.adapter = mAdapter
        }

    }

    override fun onItemClick(data: StudentReportData) {

    }
}