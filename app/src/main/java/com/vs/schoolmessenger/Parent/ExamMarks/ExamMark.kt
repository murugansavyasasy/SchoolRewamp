package com.vs.schoolmessenger.Dashboard.Parent

import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.ExamMarkBinding

class ExamMark : BaseActivity<ExamMarkBinding>() {

    private lateinit var examAdapter: ExamMarkAdapter

    override fun getViewBinding(): ExamMarkBinding {
        return ExamMarkBinding.inflate(layoutInflater)
    }
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        val examList = listOf(
            ExamItem("Half Yearly Exam"),
            ExamItem("Annual Exam"),
            ExamItem("Quarterly Exam"),
            ExamItem("Final Exam"),
            ExamItem("Final Exam"),
            ExamItem("Final Exam"),
            ExamItem("Final Exam")
        )
        // Initialize Adapter
        examAdapter = ExamMarkAdapter(examList)
        // Set up RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = examAdapter
    }
}
