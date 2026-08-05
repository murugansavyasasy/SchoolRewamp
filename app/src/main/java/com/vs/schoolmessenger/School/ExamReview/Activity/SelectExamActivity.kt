package com.vs.schoolmessenger.School.ExamReview.Activity

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamReview.Adapter.SelectExamAdapter
import com.vs.schoolmessenger.School.ExamReview.Model.ExamSeries
import com.vs.schoolmessenger.databinding.ExamStandardSelectBinding
import com.vs.schoolmessenger.databinding.SelectExamReveiwBinding

class SelectExamActivity : BaseActivity<SelectExamReveiwBinding>(), View.OnClickListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    private var isStaffDetails: StaffDetails? = null
    private var userDetails: UserDetails? = null

    private lateinit var examAdapter: SelectExamAdapter

    private val preSelectedExamIds = setOf("1", "2", "3")

    override fun getViewBinding(): SelectExamReveiwBinding =
        SelectExamReveiwBinding.inflate(layoutInflater)

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnChangeStudent.setOnClickListener(this)
        binding.btnViewAnalysis.setOnClickListener(this)
        binding.btnSelectAll.setOnClickListener(this)

        loadHardcodedStudent()
        setupExamList()
    }

    private fun loadHardcodedStudent() {
        val studentName = "JEEVA"
        val rollNo = "--"
        val classSection = "Class 6 - Section A"

        binding.lblStudentNameValue.text = studentName
        binding.lblStudentAvatarLetter.text = studentName.take(2).uppercase()
        binding.lblStudentDetail.text = "Roll No: $rollNo \u00B7 $classSection"
    }

    private fun loadHardcodedExamSeries(): List<ExamSeries> = listOf(
        ExamSeries(id = "1", title = "DRT - 1", seriesLabel = "Series 1", maxMarks = 100),
        ExamSeries(id = "2", title = "DRT - 2", seriesLabel = "Series 2", maxMarks = 100),
        ExamSeries(id = "3", title = "DRT - 3", seriesLabel = "Series 3", maxMarks = 100),
        ExamSeries(id = "4", title = "DRT - 4", seriesLabel = "Series 4", maxMarks = 100),
        ExamSeries(id = "5", title = "DRT - 5", seriesLabel = "Series 5", maxMarks = 100)
    )

    private fun setupExamList() {
        val examList = loadHardcodedExamSeries()

        examAdapter = SelectExamAdapter(
            itemList = examList,
            initiallySelectedIds = preSelectedExamIds
        ) { selectedCount ->
            updateSelectionCount(selectedCount)
        }

        binding.rcExamList.apply {
            layoutManager = LinearLayoutManager(this@SelectExamActivity)
            adapter = examAdapter
        }

        updateSelectionCount(preSelectedExamIds.size)
    }

    private fun updateSelectionCount(count: Int) {
        binding.lblSelectionCount.text = when (count) {
            0 -> "0 exams selected"
            1 -> "1 exam selected"
            else -> "$count exams selected"
        }
        binding.btnViewAnalysis.isEnabled = count > 0
        binding.btnViewAnalysis.alpha = if (count > 0) 1f else 0.5f
        binding.btnSelectAll.text = if (examAdapter.isAllSelected()) "Deselect all" else "Select all"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.toolbarLayout.imgBack.id, binding.btnBack.id -> onBackPressed()

            binding.btnChangeStudent.id -> {
                onBackPressed()
            }

            binding.btnSelectAll.id -> {
                val shouldSelectAll = !examAdapter.isAllSelected()
                examAdapter.selectAll(shouldSelectAll)
                updateSelectionCount(examAdapter.getSelectedItems().size)
            }

            binding.btnViewAnalysis.id -> {
                val selectedExams = examAdapter.getSelectedItems()
                if (selectedExams.isEmpty()) return
            }
        }
    }
}