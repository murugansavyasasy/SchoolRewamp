package com.vs.schoolmessenger.Parent.Attendance

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AttendanceReportParentBinding

class AttendanceReport : BaseActivity<AttendanceReportParentBinding>(), View.OnClickListener {

    override fun getViewBinding(): AttendanceReportParentBinding {
        return AttendanceReportParentBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: AttendanceReportAdapter
    private lateinit var studentsList: List<AttendanceReportStudentData>


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.AttendanceReport)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        loadData()
    }

    override fun onClick(p0: View?) {

    }

    fun loadData() {
        studentsList = listOf(

            AttendanceReportStudentData(
                "Sathish Ganesan", "76979871",
                "Present"
            ),

            AttendanceReportStudentData(
                "Murugan", "22439234",
                "Absent"
            ),
            AttendanceReportStudentData(
                "Saran Raj", "259411563",
                "Present"
            ),
            AttendanceReportStudentData(
                "Chanthru", "216098214",
                "Absent"
            ),
            AttendanceReportStudentData(
                "Ramesh", "90509568",
                "Present"
            ),
            AttendanceReportStudentData(
                "Lakshmanan Narayanan", "90509568",
                "Absent"
            ),
            AttendanceReportStudentData(
                "Gunal", "90509568",
                "Present"
            ),
            AttendanceReportStudentData(
                "Lakshmanan", "90509568",
                "Absent"
            ),
            AttendanceReportStudentData(
                "Narayanan", "90509568",
                "Present"
            ), AttendanceReportStudentData(
                "Gunal", "90509568",
                "Present"
            ),
            AttendanceReportStudentData(
                "Lakshmanan", "90509568",
                "Absent"
            ), AttendanceReportStudentData(
                "Gunal", "90509568",
                "Present"
            ),
            AttendanceReportStudentData(
                "Lakshmanan", "90509568",
                "Absent"
            )
        )


        mAdapter = AttendanceReportAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAttendanceReport.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter =
                AttendanceReportAdapter(studentsList, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyAttendanceReport.adapter = mAdapter
        }
    }

}