package com.vs.schoolmessenger.School.AbsenteesReport

import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesStudentListDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesCalendarListener
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesStudentDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.OnAbsenteeClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.AbsenteesReport.Model.SectionWise
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class AbsenteesReport : BaseActivity<AbsenteesReportBinding>(), View.OnClickListener,
    AbsenteesClickListener, AbsenteesStudentDetailClickListener,
    AbsenteesCalendarListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var absenteeList: List<AbsenteeData> = emptyList()
    private var studentAdapter: AbsenteesStudentListDetailAdapter? = null
    private var errorMessage: String? = ""
    private var currentMonth: Int = YearMonth.now().monthValue
    private var currentYear: Int = YearMonth.now().year

    override fun getViewBinding(): AbsenteesReportBinding {
        return AbsenteesReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name ?: ""
        isAccessToken = isStaffDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        val calendarFragment = CustomAbsenteesCalendarFragment.newInstance(
            minDate = "2020-01-01",
            maxDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            selectedDate = null,
            tag = "absentees_calendar"
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.calenderlayout, calendarFragment, "CustomCalendarFragment")
            .commit()
        // Remove initial fetch here; let the fragment's initial onMonthChanged handle it
        appViewModel?.getabsenteescountbydate?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }
            if (response.status) {
                absenteeList = response.data ?: emptyList()
                updateCalendarWithAbsentDates()
                setDefaultDateForMonth()
            } else {
                showErrorUI(response.message ?: getString(R.string.no_data_available))
            }
        }
        appViewModel?.getabsenteesstudentbydate?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response == null) {
                Toast.makeText(
                    this,
                    getString(R.string.something_went_wrong_please_try_again_later),
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            val mobileNumber = SharedPreference.getMobileNumber(this)
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.mobile_number, mobileNumber)
                addProperty(APIKeyNames.activity, Constant.add_points_abesntees_report)
                addProperty(APIKeyNames.user_type, Constant.user_type_as_staff)
                addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
            }
            appViewModel?.isAddRewardPoints("" ?: "", jsonObject,this)
            if (response.status) {
                val studentList = response.data ?: emptyList()
                bindStudentList(studentList)
            } else {
                errorMessage = response.message
                Toast.makeText(
                    this,
                    response.message ?: getString(R.string.no_student_found),
                    Toast.LENGTH_SHORT
                )
                    .show()
                bindStudentList(emptyList())
            }
        }
    }

    private fun fetchAbsenteeData(month: Int = YearMonth.now().monthValue, year: Int = YearMonth.now().year) {
        Constant.showLoading(this)
        appViewModel?.getabsenteescountbydate(
            isAccessToken ?: "",
            month,
            year,
            this
        )
    }

    private fun updateCalendarWithAbsentDates() {
        val fragment =
            supportFragmentManager.findFragmentByTag("CustomCalendarFragment") as? CustomAbsenteesCalendarFragment
        fragment?.let {
            val absentDates = absenteeList.mapNotNull { data ->
                try {
                    LocalDate.parse(
                        data.absent_date_only,
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            it.setAbsentDates(absentDates)
        }
    }

    private fun setDefaultDateForMonth() {
        val now = YearMonth.now()
        val isCurrentMonth = currentMonth == now.monthValue && currentYear == now.year
        if (isCurrentMonth && absenteeList.isNotEmpty()) {
            // For current month only, default to today if available, else earliest
            val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            if (absenteeList.any { it.absent_date_only == today }) {
                filterByDate(today)
            } else {
                val earliestDate = absenteeList.minByOrNull { it.absent_date_only }?.absent_date_only
                earliestDate?.let { filterByDate(it) }
            }
        } else {
            // For non-current months (or empty data), hide UI until date is selected
            showNoDataUI()
        }
    }

    private fun showNoDataUI() {
        binding.linearLayoutcontainer.visibility = View.GONE
        binding.rlaabsenteesreport2.visibility = View.GONE
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.selectedDateText.visibility = View.GONE
        binding.absentListTitle.visibility = View.GONE
        binding.absentStudentsRecyclerView.visibility = View.GONE
        // Optionally set a generic no-data message if needed
        // binding.noDataFound.text = getString(R.string.no_data_available)
    }

    private fun filterByDate(date: String) {
        val filtered = absenteeList.find { it.absent_date_only == date }
        if (filtered != null) {
            binding.selectedDateText.text = formatDateDisplay(date)
            binding.selectedDateText.visibility = View.VISIBLE
            binding.linearLayoutcontainer.visibility = View.VISIBLE
            binding.rlaabsenteesreport2.visibility = View.VISIBLE
            binding.lytNoDataFound.visibility = View.GONE
            binding.absentListTitle.visibility = View.VISIBLE
            binding.absentStudentsRecyclerView.visibility = View.VISIBLE

            val text = "Total Absent : ${filtered.total_absentees}"
            val span = SpannableString(text)
            span.setSpan(
                ForegroundColorSpan(Color.RED),
                text.indexOf(filtered.total_absentees),
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.totalabsentesscount.text = span
            binding.absenteesbystandard.text = "${"Absentees by standard"} : ${filtered.class_wise[0].total_absentees}"
            loadClassWiseRecycler(filtered.class_wise, date)
        } else {
            showNoDataUI()
        }
    }

    private fun loadClassWiseRecycler(classWiseList: List<ClassWise>, selectedDate: String) {
        binding.linearLayoutcontainer.visibility = View.VISIBLE
        binding.rlaabsenteesreport2.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
        binding.selectedDateText.visibility = View.VISIBLE
        binding.linearLayoutcontainer.visibility = View.VISIBLE
        binding.absentListTitle.visibility = View.VISIBLE
        binding.absentStudentsRecyclerView.visibility = View.VISIBLE
        binding.rlaabsenteesreport2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val flatList = mutableListOf<Pair<ClassWise, SectionWise>>()
        classWiseList.forEach { classWise ->
            classWise.section_wise.forEach { section ->
                flatList.add(classWise to section)
            }
        }
        val adapter = AbsenteesReportDetailAdapter(
            flatList,
            selectedDate,
            object : OnAbsenteeClickListener {
                override fun onAbsenteeClicked(
                    absentOn: String,
                    classId: String,
                    sectionId: String,
                    classname: String,
                    sectionname: String,
                    student_counts: String,
                    absent: String,
                    totalabsenteesclasswise: String,
                    total: String
                ) {
                    showStudentShimmer()
                    binding.absenteesbystandard.text = "${"Absentees by standard"} : $totalabsenteesclasswise"
                    // Update basic info
                    binding.absenteecount.text = "${getString(R.string.Absentees)} : $absent"
                    binding.totalstudentscount.text =
                        "${getString(R.string.total_students)} : $student_counts"
                    binding.classDetailname.text = "$classname - $sectionname"
                    // Safely parse to Int
                    val totalCount = total.toIntOrNull() ?: 0
                    val absentCount = absent.toIntOrNull() ?: 0
                    binding.progressAbsent.max = if (totalCount > 0) totalCount else 1
                    binding.progressAbsent.progress = absentCount.coerceAtMost(totalCount)
                    Constant.showLoading(this@AbsenteesReport)
                    appViewModel?.getabsenteesstudentbydate(
                        isAccessToken ?: "",
                        absentOn,
                        classId,
                        sectionId,
                        this@AbsenteesReport
                    )
                }
            }
        )
        binding.rlaabsenteesreport2.adapter = adapter
        if (flatList.isNotEmpty()) {
            val (classWise, sectionWise) = flatList[0]
            val absent = sectionWise.total_absentees.toIntOrNull() ?: 0
            val total = sectionWise.student_counts.toIntOrNull() ?: 1
            val sectiontotal = sectionWise.student_counts.toIntOrNull() ?: 1
            classWise.total_absentees.toIntOrNull() ?: 1
            // Set initial UI values for the first item
            binding.absenteecount.text = "${getString(R.string.Absentees)} : $absent"
            binding.totalstudentscount.text =
                "${getString(R.string.total_students)} : $sectiontotal"
            binding.classDetailname.text = "${classWise.class_name} - ${sectionWise.section_name}"
            binding.progressAbsent.max = total
            binding.progressAbsent.progress = absent
            showStudentShimmer()
            appViewModel?.getabsenteesstudentbydate(
                isAccessToken ?: "", selectedDate, classWise.class_id, sectionWise.section_id, this
            )
        }
    }

    private fun bindStudentList(studentList: List<Student>) {
        if (studentAdapter == null) {
            studentAdapter = AbsenteesStudentListDetailAdapter(
                studentList, this, this, false
            )
            binding.absentStudentsRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.absentStudentsRecyclerView.adapter = studentAdapter
        } else {
            studentAdapter?.updateData(studentList)
        }
    }

    private fun showStudentShimmer() {
        studentAdapter = AbsenteesStudentListDetailAdapter(
            listOf(), this, this, true
        )
        binding.absentStudentsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.absentStudentsRecyclerView.adapter = studentAdapter
    }

    private fun formatDateDisplay(date: String): String {
        val input = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val output = SimpleDateFormat("EEE MMM dd, yyyy", Locale.getDefault())
        return try {
            output.format(input.parse(date)!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun showErrorUI(message: String) {
        binding.linearLayoutcontainer.visibility = View.GONE
        binding.rlaabsenteesreport2.visibility = View.GONE
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = message
        binding.selectedDateText.visibility = View.GONE
        binding.linearLayoutcontainer.visibility = View.GONE
        binding.absentListTitle.visibility = View.GONE
        binding.absentStudentsRecyclerView.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onDateSelected(data: AbsenteeData) {
        filterByDate(data.absent_date_only)
    }

    override fun onFooterItemClicked(position: Int, data: Student) {
// Toast.makeText(this, "Clicked: ${data.student_name}", Toast.LENGTH_SHORT).show()
        Log.d("Profile Clicked", "Profile Clicked response checked")
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            Toast.makeText(this, getString(R.string.no_student_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDateSelected(date: String, tag: String) {
        try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = output.format(input.parse(date)!!)
            filterByDate(formattedDate)
        } catch (e: Exception) {
            filterByDate(date)
        }
    }

    override fun onMonthChanged(month: Int, year: Int) {
        currentMonth = month
        currentYear = year
        // Immediately hide previous data UI on month change
        showNoDataUI()
        // Then fetch new data for the selected month/year
        fetchAbsenteeData(month, year)
    }
}