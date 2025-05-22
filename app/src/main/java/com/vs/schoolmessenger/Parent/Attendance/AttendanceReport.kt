package com.vs.schoolmessenger.Parent.Attendance
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceReportParentBinding

class AttendanceReport : BaseActivity<AttendanceReportParentBinding>(), View.OnClickListener {

    override fun getViewBinding(): AttendanceReportParentBinding {
        return AttendanceReportParentBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: AttendanceReportAdapter
    private lateinit var attendanceReportList: List<AttendanceReportStudentData>
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.AttendanceReport)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
//        loadData()
    }
//        isChildDetails = SharedPreference.getChildDetails(this)
//        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
//        binding.toolbarLayout.lblStudentSection.text =
//            isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name
//
//        isAccessToken = isChildDetails?.access_token
//        appViewModel = ViewModelProvider(this)[App::class.java]
//        appViewModel!!.init()
//
//        loadData()

//        appViewModel!!.isChildAttendanceReport?.observe(this) { response ->
//            if (response != null) {
//                if (response.status) {
//                    binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
//                    binding.rcyAttendanceReport.visibility = View.VISIBLE
//                    attendanceReportList!!.isEmpty()
//                    attendanceReportList = response.data
//                    binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
//                    binding.rcyAttendanceReport.adapter = mAdapter
//                    mAdapter = AttendanceReportAdapter(
//                        attendanceReportList,
//                        this,
//                        Constant.isShimmerViewDisable
//                    )
//                    binding.rcyAttendanceReport.adapter = mAdapter
//
//                } else {
//                    binding.rcyAttendanceReport.visibility = View.GONE
//                    binding.toolbarLayout.rytSearch.visibility = View.GONE

//    fun loadData() {
//        studentsList = listOf(
//
//            AttendanceReportStudentData(
//                "Sathish Ganesan", "76979871",
//                "Present"
//            ),
//
//            AttendanceReportStudentData(
//                "Murugan", "22439234",
//                "Absent"
//            ),
//            AttendanceReportStudentData(
//                "Saran Raj", "259411563",
//                "Present"
//            ),
//            AttendanceReportStudentData(
//                "Chanthru", "216098214",
//                "Absent"
//            ),
//            AttendanceReportStudentData(
//                "Ramesh", "90509568",
//                "Present"
//            ),
//            AttendanceReportStudentData(
//                "Lakshmanan Narayanan", "90509568",
//                "Absent"
//            ),
//            AttendanceReportStudentData(
//                "Gunal", "90509568",
//                "Present"
//            ),
//            AttendanceReportStudentData(
//                "Lakshmanan", "90509568",
//                "Absent"
//            ),
//            AttendanceReportStudentData(
//                "Narayanan", "90509568",
//                "Present"
//            ), AttendanceReportStudentData(
//                "Gunal", "90509568",
//                "Present"
//            ),
//            AttendanceReportStudentData(
//                "Lakshmanan", "90509568",
//                "Absent"
//            ), AttendanceReportStudentData(
//                "Gunal", "90509568",
//                "Present"
//            ),
//            AttendanceReportStudentData(
//                "Lakshmanan", "90509568",
//                "Absent"
//            )
//        )
//
//
//        mAdapter = AttendanceReportAdapter(null, this, Constant.isShimmerViewShow)
//        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
//        binding.rcyAttendanceReport.adapter = mAdapter
//        Constant.executeAfterDelay {
//            mAdapter =
//                AttendanceReportAdapter(studentsList, this, Constant.isShimmerViewDisable)
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyAttendanceReport.adapter = mAdapter
//        }
//    }
//                }
//            }
//        }
//    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    fun showShimmer() {
        val shimmerAdapter = AttendanceReportAdapter(
            null,
            this,
            Constant.isShimmerViewShow
        )
        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAttendanceReport.isNestedScrollingEnabled = false
        binding.rcyAttendanceReport.adapter = shimmerAdapter
    }

    fun loadData() {
        showShimmer()
        appViewModel!!.getChildAttendanceReport(
            isAccessToken!!, activity = this
        )
    }

}