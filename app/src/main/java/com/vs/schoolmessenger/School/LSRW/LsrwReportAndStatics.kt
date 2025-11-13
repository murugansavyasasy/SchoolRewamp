package com.vs.schoolmessenger.School.LSRW

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
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
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails?.access_token

        binding.toolbarLayout.lblParentToolBar.text =
            getString(com.vs.schoolmessenger.R.string.report_analytics)
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name
        binding.toolbarLayout.monthSelectorLayout.visibility = View.VISIBLE
        binding.toolbarLayout.imgBack.setOnClickListener(this)


        setupMonthSpinner()


        appViewModel?.islsrwstats?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]


                val headerItems = mutableListOf<LsrwHeaderItem>().apply {
                    add(
                        LsrwHeaderItem(
                            Constant.Today_Submitted,
                            "",
                            "${data.today_submitted?.size ?: 0} ${getString(R.string.Students)}"
                        )
                    )
                    add(
                        LsrwHeaderItem(
                            Constant.Listening,
                            data.listening?.over_all_percentage ?: "0%",
                            "${data.listening?.student_count ?: "0"} ${getString(R.string.Students)}"
                        )
                    )
                    add(
                        LsrwHeaderItem(
                            Constant.Speaking,
                            data.speaking?.over_all_percentage ?: "0%",
                            "${data.speaking?.student_count ?: "0"} ${getString(R.string.Students)}"
                        )
                    )
                    add(
                        LsrwHeaderItem(
                            Constant.Reading,
                            data.reading?.over_all_percentage ?: "0%",
                            "${data.reading?.student_count ?: "0"} ${getString(R.string.Students)}"
                        )
                    )
                    add(
                        LsrwHeaderItem(
                            Constant.Writing,
                            data.writing?.over_all_percentage ?: "0%",
                            "${data.writing?.student_count ?: "0"} ${getString(R.string.Students)}"
                        )
                    )
                }


                binding.rclsrwheader.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                val headerAdapter = LsrwHeaderAdapter(headerItems) { selected ->
                    filterByHeader(selected, data)
                }
                binding.rclsrwheader.adapter = headerAdapter


                val allDetails = mutableListOf<AvgStudentSubmission>().apply {
                    addAll(data.listening?.details ?: emptyList())
                    addAll(data.reading?.details ?: emptyList())
                    addAll(data.speaking?.details ?: emptyList())
                    addAll(data.writing?.details ?: emptyList())
                }


                val weeklyReport = calculateWeeklyReport(allDetails)
                binding.rvWeekly.layoutManager = LinearLayoutManager(this)
                binding.rvWeekly.adapter = WeeklyReportAdapter(weeklyReport)


                val topPerformers = calculateTopPerformers(allDetails)
                if (topPerformers.isNotEmpty()) {
                    binding.rvTopPerformance.visibility = View.VISIBLE
                    binding.topperformanceLabel.visibility = View.VISIBLE
                    binding.rvTopPerformance.layoutManager = LinearLayoutManager(this)
                    binding.rvTopPerformance.adapter = TopPerformanceAdapter(topPerformers)
                } else {
                    binding.rvTopPerformance.visibility = View.GONE
                    binding.topperformanceLabel.visibility = View.GONE
                }


                binding.apply {
                    rclsrwheader.visibility = View.VISIBLE
                    topperformanceCardview.visibility = View.VISIBLE
                    monthlyReportcardview.visibility = View.VISIBLE
                    rvWeekly.visibility = View.VISIBLE
                    weeklyreportLabel.visibility = View.VISIBLE
                    monthlyLabel.visibility = View.VISIBLE
                    lytNoDataFound.visibility = View.GONE
                }


                if (headerItems.isNotEmpty()) {
                    filterByHeader(headerItems[0], data)
                }

            } else {
                binding.apply {
                    rclsrwheader.visibility = View.GONE
                    rvWeekly.visibility = View.GONE
                    weeklyreportLabel.visibility = View.GONE
                    monthlyLabel.visibility = View.GONE
                    studentsLabel.visibility = View.GONE
                    rcstudents.visibility = View.GONE
                    rvTopPerformance.visibility = View.GONE
                    topperformanceCardview.visibility = View.GONE
                    monthlyReportcardview.visibility = View.GONE
                    topperformanceLabel.visibility = View.GONE


                    lytNoDataFound.visibility = View.VISIBLE
                    noDataFound.text = response?.message ?: getString(R.string.no_data_found)
                }
            }
        }


    }

    private fun setupMonthSpinner() {
        val months = DateFormatSymbols().months
        val monthList = months.take(12)

        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, monthList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView

                view.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                view.textSize = 16f
                return view
            }

            override fun getDropDownView(
                position: Int, convertView: View?, parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                view.textSize = 16f
                view.setPadding(24, 20, 24, 20)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.toolbarLayout.lblDropDownMonth.adapter = adapter

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        binding.toolbarLayout.lblDropDownMonth.setSelection(currentMonthIndex)

        binding.toolbarLayout.lblDropDownMonth.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    selectedMonth = position + 1
                    fetchLsrwstatsReportData(selectedMonth)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // Initial fetch for current month
        fetchLsrwstatsReportData(selectedMonth)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterByHeader(selected: LsrwHeaderItem, data: AvgSkillData) {
        val details = when (selected.title) {
            Constant.Listening -> data.listening?.details ?: emptyList()
            Constant.Speaking -> data.speaking?.details ?: emptyList()
            Constant.Reading -> data.reading?.details ?: emptyList()
            Constant.Writing -> data.writing?.details ?: emptyList()
            Constant.Today_Submitted -> data.today_submitted ?: emptyList()
            else -> emptyList()
        }

        val uniqueDetails = details.groupBy { it.id }.map { entry -> entry.value.first() }

        binding.rcstudents.layoutManager = LinearLayoutManager(this)
        binding.rcstudents.adapter = StudentListAdapter(uniqueDetails, this)

        if (uniqueDetails.isEmpty()) {
            binding.studentsLabel.visibility = View.GONE
            binding.rcstudents.visibility = View.GONE
        } else {
            binding.studentsLabel.visibility = View.VISIBLE
            binding.rcstudents.visibility = View.VISIBLE
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateWeeklyReport(details: List<AvgStudentSubmission>): List<WeeklyReportItem> {


        val formatter = DateTimeFormatter.ofPattern(Constant.ddMMyyyy, Locale.getDefault())
        val currentYear = LocalDate.now().year

        // Get unique activities by id to avoid double-counting
        val uniqueActivities = details.groupBy { it.id }.mapValues { entry -> entry.value.first() }.values.toList()

        val sums = mutableMapOf<Int, Pair<Int, Int>>() // week -> (sum_submitted, sum_member)

        uniqueActivities.forEach { activity ->
            val date = LocalDate.parse(activity.student_submited_on, formatter)
            if (date.monthValue == selectedMonth && date.year == currentYear) {
                val weekOfMonth = date.get(WeekFields.of(Locale.getDefault()).weekOfMonth())
                val current = sums.getOrDefault(weekOfMonth, 0 to 0)
                val submitted = activity.submitted_count ?: 0
                val members = activity.member_count ?: 0
                sums[weekOfMonth] = (current.first + submitted) to (current.second + members)
            }
        }

        val weeklyReport = mutableListOf<WeeklyReportItem>()
        for (week in 1..6) {
            val (sub, mem) = sums.getOrDefault(week, 0 to 0)
            val avg = if (mem > 0) ((sub.toDouble() / mem) * 100).toInt() else 0
            weeklyReport.add(WeeklyReportItem("${Constant.Week} $week", avg))
        }
        return weeklyReport
    }

    private fun calculateTopPerformers(details: List<AvgStudentSubmission>): List<TopPerformanceItem> {
        return details.map {
            TopPerformanceItem(
                studentName = it.student_name,
                className = "${Constant.Class} ${it.std_sec}",
                percentage = it.remark.replace("%", "").toIntOrNull() ?: 0
            )
        }.filter { it.percentage > 0 }.sortedByDescending { it.percentage }
    }

    private fun fetchLsrwstatsReportData(month: Int) {
        Constant.showLoading(this)
        binding.rclsrwheader.visibility = View.VISIBLE
        appViewModel?.islsrwstats(isAccessToken ?: "", month)
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.toolbarLayout.imgBack.id) {
            onBackPressed()
        }
    }
}