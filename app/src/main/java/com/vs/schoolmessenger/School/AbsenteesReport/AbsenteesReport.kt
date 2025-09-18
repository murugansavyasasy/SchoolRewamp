package com.vs.schoolmessenger.School.AbsenteesReport

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesDetailData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.AbsenteesReport.Model.SectionWise
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AbsenteesReport : BaseActivity<AbsenteesReportBinding>(), View.OnClickListener,
    AbsenteesClickListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null

    private var absenteeList: List<AbsenteeData> = emptyList()

    override fun getViewBinding(): AbsenteesReportBinding {
        return AbsenteesReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName

        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name ?: ""

        isAccessToken = isStaffDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        fetchAbsenteeData()


        binding.calenderlayout.customCalendar.setOnDateSelectedListener { date ->
            filterByDate(date)
        }


        appViewModel?.getabsenteescountbydate?.observe(this) { response ->
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }
            if (response.status) {
                absenteeList = response.data ?: emptyList()
                setDefaultDateData()
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }
    }

    private fun fetchAbsenteeData() {
        appViewModel?.getabsenteescountbydate(
            isAccessToken ?: "",
            this
        )
    }

    private fun setDefaultDateData() {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        filterByDate(today)
    }

    private fun filterByDate(date: String) {
        val filtered = absenteeList.find { it.absent_date_only == date }

        if (filtered != null) {
            binding.selectedDateText.text = formatDateDisplay(date)
            loadClassWiseRecycler(filtered.class_wise)
        } else {
            binding.rlaabsenteesreport2.visibility = View.GONE
        }
    }

    private fun loadClassWiseRecycler(classWiseList: List<ClassWise>) {
        binding.rlaabsenteesreport2.visibility = View.VISIBLE
        binding.rlaabsenteesreport2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        val flatList = mutableListOf<Pair<ClassWise, SectionWise>>()
        classWiseList.forEach { classWise ->
            classWise.section_wise.forEach { section ->
                flatList.add(classWise to section)
            }
        }

        val adapter = AbsenteesReportDetailAdapter(flatList)
        binding.rlaabsenteesreport2.adapter = adapter
    }

    private fun formatDateDisplay(date: String): String {
        val input = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val output = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return try {
            output.format(input.parse(date)!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun showErrorUI(message: String) {
        binding.rlaabsenteesreport2.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onDateSelected(data: AbsenteeData) {
        filterByDate(data.absent_date_only)
    }
}
