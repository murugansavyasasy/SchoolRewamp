package com.vs.schoolmessenger.School.SchoolStrength


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
                isLoadSchoolStrengthData(response.data)
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

    private fun isLoadSchoolStrengthData(data: List<SchoolData>?) {
        if (data.isNullOrEmpty()) return

        val totalStudentStrength = data[0].totalStudentStrength.toFloatOrNull() ?: 0f
        val totalStaffStrength = data[0].totalStaffStrength.toFloatOrNull() ?: 0f
        val total = totalStudentStrength + totalStaffStrength

        val chartData = listOf(
            Pair((totalStudentStrength / total) * 100, getColor(R.color.light_green_bg1)),
            Pair((totalStaffStrength / total) * 100, getColor(R.color.red))
        )
        binding.customPieChart.setData(chartData)

        binding.studentsData.text = "Students - ${totalStudentStrength.toInt()}"
        binding.studentsData1.text = "Staff - ${totalStaffStrength.toInt()}"
        binding.studentsData2.text = "Total - ${total.toInt()}"

        val standardList = data.flatMap { it.standards ?: emptyList() }
        Log.d("StandardList", "Size: ${standardList.size} | Data: $standardList")


        schoolstrengthadapter = SchoolStrengthAdapter(standardList, this, false)
        binding.rlaabsenteesreport2.adapter = schoolstrengthadapter
    }
}
