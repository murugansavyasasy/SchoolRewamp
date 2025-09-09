package com.vs.schoolmessenger.School.LSRW

import android.R
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwHeaderAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.StudentListAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.TopPerformanceAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.WeeklyReportAdapter
import com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel.AvgSkillData
import com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel.AvgStudentSubmission
import com.vs.schoolmessenger.School.LSRW.Model.LsrwHeaderItem
import com.vs.schoolmessenger.School.LSRW.Model.TopPerformanceItem
import com.vs.schoolmessenger.School.LSRW.Model.WeeklyReportItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwReportstaticsBinding
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale

class LsrwReportAndStatics : BaseActivity<LsrwReportstaticsBinding>(), View.OnClickListener {

    override fun getViewBinding(): LsrwReportstaticsBinding {
        return LsrwReportstaticsBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        binding.toolbarLayout.lblParentToolBar.text = "Report & Analytics"
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name
        binding.toolbarLayout.lblDropDownMonth.visibility = View.VISIBLE
        binding.toolbarLayout.imgBack.setOnClickListener(this)


        setupMonthSpinner()


        appViewModel?.islsrwstats?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]

                val headerItems = mutableListOf<LsrwHeaderItem>()
                headerItems.add(LsrwHeaderItem("Today Submitted", "", "${data.today_submitted?.size ?: 0} Students"))
                headerItems.add(LsrwHeaderItem("Listening", data.listening?.over_all_percentage ?: "0%", "${data.listening?.student_count ?: "0"} Students"))
                headerItems.add(LsrwHeaderItem("Speaking", data.speaking?.over_all_percentage ?: "0%", "${data.speaking?.student_count ?: "0"} Students"))
                headerItems.add(LsrwHeaderItem("Reading", data.reading?.over_all_percentage ?: "0%", "${data.reading?.student_count ?: "0"} Students"))
                headerItems.add(LsrwHeaderItem("Writing", data.writing?.over_all_percentage ?: "0%", "${data.writing?.student_count ?: "0"} Students"))

                binding.rclsrwheader.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                val headerAdapter = LsrwHeaderAdapter(headerItems) { selected ->
                    filterByHeader(selected, data)
                }
                binding.rclsrwheader.adapter = headerAdapter

                val allDetails = mutableListOf<AvgStudentSubmission>()
                allDetails.addAll(data.reading?.details ?: emptyList())
                allDetails.addAll(data.speaking?.details ?: emptyList())
                allDetails.addAll(data.writing?.details ?: emptyList())

                val weeklyReport = calculateWeeklyReport(allDetails)
                binding.rvWeekly.layoutManager = LinearLayoutManager(this)
                binding.rvWeekly.adapter = WeeklyReportAdapter(weeklyReport)

                val topPerformers = calculateTopPerformers(allDetails)
                if (topPerformers.isNotEmpty()) {
                    binding.rvTopPerformance.visibility = View.VISIBLE
                    binding.rvTopPerformance.adapter = TopPerformanceAdapter(topPerformers)
                } else {
                    binding.rvTopPerformance.visibility = View.GONE
                }
                binding.rvTopPerformance.layoutManager = LinearLayoutManager(this)
                binding.rvTopPerformance.adapter = TopPerformanceAdapter(topPerformers)


                binding.rclsrwheader.visibility = View.VISIBLE
                binding.rvWeekly.visibility = View.VISIBLE
                binding.rvTopPerformance.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE
            } else {

                binding.rclsrwheader.visibility = View.GONE
                binding.rvWeekly.visibility = View.GONE
                binding.rvTopPerformance.visibility = View.GONE
                binding.noDataFound.visibility = View.VISIBLE
            }
        }

    }

    private fun setupMonthSpinner() {
        val months = DateFormatSymbols().months
        val monthList = months.take(12)

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, monthList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.toolbarLayout.lblDropDownMonth.adapter = adapter



        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        binding.toolbarLayout.lblDropDownMonth.setSelection(currentMonthIndex)

        binding.toolbarLayout.lblDropDownMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMonthNumber = position + 1
                fetchLsrwstatsReportData(selectedMonthNumber)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterByHeader(selected: LsrwHeaderItem, data: AvgSkillData) {
        val details = when (selected.title) {
            "Listening" -> data.listening?.details ?: emptyList()
            "Speaking" -> data.speaking?.details ?: emptyList()
            "Reading" -> data.reading?.details ?: emptyList()
            "Writing" -> data.writing?.details ?: emptyList()
            "Today Submitted" -> data.today_submitted ?: emptyList()
            else -> emptyList()
        }

        binding.rcstudents.layoutManager = LinearLayoutManager(this)
        binding.rcstudents.adapter = StudentListAdapter(details)
        binding.rcstudents.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateWeeklyReport(details: List<AvgStudentSubmission>): List<WeeklyReportItem> {



        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year

        val weeks = mutableMapOf<Int, MutableList<Int>>()

        details.forEach { detail ->
            val date = LocalDate.parse(detail.submission_date, formatter)
            if (date.monthValue == currentMonth && date.year == currentYear) {
                val weekOfMonth = date.get(WeekFields.of(Locale.getDefault()).weekOfMonth())
                val remarkValue = detail.remark.replace("%", "").toIntOrNull() ?: 0
                weeks.getOrPut(weekOfMonth) { mutableListOf() }.add(remarkValue)
            }
        }

        val weeklyReport = mutableListOf<WeeklyReportItem>()
        for (week in 1..6) {
            val values = weeks[week] ?: emptyList()
            val avg = if (values.isNotEmpty()) values.sum() / values.size else 0
            weeklyReport.add(WeeklyReportItem("Week $week", avg))
        }
        return weeklyReport
    }

    private fun calculateTopPerformers(details: List<AvgStudentSubmission>): List<TopPerformanceItem> {
        return details.map {
            TopPerformanceItem(
                studentName = it.student_name,
                className = "Class ${it.std_sec}",
                percentage = it.remark.replace("%", "").toIntOrNull() ?: 0
            )
        }
            .filter {it.percentage > 0}
            .sortedByDescending { it.percentage }
    }


    private fun fetchLsrwstatsReportData(month: Int) {
        binding.rclsrwheader.visibility = View.VISIBLE
        appViewModel?.islsrwstats(isAccessToken ?: "", month)
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.toolbarLayout.imgBack.id) {
            onBackPressed()
        }
    }
}
