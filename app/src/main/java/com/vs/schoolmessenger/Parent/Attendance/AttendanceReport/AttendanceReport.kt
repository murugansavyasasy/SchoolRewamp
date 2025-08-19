package com.vs.schoolmessenger.Parent.Attendance.AttendanceReport

import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AttendanceReportParentBinding

class AttendanceReport : BaseActivity<AttendanceReportParentBinding>(), View.OnClickListener,
    AttendanceReportClickListener {

    override fun getViewBinding(): AttendanceReportParentBinding {
        return AttendanceReportParentBinding.inflate(layoutInflater)
    }

    private lateinit var mAdapter: AttendanceReportAdapter
    private lateinit var attendanceReportList: List<AttendanceReportStudentData>
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

        // Toolbar setup
        binding.imgBack.setOnClickListener(this)
        binding.lblParentToolBar.text = getString(R.string.AttendanceReport)

        binding.rytSearch.visibility = View.VISIBLE


        isChildDetails = SharedPreference.getChildDetails(this)
        binding.lblStudentName.text = isChildDetails?.name ?: ""
        binding.lblStudentName.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.lblStudentSection.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
        binding.imgSearchBtn.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        binding.imgSearchBtn.setOnClickListener {
            if (binding.rytSearch.visibility == View.VISIBLE) {
                binding.rytSearch.visibility = View.GONE
            } else {
                binding.rytSearch.visibility = View.VISIBLE
                binding.txtVideoMenu.text.clear()
            }
        }



        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        loadData()


        binding.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })




        appViewModel!!.isChildAttendanceReport?.observe(this) { response ->
            if (response != null && response.status) {
                val dataList = response.data

                if (!dataList.isNullOrEmpty()) {

                    binding.rytSearch.visibility = View.VISIBLE
                    binding.rcyAttendanceReport.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE

                    attendanceReportList = dataList
                    mAdapter = AttendanceReportAdapter(
                        attendanceReportList,
                        this,
                        this,
                        Constant.isShimmerViewDisable
                    )
                    binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
                    binding.rcyAttendanceReport.adapter = mAdapter
                } else {

                    binding.rcyAttendanceReport.visibility = View.GONE
                    binding.rytSearch.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                }
            } else {

                binding.rcyAttendanceReport.visibility = View.GONE
                binding.rytSearch.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
            }
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = "No matching report found"
            binding.rcyAttendanceReport.visibility = View.GONE
        } else {
            binding.lytList.visibility = View.GONE
            binding.rcyAttendanceReport.visibility = View.VISIBLE
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    private fun showShimmer() {
        val shimmerAdapter = AttendanceReportAdapter(
            null,
            this,
            this,
            Constant.isShimmerViewShow
        )
        binding.rcyAttendanceReport.layoutManager = LinearLayoutManager(this)
        binding.rcyAttendanceReport.isNestedScrollingEnabled = false
        binding.rcyAttendanceReport.adapter = shimmerAdapter
    }

    private fun loadData() {
        showShimmer()
        appViewModel?.getChildAttendanceReport(
            isAccessToken.orEmpty(), activity = this
        )
    }
}
