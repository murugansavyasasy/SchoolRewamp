package com.vs.schoolmessenger.School.SchoolStrength


import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.SchoolStrength.Adapter.SchoolStrengthAdapter
import com.vs.schoolmessenger.School.SchoolStrength.Adapter.SchoolStrengthDetailAdapter
import com.vs.schoolmessenger.School.SchoolStrength.Model.SchoolData
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolStrengthBinding


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
    var isFirstLoad = false

    private lateinit var schoolstrengthadapter: SchoolStrengthAdapter
    private lateinit var schoolstrengthdetailadapter: SchoolStrengthDetailAdapter

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.AcademicYear.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = "School Strength"
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }

        // Setup LayoutManager only
        binding.rlaabsenteesreport2.layoutManager = LinearLayoutManager(this)


        appViewModel!!.isGetAcademicList?.observe(this) { response ->
            response?.data?.let { academicList ->
                val reorderedList = academicList.sortedByDescending { it.current_academic_year }
                if (isAcademicYear == reorderedList) return@observe
                isAcademicYear = reorderedList
                isLoadAcademicYear(isAcademicYear)
                isValidAcademicYear =
                    isAcademicYear?.any { it.current_academic_year == true } == true
                isAcademicYearId = isAcademicYear!![0].id
                isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
                isGetSchoolStrength()
            }
        }

        appViewModel?.isGetSchoolStrengthReport?.observe(this) { response ->
            if (response != null && response.status) {
                isFirstLoad = true
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.rlaPieChartCount.visibility = View.VISIBLE
                binding.rlaabsenteesreport2.visibility = View.VISIBLE
                //isLoadSchoolStrengthData(response.data)
                setupPieChart(response.data)
            } else {
                 binding.nomessage.visibility = View.VISIBLE
                 binding.txtNoData.visibility = View.VISIBLE
                binding.rlaPieChartCount.visibility = View.GONE
                binding.rlaabsenteesreport2.visibility = View.GONE
            }
        }
        isGetAcademicYear()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
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
                    isGetSchoolStrength()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isGetAcademicYear() {
        appViewModel!!.isGetAcademicYear(isAccessToken!!, this)
    }

    private fun isGetSchoolStrength() {
        appViewModel?.isGetSchoolStrengthReport(isAccessToken ?: "", isAcademicYearId, this)
    }


    private fun setupPieChart(data: List<SchoolData>?) {
        if (data.isNullOrEmpty()) return

        val totalStudentStrength = data[0].totalStudentStrength.toFloatOrNull() ?: 0f
        val totalStaffStrength = data[0].totalStaffStrength.toFloatOrNull() ?: 0f
        val totalBoysStrength = data[0].totalBoysStrength.toFloatOrNull() ?: 0f
        val totalGirlsStrength = data[0].totalGirlsStrength.toFloatOrNull() ?: 0f
        val totalothersStrength = data[0].totalOthersStrength.toFloatOrNull() ?: 0f

        val total = totalBoysStrength + totalGirlsStrength + totalStaffStrength + totalothersStrength // 22
        binding.customPieChart.setUsePercentValues(true)
        binding.customPieChart.getDescription().setEnabled(false)
        binding.customPieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        // on below line we are setting drag for our pie chart
        binding.customPieChart.setDragDecelerationFrictionCoef(0.95f)
        // on below line we are setting hole
        // and hole color for pie chart
        binding.customPieChart.setDrawHoleEnabled(true)
        binding.customPieChart.setHoleColor(Color.WHITE)
        // on below line we are setting circle color and alpha
        binding.customPieChart.setTransparentCircleColor(Color.WHITE)
        binding.customPieChart.setTransparentCircleAlpha(110)
        // on  below line we are setting hole radius
        binding.customPieChart.setHoleRadius(58f)
        binding.customPieChart.setTransparentCircleRadius(61f)
        // on below line we are setting center text
        binding.customPieChart.setDrawCenterText(true)
        binding.customPieChart.centerText = "${total.toInt()}\nTotal"
        binding.customPieChart.setCenterTextSize(12f)
        // on below line we are setting
        // rotation for our pie chart
        binding.customPieChart.setRotationAngle(0f)
        // enable rotation of the pieChart by touch
        binding.customPieChart.setRotationEnabled(true)
        binding.customPieChart.setHighlightPerTapEnabled(true)
        // on below line we are setting animation for our pie chart
        binding.customPieChart.animateY(1400, Easing.EaseInOutQuad)
        // on below line we are disabling our legend for pie chart
        binding.customPieChart.legend.isEnabled = false
        binding.customPieChart.setEntryLabelColor(Color.WHITE)
        binding.customPieChart.setEntryLabelTextSize(12f)
        binding.customPieChart.setUsePercentValues(false)
        // on below line we are creating array list and
        // adding data to it to display in pie chart
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(totalStaffStrength))
        entries.add(PieEntry(totalBoysStrength))
        entries.add(PieEntry(totalGirlsStrength))
        if(!data[0].totalOthersStrength.equals("0")) {
            entries.add(PieEntry(totalothersStrength))
        }
        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "")
        // on below line we are setting icons.
        dataSet.setDrawIcons(false)
        // on below line we are setting slice for pie
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(resources.getColor(R.color.yellow))
        colors.add(resources.getColor(R.color.teal))
        colors.add(resources.getColor(R.color.pink))
        if(!data[0].totalOthersStrength.equals("0")) {
            colors.add(resources.getColor(R.color.green))
        }
        // on below line we are setting colors.
        dataSet.colors = colors
        // on below line we are setting pie data set
        val chart_data = PieData(dataSet)
//        chart_data.setValueFormatter(PercentFormatter())
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()  // removes decimal points
            }
        }
        chart_data.setValueTextSize(10f)
        chart_data.setValueTypeface(Typeface.DEFAULT_BOLD)
        chart_data.setValueTextColor(Color.WHITE)
        binding.customPieChart.setData(chart_data)
        // undo all highlights
        binding.customPieChart.highlightValues(null)
        // loading chart
        binding.customPieChart.invalidate()
        binding.staffCount.text = "Staff - ${totalStaffStrength.toInt()}"
        binding.totalStudentCount.text = "Students - ${totalStudentStrength.toInt()}"
        binding.girlsCount.text = "Girls - ${totalGirlsStrength.toInt()}"
        binding.boysCount.text = "Boys - ${totalBoysStrength.toInt()}"
        binding.othersCount.text = "Others - ${totalothersStrength.toInt()}"

        val standardList = data.flatMap { it.standards ?: emptyList() }
        Log.d("StandardList", "Size: ${standardList.size} | Data: $standardList")
        schoolstrengthadapter = SchoolStrengthAdapter(standardList, this, false)
        binding.rlaabsenteesreport2.adapter = schoolstrengthadapter
    }
}
