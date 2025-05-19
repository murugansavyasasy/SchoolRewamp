package com.vs.schoolmessenger.School.SchoolStrength

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.CustomGraphView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionItem
import com.vs.schoolmessenger.School.DailyCollection.DcfAdapter
import com.vs.schoolmessenger.School.DailyCollection.DisplayItem
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportAdapter
import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.SchoolStrengthBinding
import kotlin.collections.forEach
import kotlin.text.isNullOrEmpty


class SchoolStrength : BaseActivity<SchoolStrengthBinding>(), View.OnClickListener {

    override fun getViewBinding(): SchoolStrengthBinding {
        return SchoolStrengthBinding.inflate(layoutInflater)
    }

    private var isAcademicYear: List<AcademicYear>? = null
    private var isValidAcademicYear = false
    private var isAcademicYearId = 0
    private var isCurrentAcademicYear = true
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.AcademicYear.setOnClickListener(this)


        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isValidAcademicYear = isAcademicYear?.any { it.current_academic_year } == true

                val defaultYear = isAcademicYear!!.first()
                binding.lblAcademicYear.text = defaultYear.year
                isAcademicYearId = defaultYear.id
                isCurrentAcademicYear = defaultYear.current_academic_year

                Log.d("DefaultAcademicYear", "ID: $isAcademicYearId, Year: ${defaultYear.year}")


                isGetSchoolStrength()
            }
        }


        appViewModel?.isGetSchoolStrengthReport?.observe(this) { response ->
            Log.d("response++", response.toString())

            if (response != null && response.status) {
                isLoadSchoolStrengthData(response.data)
            } else {
                // Handle empty/error state if needed
                // binding.nomessage.visibility = View.VISIBLE
                // binding.txtNoData.visibility = View.VISIBLE
                // binding.totalsummary1.visibility = View.GONE
            }
        }


        isGetAcademicYear()



        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Bar)
            .title("School Strength")
            .titleStyle(AAStyle().color("#FFFFFF"))
            .subtitle("Section Data")
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .categories(arrayOf("A Sec", "B Sec", "C Sec", "D Sec", "E Sec", "F Sec"))
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Strength")
                        .color("#53c0bd")
                        .data(arrayOf(32.0, 47.0, 30.0, 55.0, 32.0, 47.0)),
                )
            )
        binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.AcademicYear -> {
                showAcademicDropdown(binding.AcademicYear, this, isAcademicYear) { selectedYear ->
                    binding.lblAcademicYear.text = selectedYear.year
                    isAcademicYearId = selectedYear.id
                    isCurrentAcademicYear = selectedYear.current_academic_year

                    Log.d(
                        "DropdownMenu",
                        "Clicked Academic Year: ID = ${selectedYear.id}, Year = ${selectedYear.year}, Current = ${selectedYear.current_academic_year}"
                    )
                    isGetSchoolStrength()
                }
            }
        }
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(isAccessToken!!, this)
    }

    private fun isGetSchoolStrength() {
        appViewModel?.isGetSchoolStrengthReport(isAccessToken ?: "", isAcademicYearId, this)
    }

    private fun isLoadSchoolStrengthData(data: List<SchoolData>?) {
        if (data.isNullOrEmpty()) return

        val totalStudentStrength = data[0].totalStudentStrength.toFloatOrNull() ?: 0f
        val totalStaffStrength = data[0].totalStaffStrength.toFloatOrNull() ?: 0f
        val total = totalStudentStrength + totalStaffStrength

        val chartData = listOf(
            Pair((totalStudentStrength / total) * 100, getColor(R.color.light_green_bg1)), // Students
            Pair((totalStaffStrength / total) * 100, getColor(R.color.red))                // Staff
        )

        binding.customPieChart.setData(chartData)

        binding.studentsData.text = "Students -${totalStudentStrength.toInt()}"
        binding.studentsData1.text = "Staff -${totalStaffStrength.toInt()}"
        binding.studentsData2.text = "Total -${total.toInt()}"
    }
}
