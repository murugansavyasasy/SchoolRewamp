package com.vs.schoolmessenger.School.ClassTest.Section

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.School.ClassTest.Subject.ActivityClass.SubjectActivity
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ActivitySectionBinding

class SectionActivity : BaseActivity<ActivitySectionBinding>() {

    private lateinit var sectionAdapter: SectionAdapter

    override fun getViewBinding(): ActivitySectionBinding {
        return ActivitySectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        window.statusBarColor = resources.getColor(R.color.PrimaryColor, theme)

        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen",
            "android"
        )

        if (resourceId > 0) {
            binding.statusBarBackground.layoutParams.height =
                resources.getDimensionPixelSize(resourceId)
            binding.statusBarBackground.requestLayout()
        }

        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.lblStepInfo.text = "Step 2 of 5"
        binding.lblSubtitle.text =
            "Standard ${Constant.isSelectedStandardName} — select one or more"

        setupStepIndicator()
        loadSections()
        setupButtons()
    }

    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 2)
    }

    private fun loadSections() {

        val sectionsForStandard: List<StandardSection> =
            (Constant.isAllStandardSections ?: emptyList())
                .filter { it.standardId == Constant.isSelectedStandardId }

        sectionAdapter = SectionAdapter(sectionsForStandard) { selectedCount ->
            updateSelectionBadge(selectedCount)
        }

        binding.rcSectionList.layoutManager = LinearLayoutManager(this)
        binding.rcSectionList.adapter = sectionAdapter
    }

    private fun updateSelectionBadge(count: Int) {
        if (count == 0) {
            binding.lblSelectionCount.visibility = View.GONE
        } else {
            binding.lblSelectionCount.text =
                "$count section${if (count > 1) "s" else ""} selected"
            binding.lblSelectionCount.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnContinue.setOnClickListener {
            val selected = sectionAdapter.getSelectedSections()
            if (selected.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please select at least one section",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            Constant.isSelectedSections = selected
            val sectionIds = selected.joinToString(",") { it.sectionId }
            val intent = Intent(this, SubjectActivity::class.java)
            intent.putExtra("SECTION_IDS",sectionIds)
            startActivity(intent)
        }
    }
}