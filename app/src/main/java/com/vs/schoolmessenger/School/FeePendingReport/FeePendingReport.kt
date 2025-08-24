package com.vs.schoolmessenger.School.FeePendingReport

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
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

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.AcademicYear.setOnClickListener(this)
        binding.categoryName.setOnClickListener(this)
        binding.className.setOnClickListener(this)
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name


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

            if (response != null && response.status && !response.data.isNullOrEmpty()) {
                isFirstLoad = true
                isLoadDailyCollectionData(response.data)
                binding.relativeLayout6.visibility = View.GONE
            } else {
                showNoDataMessage(response?.message ?: "No fee pending data available.")
                binding.relativeLayout6.visibility = View.GONE
            }
        }

        appViewModel?.isDetailedWisePendingReport?.observe(this) { response ->
            Constant.hideLoading(this@FeePendingReport)
            Log.d("response++", response.toString())
            mAdapter?.clearData()

            if (response != null && response.status && !response.data.isNullOrEmpty()) {
                isFirstLoad = true
                isLoadDailyCollectionData(response.data)
                binding.relativeLayout6.visibility = View.GONE
            } else {
                showNoDataMessage(response?.message ?: "No fee pending data available.")
                binding.relativeLayout6.visibility = View.GONE
            }
        }
    }

    private fun isLoadDailyCollectionData(data: List<FeeData>?) {
        val flatList = mutableListOf<FeePendingCollectionDisplayItem>()

        if (data.isNullOrEmpty()) {
            showNoDataMessage("No fee pending data available.")
            return
        }

        data.forEach { pendingData ->
            pendingData.pending_details?.forEach { item ->
                if (!item.category.isNullOrEmpty()) {
                    val feeList = item.pending_data?.map { fee ->
                        FeePendingCollectionDisplayItem.Fee(
                            fee.type_name ?: "Unknown",
                            fee.amount ?: "0"
                        )
                    } ?: emptyList()

                    flatList.add(
                        FeePendingCollectionDisplayItem.Header(
                            item.category ?: "Unknown",
                            item.total ?: "0",
                            feeList
                        )
                    )
                }

            }
        }

        if (flatList.isEmpty()) {
            showNoDataMessage("No fee pending data available.")
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.totalsummary1.visibility = View.VISIBLE
            binding.relativeLayout5.visibility = View.VISIBLE

            mAdapter = FeePendingReportAdapter(flatList, this)
            binding.totalsummary1.layoutManager = LinearLayoutManager(this)
            binding.totalsummary1.adapter = mAdapter
            val totalCollectionSum = data.sumOf {
                it.total_pending.replace("₹", "").toDoubleOrNull() ?: 0.0
            }
            binding.totalCollection.text = "₹ %.2f".format(totalCollectionSum)

        }
    }

    private fun showNoDataMessage(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.totalsummary1.visibility = View.GONE
        binding.relativeLayout5.visibility = View.GONE
    }

    private fun isGetDailyCollection() {
        showLoadingAndResetList()
        appViewModel?.isDetailedPendingReport(
            isAccessToken ?: "",
            isAcademicYearId,
            this
        )
    }

    private fun isGetDailyWiseCollection() {
        showLoadingAndResetList()
        appViewModel?.isDetailedWisePendingReport(
            isAccessToken ?: "",
            isAcademicYearId,
            this
        )
    }

    private fun resetListUI() {
        binding.totalsummary1.visibility = View.GONE
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.relativeLayout5.visibility = View.GONE

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
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter
        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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


            R.id.category_name -> {
                if (!isClassWiseSelected) return
                isClassWiseSelected = false
                binding.categoryName.setBackgroundResource(R.drawable.white_radious)
                binding.categoryName.setTextColor(Color.BLACK)
                binding.className.setBackgroundResource(R.drawable.bg_light_blue)
                binding.className.setTextColor(Color.BLACK)
                isGetDailyCollection()
            }

            R.id.class_name -> {
                if (isClassWiseSelected) return
                isClassWiseSelected = true
                binding.className.setBackgroundResource(R.drawable.white_radious)
                binding.className.setTextColor(Color.BLACK)
                binding.categoryName.setBackgroundResource(R.drawable.bg_light_blue)
                binding.categoryName.setTextColor(Color.BLACK)
                isGetDailyWiseCollection()
            }
        }
    }


}
