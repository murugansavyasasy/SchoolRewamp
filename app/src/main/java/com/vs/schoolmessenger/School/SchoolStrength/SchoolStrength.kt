package com.vs.schoolmessenger.School.SchoolStrength


import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.compose.ui.BiasAbsoluteAlignment
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
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAcademicYearList
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolStrengthBinding


class SchoolStrength : BaseActivity<SchoolStrengthBinding>(), View.OnClickListener {

    override fun getViewBinding(): SchoolStrengthBinding {
        return SchoolStrengthBinding.inflate(layoutInflater)
    }

    private var isValidAcademicYear = false
    private var isAcademicYearId = 0
    private var isCurrentAcademicYear = true
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    var isFirstLoad = false

    private lateinit var schoolstrengthadapter: SchoolStrengthAdapter

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.AcademicYear.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }

        // Setup LayoutManager only
        binding.rcysummarystatics.layoutManager = LinearLayoutManager(this)
        binding.rlaabsenteesreport2.layoutManager = LinearLayoutManager(this)


        isLoadAcademicYear(isAcademicYearList)
        isValidAcademicYear =
            isAcademicYearList?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYearList!![0].id
        isCurrentAcademicYear = isAcademicYearList!![0].current_academic_year
        isGetSchoolStrength()

        appViewModel?.isGetSchoolStrengthReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    isFirstLoad = true
                    binding.nomessage.visibility = View.GONE
                    binding.txtNoData.visibility = View.GONE
                    binding.rlaabsenteesreport2.visibility = View.VISIBLE
                    binding.rcysummarystatics.visibility = View.VISIBLE
                } else {
                    binding.txtNoData.text = response.message
                    binding.nomessage.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.rlaabsenteesreport2.visibility = View.GONE
                    binding.rcysummarystatics.visibility = View.GONE
                }
            } else {
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rlaabsenteesreport2.visibility = View.GONE
                binding.rcysummarystatics.visibility = View.GONE
            }
        }
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


    private fun isGetSchoolStrength() {
        Constant.showLoading(this)
        appViewModel?.isGetSchoolStrengthReport(isAccessToken ?: "", isAcademicYearId, this)
    }

}