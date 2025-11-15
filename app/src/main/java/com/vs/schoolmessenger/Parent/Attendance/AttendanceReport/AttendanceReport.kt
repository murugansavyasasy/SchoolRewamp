package com.vs.schoolmessenger.Parent.Attendance.AttendanceReport

import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )


        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblParentToolBar.text = getString(R.string.leave_history)
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentName.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.toolbarLayout.lblStudentSection.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.toolbarLayout.imgBack.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
        binding.toolbarLayout.imgSearchToolBar.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch.visibility == View.VISIBLE) {
                binding.rytSearch.visibility = View.GONE
                binding.txtVideoMenu.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtVideoMenu.windowToken, 0)
            } else {
                binding.rytSearch.visibility = View.VISIBLE
                binding.txtVideoMenu.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtVideoMenu.windowToken, 0)
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
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                    binding.rcyAttendanceReport.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE

                    attendanceReportList = dataList
                    mAdapter = AttendanceReportAdapter(
                        attendanceReportList,
                        this,
                        this,
                        Constant.isShimmerViewDisable
                    )
                    binding.rcyAttendanceReport.layoutManager = GridLayoutManager(this, 2)
                    binding.rcyAttendanceReport.adapter = mAdapter
                } else {
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                    binding.rcyAttendanceReport.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text=response.message?:getString(R.string.no_data_found)
                }
            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                binding.rcyAttendanceReport.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text=response?.message?:getString(R.string.something_went_wrong_please_try_again_later)

            }
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_report_found)
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
        binding.rcyAttendanceReport.layoutManager = GridLayoutManager(this, 2)
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
