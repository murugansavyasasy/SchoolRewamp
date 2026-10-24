package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model.FeeDetailsResponse
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.ModelConvert.FeeUiMapper
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StudentFeeDetailsBinding


class StudentFeeDetails : BaseActivity<StudentFeeDetailsBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var mAdapter: StudentFeeAdapter
    private var studentId: Int = 0
    private var academicYearId: Int = 0
    private var name: String = ""
    private var class_name: String = ""
    private var section_name: String = ""

    override fun getViewBinding(): StudentFeeDetailsBinding {
        return StudentFeeDetailsBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.rytSearchBar.visibility = View.GONE

        studentId = intent.getIntExtra("student_id", 0)
        academicYearId = intent.getIntExtra("acadamic_year_id", 0)
        name = intent.getStringExtra("student_name") ?: ""
        class_name = intent.getStringExtra("student_class") ?: ""
        section_name = intent.getStringExtra("student_section") ?: ""
        binding.toolbarLayout.lblParentToolBar.text = name
        binding.toolbarLayout.lblSchoolName.text = "${class_name}-${section_name}"

        binding.rcyStudentFeeDetails.layoutManager = LinearLayoutManager(this)

        appViewModel!!.isStudentFeeDetails?.observe(this) { response ->
            Constant.hideLoading(this@StudentFeeDetails)
            showFees(response)
        }

        loadFeeDetails()
    }

    private fun loadFeeDetails() {
        Constant.showLoading(this)
        val countryId = SharedPreference.getCountryId(this@StudentFeeDetails)
        appViewModel!!.isStudentFeeDetails(
            isAccessToken!!, studentId, academicYearId, countryId, activity = this
        )
    }

    private fun showFees(response: FeeDetailsResponse?) {
        val data = response?.data?.firstOrNull()

        if (response?.status == true && data != null) {
            binding.rcyStudentFeeDetails.visibility = View.VISIBLE
            binding.lytNoDataFound.visibility = View.GONE
            mAdapter = StudentFeeAdapter(FeeUiMapper.build(this, data))
            binding.rcyStudentFeeDetails.adapter = mAdapter
        } else {
            showNoData(
                when {
                    response == null -> getString(R.string.Something_went_wrong_Please_try_again)
                    response.status != true -> response.message ?: getString(R.string.no_data_found)
                    else -> getString(R.string.no_data_found)
                }
            )
        }
    }

    private fun showNoData(message: String) {
        binding.rcyStudentFeeDetails.visibility = View.GONE
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = message
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}