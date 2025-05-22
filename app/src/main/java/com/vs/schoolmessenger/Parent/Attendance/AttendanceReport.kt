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
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        loadData()

        appViewModel!!.isChildAttendanceReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                    binding.rcyAttendanceReport.visibility = View.VISIBLE
                    attendanceReportList!!.isEmpty()
                    attendanceReportList = response.data
                    binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
                    binding.rcyAttendanceReport.adapter = mAdapter
                    mAdapter = AttendanceReportAdapter(
                        attendanceReportList,
                        this,
                        Constant.isShimmerViewDisable
                    )
                    binding.rcyAttendanceReport.adapter = mAdapter

                } else {
                    binding.rcyAttendanceReport.visibility = View.GONE
                    binding.toolbarLayout.rytSearch.visibility = View.GONE

                }
            }
        }
    }

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