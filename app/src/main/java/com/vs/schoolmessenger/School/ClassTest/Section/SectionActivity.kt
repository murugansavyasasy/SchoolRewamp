package com.vs.schoolmessenger.School.ClassTest.Section

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Report.ExamReportActivity
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.School.ClassTest.Subject.ActivityClass.SubjectActivity
import com.vs.schoolmessenger.School.ExamReview.Activity.ExamStandardActivity
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ActivitySectionBinding

class SectionActivity : BaseActivity<ActivitySectionBinding>(), View.OnClickListener  {

    private lateinit var sectionAdapter: SectionAdapter

    override fun getViewBinding(): ActivitySectionBinding {
        return ActivitySectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.statusBarBackground.layoutParams.height = statusBarHeight
            binding.statusBarBackground.requestLayout()

            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val baseBottomMargin = resources.getDimensionPixelSize(R.dimen.twenty)
            val lytContentParams =
                binding.lytContent.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            lytContentParams.bottomMargin = baseBottomMargin + navBarHeight
            binding.lytContent.layoutParams = lytContentParams

            insets
        }
        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.viewreporttext.setOnClickListener(this)
        binding.viewmarkanalysis.setOnClickListener(this)
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

        updateSelectionBadge(0)
    }

    private fun updateSelectionBadge(count: Int) {
        binding.lblSelectionCount.visibility = View.VISIBLE
        binding.lblSelectionCount.text = when (count) {
            0 -> "0 sections selected"
            1 -> "1 section selected"
            else -> "$count sections selected"
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


    private fun RedirectToReport() {
        val intent = Intent(this, ExamReportActivity::class.java)
        startActivity(intent)
    }

    private fun RedirectToAnalysis() {
        val intent = Intent(this, ExamStandardActivity::class.java)
        startActivity(intent)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.viewreporttext-> RedirectToReport()
            R.id.viewmarkanalysis-> RedirectToAnalysis()

        }
    }
}