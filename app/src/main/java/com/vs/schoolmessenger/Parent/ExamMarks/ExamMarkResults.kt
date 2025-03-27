package com.vs.schoolmessenger.Parent.ExamMarks

import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.ExamMarkDetailBinding

class ExamMarkResults : BaseActivity<ExamMarkDetailBinding>() {

    private lateinit var adapter: ExamMarkResultsAdapter
    private val exammarklist = mutableListOf<ExamMarkDataModel>()

    override fun getViewBinding(): ExamMarkDetailBinding {
        return ExamMarkDetailBinding.inflate(layoutInflater)
    }
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        setupRecyclerView()
        loadHardcodedData()

    }


    private fun setupRecyclerView() {
        adapter = ExamMarkResultsAdapter(exammarklist, object : ExamMarkListener {
            override fun onItemClick(
                data: ExamMarkDataModel,
                holder: ExamMarkResultsAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.ExamMarkRV.layoutManager = LinearLayoutManager(this)
        binding.ExamMarkRV.adapter = adapter
    }

    private fun  loadHardcodedData() {
        exammarklist.apply {
            add(ExamMarkDataModel("Tamil","85/100"))
            add(ExamMarkDataModel("English","85/100"))
            add(ExamMarkDataModel("Maths","85/100"))
            add(ExamMarkDataModel("Science","85/100"))
            add(ExamMarkDataModel("Social Science","85/100"))
        }
    }

}

