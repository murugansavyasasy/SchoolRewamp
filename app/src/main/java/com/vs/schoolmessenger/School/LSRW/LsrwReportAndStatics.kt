package com.vs.schoolmessenger.School.LSRW

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwHeaderAdapter
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
import com.vs.schoolmessenger.databinding.LsrwSkillMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
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


        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)


        fetchLsrwstatsReportData()

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
                binding.rvTopPerformance.layoutManager = LinearLayoutManager(this)
                binding.rvTopPerformance.adapter = TopPerformanceAdapter(topPerformers)

                binding.rclsrwheader.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE
            } else {
                binding.rclsrwheader.visibility = View.GONE
                binding.noDataFound.visibility = View.VISIBLE
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
            else -> emptyList()
        }

        val weeklyReport = calculateWeeklyReport(details)
        binding.rvWeekly.adapter = WeeklyReportAdapter(weeklyReport)

        val topPerformers = calculateTopPerformers(details)
        binding.rvTopPerformance.adapter = TopPerformanceAdapter(topPerformers)
    }





    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateWeeklyReport(details: List<AvgStudentSubmission>): List<WeeklyReportItem> {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
        val weeks = mutableMapOf<Int, MutableList<Int>>()

        details.forEach { detail ->
            val date = LocalDate.parse(detail.submission_date, formatter)
            val weekOfMonth = date.get(WeekFields.of(Locale.getDefault()).weekOfMonth())

            val remarkValue = detail.remark.replace("%", "").toIntOrNull() ?: 0
            weeks.getOrPut(weekOfMonth) { mutableListOf() }.add(remarkValue)
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
        }.sortedByDescending { it.percentage }
    }




    private fun fetchLsrwstatsReportData() {
        binding.rclsrwheader.visibility = View.VISIBLE
        appViewModel?.islsrwstats(isAccessToken ?: "", 8)

    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}