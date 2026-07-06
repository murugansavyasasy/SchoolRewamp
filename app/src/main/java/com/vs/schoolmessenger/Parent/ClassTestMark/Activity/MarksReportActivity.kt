package com.vs.schoolmessenger.Parent.ClassTestMark.Activity

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.ClassTestMark.Adapter.MarksReportAdapter
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.MarkData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MarkReportBinding

class MarksReportActivity : BaseActivity<MarkReportBinding>() {

    override fun getViewBinding(): MarkReportBinding {
        return MarkReportBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var examId: String? = null
    private var examName: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()

        examId = intent.getStringExtra("EXAM_ID")
        examName = intent.getStringExtra("EXAM_NAME")

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        setupRecyclerView()
        getMarksReport()

        appViewModel?.isViewClassTestResponsel?.observe(this) { response ->

            if (response != null && response.status) {

                if (response.data.isNotEmpty()) {
                    binding.rvMarkSubjects.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE

                    displayMarksData(response.data[0])

                } else {
                    binding.rvMarkSubjects.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = response.message
                }

            } else {
                binding.rvMarkSubjects.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message
            }
        }
        binding.isBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.rvMarkSubjects.apply {
            layoutManager = LinearLayoutManager(this@MarksReportActivity)
            adapter = MarksReportAdapter(emptyList())
        }
    }

    private fun displayMarksData(data: MarkData) {
        binding.tvScored.text = data.overallStudentMarks
        binding.tvTotal.text = data.overallMarks
        binding.tvPercentage.text = data.overallPercentage

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