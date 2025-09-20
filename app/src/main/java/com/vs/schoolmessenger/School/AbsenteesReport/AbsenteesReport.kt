package com.vs.schoolmessenger.School.AbsenteesReport

import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.CustomCalendarFragement.CustomCalendarFragment
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesStudentListDetailAdapter
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
import java.util.Date
import java.util.Locale


class AbsenteesReport : BaseActivity<AbsenteesReportBinding>(), View.OnClickListener,
    AbsenteesClickListener, AbsenteesStudentDetailClickListener, CustomCalendarFragment.CalendarDateListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var absenteeList: List<AbsenteeData> = emptyList()
    private var studentAdapter: AbsenteesStudentListDetailAdapter? = null

    override fun getViewBinding(): AbsenteesReportBinding {
        return AbsenteesReportBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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


        val calendarFragment = CustomCalendarFragment.newInstance(
            minDate = "2020-01-01", // Set appropriate min date
            maxDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            selectedDate = null,
            tag = "absentees_calendar"
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.calenderlayout, calendarFragment, "CustomCalendarFragment")
            .commit()

        fetchAbsenteeData()

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

        appViewModel?.getabsenteesstudentbydate?.observe(this) { response ->
            if (response == null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                return@observe
            }
            if (response.status) {
                val studentList = response.data ?: emptyList()
                bindStudentList(studentList)
            } else {
                Toast.makeText(this, response.message ?: "No students found", Toast.LENGTH_SHORT)
                    .show()
                bindStudentList(emptyList())
            }
        }
    }

    private fun fetchAbsenteeData() {
        appViewModel?.getabsenteescountbydate(
            isAccessToken ?: "", this
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
            loadClassWiseRecycler(filtered.class_wise, date)
        } else {
            binding.rlaabsenteesreport2.visibility = View.GONE
            binding.lytNoDataFound.visibility = View.VISIBLE
            binding.selectedDateText.visibility = View.GONE
            binding.linearLayoutcontainer.visibility = View.GONE
            binding.absentListTitle.visibility = View.GONE
            binding.absentStudentsRecyclerView.visibility = View.GONE
        }
    }

    private fun loadClassWiseRecycler(classWiseList: List<ClassWise>, selectedDate: String) {
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

        val adapter =
            AbsenteesReportDetailAdapter(flatList, selectedDate, object : OnAbsenteeClickListener {
                override fun onAbsenteeClicked(absentOn: String, sectionId: String) {
                    showStudentShimmer()
                    appViewModel?.getabsenteesstudentbydate(
                        isAccessToken ?: "", absentOn, sectionId, this@AbsenteesReport
                    )
                }
            })
        binding.rlaabsenteesreport2.adapter = adapter

        if (flatList.isNotEmpty()) {
            val firstSection = flatList[0].second
            showStudentShimmer()
            appViewModel?.getabsenteesstudentbydate(
                isAccessToken ?: "", selectedDate, firstSection.section_id, this
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
        val output = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return try {
            output.format(input.parse(date)!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun showErrorUI(message: String) {
        binding.rlaabsenteesreport2.visibility = View.GONE
        binding.lytNoDataFound.visibility = View.VISIBLE
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
        Toast.makeText(this, "Clicked: ${data.student_name}", Toast.LENGTH_SHORT).show()
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            Toast.makeText(this, "No students found", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
}