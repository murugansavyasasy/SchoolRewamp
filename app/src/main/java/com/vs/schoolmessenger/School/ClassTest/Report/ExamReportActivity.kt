package com.vs.schoolmessenger.School.ClassTest.Report

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.Report.Model.ExamlistModel
import com.vs.schoolmessenger.School.ClassTest.Report.Model.SectionModeldata
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamReportListBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExamReportActivity : BaseActivity<ExamReportListBinding>() {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    private var isStaffDetails: StaffDetails? = null

    private lateinit var adapter: ExamReportAdapter
    private val examListData = mutableListOf<ExamlistModel>()

    private var selectedExamDate: String = "0"

    private var hasUserInteracted = false

    private val defaultClassTestId = "0"

    private var pendingDeleteId: String? = null
    private val apiDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    override fun getViewBinding(): ExamReportListBinding {
        return ExamReportListBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = "Exams"
        binding.toolbarLayout.lblExamCount.visibility = View.VISIBLE
        setupRecyclerView()
        setupViewModel()
        setupDateFilter()


        appViewModel!!.isputExamDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    val index = examListData.indexOfFirst { it.classTestId == pendingDeleteId }
                    showMessageDialog(response.message ?: "Deleted successfully") {
                        if (index != -1) {
                            adapter.removeAt(index)
                            updateExamCountBadge(examListData.size)
                        }
                    }
                } else {
                    showMessageDialog(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
                pendingDeleteId = null
            }
        }

    }

    private fun updateExamCountBadge(count: Int) {
        binding.toolbarLayout.lblExamCount.text =
            "$count ${if (count == 1) "EXAM" else "EXAMS"}"
    }


    private fun setupRecyclerView() {
        adapter = ExamReportAdapter(
            list = examListData,
            onSectionClick = { classTest, section ->
                openMarksEntry(classTest, section)
            },
            onDeleteClick = { classTest, position ->
               deleteClassTest(classTest, position)
            }
        )
        binding.rcyexamlist.layoutManager = LinearLayoutManager(this)
        binding.rcyexamlist.adapter = adapter
    }

    private fun deleteClassTest(classTest: ExamlistModel, position: Int) {
        pendingDeleteId = classTest.classTestId

        val jsonObject = JsonObject().apply {
            addProperty("class_test_id", classTest.classTestId)
        }

        appViewModel!!.isputExamDelete(
            isToken = isAccessToken ?: "",
            jsonObject = jsonObject,
            this
        )
    }

    private fun openMarksEntry(classTest: ExamlistModel, section: SectionModeldata) {
        Constant.isExamName = classTest.examName
        Constant.isSelectedClassTestId = classTest.classTestId
        Constant.isSelectedSectionId = section.sectionId
        Constant.isSelectedSectionName = section.sectionName
        Constant.isExamReportSubjects = section.subjects

        startActivity(Intent(this, ExamMarksEnterActivity::class.java))
    }

    private fun setupViewModel() {
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)

        if (!isAcademicYear.isNullOrEmpty()) {
            isAcademicYearId = isAcademicYear!![0].id
            isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
            Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
        }

        appViewModel!!.isgetExamreportdetails?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    showData(response.data)
                } else {
                    showError(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        }
        fetchExamReport()
    }


    private fun showMessageDialog(message: String, onOkClick: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onOkClick?.invoke()
            }
            .show()
    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val spinnerAdapter = ClassTestAcademicYearAdapter(this, isAcademicYear)
        binding.academicyear.adapter = spinnerAdapter

        binding.academicyear.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    (view as? TextView)?.setTextColor(Color.BLACK)
                    spinnerAdapter.selectedPosition = position

                    val selected = isAcademicYear?.getOrNull(position) ?: return
                    isAcademicYearId = selected.id
                    isCurrentAcademicYear = selected.current_academic_year
                    Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()

                    Log.d(
                        "DropdownMenu",
                        "Year: ID=${selected.id}, Year=${selected.year}, Current=${selected.current_academic_year}"
                    )

                    if (position != 0 || hasUserInteracted) {
                        fetchExamReport()
                    }
                    hasUserInteracted = true
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }
    private fun setupDateFilter() {
        binding.rytDateFilter.setOnClickListener { openDatePicker() }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                selectedExamDate = apiDateFormat.format(calendar.time)

                binding.txtDateLabel.text = getString(R.string.selected)
                binding.txtSelectedDate.text = displayDateFormat.format(calendar.time)

                fetchExamReport()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fetchExamReport() {
        appViewModel!!.isgetExamreportdetails(
            isToken = isAccessToken ?: "",
            class_test_id = defaultClassTestId,
            exam_date = selectedExamDate,
            academic_year_id = isAcademicYearId.toString(),
            this
        )
    }

    private fun showData(data: List<ExamlistModel>) {
        binding.rcyexamlist.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
        adapter.updateList(data)
        updateExamCountBadge(data.size)
    }

    private fun showError(message: String) {
        binding.rcyexamlist.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.visibility = View.VISIBLE
        binding.txtNoData.text = message
        adapter.updateList(emptyList())
        updateExamCountBadge(0)
    }
}