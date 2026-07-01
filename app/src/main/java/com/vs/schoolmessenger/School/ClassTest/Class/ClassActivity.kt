package com.vs.schoolmessenger.School.ClassTest.Class.ClassActivity

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Class.ClassAdapter
import com.vs.schoolmessenger.School.ClassTest.Class.Models.ClassTestItem
import com.vs.schoolmessenger.School.ClassTest.Class.Models.SelectedSubject
import com.vs.schoolmessenger.School.ClassTest.Review.ReviewActivity
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ClassActivityBinding

class ClassActivity : BaseActivity<ClassActivityBinding>() {

    private lateinit var adapter: ClassAdapter

    override fun getViewBinding(): ClassActivityBinding {
        return ClassActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        window.statusBarColor = resources.getColor(R.color.PrimaryColor, theme)
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            binding.statusBarBackground.layoutParams.height =
                resources.getDimensionPixelSize(resourceId)
            binding.statusBarBackground.requestLayout()
        }
        setupStepIndicator()
        loadSubjectData()
        setupContinueButton()
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 4)
    }

    private fun loadSubjectData() {
        val selectedSubjects: List<SelectedSubject> = Constant.isSelectedSubjectss ?: emptyList()
        if (selectedSubjects.isEmpty()) {
            showError("No subjects selected")
            return
        }
        val items = selectedSubjects.map { s ->
            ClassTestItem(
                subjectId = s.subjectId,
                subjectName = s.subjectName,
                sectionId = s.sectionId,
                sectionLabel = "Section ${s.sectionName}",
                isMerged = false,
                mergedSections = emptyList(),
                mergedSectionIds = emptyList()
            )
        }
        buildAdapter(items)
    }

    private fun buildAdapter(items: List<ClassTestItem>) {
        adapter = ClassAdapter(items.toMutableList())
        binding.rcClassList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ClassActivity.adapter
        }
        binding.rcClassList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    private fun setupContinueButton() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnContinue.setOnClickListener {
            if (!::adapter.isInitialized) return@setOnClickListener

            val globalExamName = binding.etExamName.text?.toString()?.trim() ?: ""
            if (globalExamName.isBlank()) {
                binding.etExamName.background = ContextCompat.getDrawable(
                    this, R.drawable.input_field_bg_error
                )
                binding.etExamName.error = "Exam name is required"
                binding.etExamName.requestFocus()
                Toast.makeText(this, "Please enter the Exam Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                binding.etExamName.background = ContextCompat.getDrawable(
                    this, R.drawable.input_field_bg
                )
            }

            val allItems = adapter.getAllItems()

            val touchedItems = allItems.filter { item ->
                item.tests.any { t ->
                    t.examName.isNotBlank() || t.testDate.isNotBlank()
                }
            }

            if (touchedItems.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill in at least one subject's test details",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val hasIncomplete = touchedItems.any { item ->
                item.tests.any { t ->
                    t.examName.isBlank() || t.testDate.isBlank()
                }
            }

            if (hasIncomplete) {
                Toast.makeText(
                    this,
                    "Please fill Activity Name and Test Date for every test",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Constant.isExamName = globalExamName
            Constant.isClassTestItems = touchedItems
            startActivity(Intent(this, ReviewActivity::class.java))
        }

        binding.etExamName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrBlank()) {
                    binding.etExamName.background = ContextCompat.getDrawable(
                        this@ClassActivity, R.drawable.input_field_bg
                    )
                }
            }
        })
    }

    private fun showError(message: String) {
        binding.rcClassList.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
    }
}