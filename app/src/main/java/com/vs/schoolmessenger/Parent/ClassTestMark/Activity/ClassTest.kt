package com.vs.schoolmessenger.Parent.ClassTestMark.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.ClassTestMark.Adapter.ClassTestAdapter
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestData
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestResponse
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ClasstestViewmarkBinding

class ClassTest : BaseActivity<ClasstestViewmarkBinding>() {

    override fun getViewBinding(): ClasstestViewmarkBinding {
        return ClasstestViewmarkBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private lateinit var classTestAdapter: ClassTestAdapter
    private var classTestList: List<ClassTestData> = emptyList()

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        setupRecyclerView()
        isGetClassTestMark()

        appViewModel?.isClassTestResponsel?.observe(this) { response ->
            binding.progressBar.visibility = View.GONE
            if (response != null && response.status) {
                if (response.data.isNotEmpty()) {
                    classTestList = response.data
                    classTestAdapter.updateList(classTestList)
                    updateHeaderStats(response)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        classTestAdapter = ClassTestAdapter(emptyList()) { selectedExam ->
            val intent = Intent(this, MarksReportActivity::class.java).apply {
                putExtra("EXAM_ID", selectedExam.classTestId)
                putExtra("EXAM_NAME", selectedExam.examName)
            }
            startActivity(intent)
        }

        binding.rvClassTests.apply {
            layoutManager = LinearLayoutManager(this@ClassTest)
            adapter = classTestAdapter
        }
    }

    private fun updateHeaderStats(response: ClassTestResponse) {
        val totalExams = response.data.size
        val totalSubjects = response.data.sumOf { it.subjects.size }
        val totalActivities = response.data.sumOf { exam ->
            exam.subjects.sumOf { it.activities.size }
        }

        binding.tvExamsCount.text = totalExams.toString()
        binding.tvSubjectsCount.text = totalSubjects.toString()
        binding.tvActivitiesCount.text = totalActivities.toString()
    }

    private fun isGetClassTestMark() {
        binding.progressBar.visibility = View.VISIBLE
        appViewModel!!.isClassTestStudent(
            isAccessToken!!,
            this
        )
    }
}