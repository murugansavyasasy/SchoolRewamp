package com.vs.schoolmessenger.School.FeePendingReport

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeeData
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel.FeePendingCollectionDisplayItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeePendingReportBinding

class FeePendingReport : BaseActivity<FeePendingReportBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeePendingReportBinding {
        return FeePendingReportBinding.inflate(layoutInflater)
    }

    private var isAcademicYear: List<AcademicYear>? = null
    private var isValidAcademicYear = false
    private var isAcademicYearId = 0
    private var isCurrentAcademicYear = true
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var mAdapter: FeePendingReportAdapter? = null
    var isFirstLoad = false
    private var isClassWiseSelected = false

    private var country_id: String? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        country_id = SharedPreference.getCountryId(this)?.toString()


        val params =
            binding.toolbarLayout.lytTitleAndName.layoutParams as RelativeLayout.LayoutParams// Get current layout params (RelativeLayout.LayoutParams)
        params.removeRule(RelativeLayout.START_OF)// Remove the old rule
        params.addRule(
            RelativeLayout.START_OF,
            R.id.rlaSpinner
        )// Add the new rule -> align to start of rlaSpinner
        binding.toolbarLayout.lytTitleAndName.layoutParams = params// Re-apply params


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.lnrTabTwoName.setOnClickListener(this)
        binding.lnrTabOneName.setOnClickListener(this)


        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.rlaSpinner.visibility = View.VISIBLE


        isLoadAcademicYear(isAcademicYearList)
        isValidAcademicYear =
            isAcademicYearList?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYearList!![0].id
        isCurrentAcademicYear = isAcademicYearList!![0].current_academic_year
        if (isClassWiseSelected) {
            isGetDailyWiseCollection()
        } else {
            isGetDailyCollection()
        }

        appViewModel?.isDetailedPendingReport?.observe(this) { response ->
            Constant.hideLoading(this@FeePendingReport)
            Log.d("response++", response.toString())
            mAdapter?.clearData()
            if (response != null) {
                if (response != null && response.status && !response.data.isNullOrEmpty()) {
                    isFirstLoad = true
                    isLoadDailyCollectionData(response.data)
                    binding.relativeLayout6.visibility = View.VISIBLE
                } else {
                    showNoDataMessage(
                        response?.message ?: getString(R.string.no_fee_pending_data_available)
                    )
                    binding.relativeLayout6.visibility = View.GONE
                }
            }
        }

        appViewModel?.isDetailedWisePendingReport?.observe(this) { response ->
            Constant.hideLoading(this@FeePendingReport)
            Log.d("response++", response.toString())
            mAdapter?.clearData()
            if (response != null) {
                if (response != null && response.status && !response.data.isNullOrEmpty()) {
                    isFirstLoad = true
                    isLoadDailyCollectionData(response.data)
                    binding.relativeLayout6.visibility = View.VISIBLE
                } else {
                    showNoDataMessage(
                        response?.message ?: getString(R.string.no_fee_pending_data_available)
                    )
                    binding.relativeLayout6.visibility = View.GONE
                }
            }
        }
    }

    private fun isLoadDailyCollectionData(data: List<FeeData>?) {
        val flatList = mutableListOf<FeePendingCollectionDisplayItem>()

        if (data.isNullOrEmpty()) {
            showNoDataMessage(getString(R.string.no_fee_pending_data_available))
            return
        }

        data.forEach { pendingData ->
            pendingData.pending_details?.forEach { item ->
                if (!item.category.isNullOrEmpty()) {
                    val feeList = item.pending_data?.map { fee ->
                        FeePendingCollectionDisplayItem.Fee(
                            fee.type_name ?: getString(R.string.Unknown),
                            fee.amount ?: "0"
                        )
                    } ?: emptyList()

                    flatList.add(
                        FeePendingCollectionDisplayItem.Header(
                            item.category ?: getString(R.string.Unknown),
                            item.total ?: "0",
                            feeList
                        )
                    )
                }

            }
        }

        if (flatList.isEmpty()) {
            showNoDataMessage(getString(R.string.no_fee_pending_data_available))
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.totalsummary1.visibility = View.VISIBLE
            binding.relativeLayout5.visibility = View.VISIBLE
            binding.relativeLayout6.visibility = View.VISIBLE

            mAdapter = FeePendingReportAdapter(flatList, this)
            binding.totalsummary1.layoutManager = LinearLayoutManager(this)
            binding.totalsummary1.adapter = mAdapter
            val totalCollectionSum = data[0].total_pending
            binding.totalCollection.text = totalCollectionSum.toString()

        }
    }

    private fun showNoDataMessage(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.totalsummary1.visibility = View.GONE
        binding.relativeLayout5.visibility = View.GONE
        binding.relativeLayout6.visibility = View.GONE
    }

    private fun isGetDailyCollection() {
        showLoadingAndResetList()
        appViewModel?.isDetailedPendingReport(
            isAccessToken ?: "",
            isAcademicYearId,
            country_id ?: "",
            this
        )
    }

    private fun isGetDailyWiseCollection() {
        showLoadingAndResetList()
        appViewModel?.isDetailedWisePendingReport(
            isAccessToken ?: "",
            isAcademicYearId,
            country_id ?: "",
            this
        )
    }

    private fun resetListUI() {
        binding.totalsummary1.visibility = View.GONE
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.relativeLayout5.visibility = View.GONE
        binding.relativeLayout6.visibility = View.GONE

        mAdapter?.clearData()
        mAdapter = FeePendingReportAdapter(emptyList(), this)
        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
        binding.totalsummary1.adapter = mAdapter
    }

    private fun showLoadingAndResetList() {
        Constant.showLoading(this@FeePendingReport)
        resetListUI()
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = NewAcademicYearAdapter(this, isAcademicYear)
        binding.toolbarLayout.isAcademicSpinner.adapter = adapter
        binding.toolbarLayout.isAcademicSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    if (isFirstLoad) {
                        val selectedOption = isAcademicYear!![position]
                        isAcademicYearId = selectedOption.id
                        isCurrentAcademicYear = selectedOption.current_academic_year

                        Log.d(
                            "DropdownMenu",
                            "Clicked Academic Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                        )

                        if (isClassWiseSelected) {
                            isGetDailyWiseCollection()
                        } else {
                            isGetDailyCollection()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lnrTabTwoName -> {
                if (isClassWiseSelected) return
                isClassWiseSelected = true
                binding.lnrTabOneName.isEnabled = true
                binding.lnrTabTwoName.isEnabled = false
                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
                binding.line2.setBackgroundResource(R.color.iconBlue)
                binding.line1.setBackgroundResource(R.color.athens_gray)
                isGetDailyWiseCollection()
            }

            R.id.lnrTabOneName -> {
                if (!isClassWiseSelected) return
                isClassWiseSelected = false
                binding.lnrTabOneName.isEnabled = false
                binding.lnrTabTwoName.isEnabled = true
                binding.line1.setBackgroundResource(R.color.iconBlue)
                binding.tabOneName.setTextColor(ContextCompat.getColor(this, R.color.iconBlue))
                binding.tabTwoName.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.line2.setBackgroundResource(R.color.athens_gray)
                isGetDailyCollection()

            }

        }
    }
}