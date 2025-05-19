package com.vs.schoolmessenger.School.FeePendingReport

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingData
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeePendingReportBinding
import kotlin.collections.forEach
import kotlin.text.isNullOrEmpty

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

    private var isClassWiseSelected = false

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.AcademicYear.setOnClickListener(this)
        binding.categoryName.setOnClickListener(this)
        binding.className.setOnClickListener(this)

        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isValidAcademicYear = isAcademicYear?.any { it.current_academic_year == true } == true

                val defaultYear = isAcademicYear!!.first()
                binding.lblAcademicYear.text = defaultYear.year
                isAcademicYearId = defaultYear.id
                isCurrentAcademicYear = defaultYear.current_academic_year

                if (isClassWiseSelected) {
                    isGetDailyWiseCollection()
                } else {
                    isGetDailyCollection()
                }
            }
        }

        isGetAcademicYear()

        appViewModel?.isDetailedPendingReport?.observe(this) { response ->
            Log.d("response++", response.toString())
            mAdapter?.clearData()

            if (response != null && response.status && !response.data.isNullOrEmpty()) {
                isLoadDailyCollectionData(response.data)
            } else {
                showNoDataMessage()
            }
        }

        appViewModel?.isDetailedWisePendingReport?.observe(this) { response ->
            Log.d("response++", response.toString())
            mAdapter?.clearData()

            if (response != null && response.status && !response.data.isNullOrEmpty()) {
                isLoadDailyCollectionData(response.data)
            } else {
                showNoDataMessage()
            }
        }
    }

    private fun isLoadDailyCollectionData(data: List<FeePendingCollectionItem>?) {
        val flatList = mutableListOf<DisplayItem>()

        if (data.isNullOrEmpty()) {
            showNoDataMessage()
            return
        }

        data.forEach { item ->
            if (!item.category.isNullOrEmpty()) {
                flatList.add(DisplayItem.Header(item.category ?: "Unknown", item.total ?: "0"))
            }

            item.pending_data?.forEach { fee ->
                flatList.add(DisplayItem.Fee(fee.type_name ?: "Unknown", fee.amount ?: "0"))
            }
        }

        if (flatList.isEmpty()) {
            showNoDataMessage()
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.totalsummary1.visibility = View.VISIBLE

            mAdapter = FeePendingReportAdapter(flatList, this)
            binding.totalsummary1.layoutManager = LinearLayoutManager(this)
            binding.totalsummary1.adapter = mAdapter
        }
    }

    private fun showNoDataMessage() {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.visibility = View.VISIBLE
        binding.totalsummary1.visibility = View.GONE
    }

    private fun isGetDailyCollection() {
        resetListUI()
        appViewModel?.isDetailedPendingReport(
            isAccessToken ?: "",
            isAcademicYearId,
            this
        )
    }

    private fun isGetDailyWiseCollection() {
        resetListUI()
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

        mAdapter?.clearData()
        mAdapter = FeePendingReportAdapter(emptyList(), this)
        binding.totalsummary1.layoutManager = LinearLayoutManager(this)
        binding.totalsummary1.adapter = mAdapter
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.AcademicYear -> {
                showAcademicDropdown(
                    binding.AcademicYear, this, isAcademicYear
                ) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    isAcademicYearId = selectedYear.id
                    isCurrentAcademicYear = selectedYear.current_academic_year

                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )

                    // Respect tab selection
                    if (isClassWiseSelected) {
                        isGetDailyWiseCollection()
                    } else {
                        isGetDailyCollection()
                    }
                }
            }

            R.id.category_name -> {
                isClassWiseSelected = false
                binding.categoryName.setBackgroundResource(R.drawable.custom_category_background)
                binding.categoryName.setTextColor(Color.WHITE)
                binding.className.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.className.setTextColor(Color.BLACK)
                isGetDailyCollection()
            }

            R.id.class_name -> {
                isClassWiseSelected = true
                binding.className.setBackgroundResource(R.drawable.custom_category_background)
                binding.className.setTextColor(Color.WHITE)
                binding.categoryName.setBackgroundResource(R.drawable.custom_rounded_background2)
                binding.categoryName.setTextColor(Color.BLACK)
                isGetDailyWiseCollection()
            }
        }
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(
            isAccessToken!!,
            this
        )
    }
}
