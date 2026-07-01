package com.vs.schoolmessenger.Parent.ClassTestMark.Activity

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.ClassTestMark.Adapter.MarksReportAdapter
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.MarkData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference

class MarksReportActivity : BaseActivity<ActivityMarksReportBinding>() {

    override fun getViewBinding(): ActivityMarksReportBinding {
        return ActivityMarksReportBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var examId: String? = null
    private var examName: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        examId = intent.getStringExtra("EXAM_ID")
        examName = intent.getStringExtra("EXAM_NAME")

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.tvReportTitle.text = "$examName — Marks"

        setupRecyclerView()
        getMarksReport()

        appViewModel?.isViewClassTestResponsel?.observe(this) { response ->
            if (response != null && response.status) {
                if (response.data.isNotEmpty()) {
                    displayMarksData(response.data[0])
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvMarkSubjects.apply {
            layoutManager = LinearLayoutManager(this@MarksReportActivity)
            adapter = MarksReportAdapter(emptyList())
        }
    }

    private fun displayMarksData(data: MarkData) {
        // Header info
        binding.tvExamName.text = data.examName
        val totalSubjects = data.subjects.size
        val totalAssessed = data.subjects.sumOf { it.activities.size }
        binding.tvExamDetail.text = "$totalSubjects ${if (totalSubjects == 1) "subject" else "subjects"} · $totalAssessed assessed"

        binding.tvOverallPercentage.text = data.overallPercentage
        binding.tvScored.text = data.overallStudentMarks
        binding.tvTotal.text = data.overallMarks
        binding.tvPercentage.text = data.overallPercentage

        // Setup subjects RecyclerView
        binding.rvMarkSubjects.adapter = MarksReportAdapter(data.subjects)
    }

    private fun getMarksReport() {
        appViewModel!!.isViewClassTestStudent(
            isAccessToken!!,
            examId!!,
            this
        )
    }
}