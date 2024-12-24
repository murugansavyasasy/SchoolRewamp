package com.vs.schoolmessenger.School.ExamSchedule

import android.content.Intent
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ExamBinding


class Exam : BaseActivity<ExamBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ExamBinding {
        return ExamBinding.inflate(layoutInflater)
    }

    private val itemsSection = listOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
    )
    private val itemsStandard = listOf(
        "V",
        "VI",
        "VII",
        "VIII",
        "IX",
        "X",
        "XI",
        "XII"
    )


    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.rlaStandard.setOnClickListener(this)
        binding.rlaSection.setOnClickListener(this)
        binding.btnSelectExam.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaStandard -> {
                showDropdownMenuSort(
                    binding.lblStandard,
                    this,
                    itemsStandard
                ) { selectedOption ->
                    binding.lblStandard.text = selectedOption
                }
            }

            R.id.rlaSection -> {
                showDropdownMenuSort(
                    binding.lblSection,
                    this,
                    itemsSection
                ) { selectedOption ->
                    binding.lblSection.text = selectedOption
                }
            }

            R.id.btnSelectExam -> {
                startActivity(Intent(this, ExamSubjectList::class.java))

            }
        }
    }
}