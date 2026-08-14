package com.vs.schoolmessenger.School.ClassTest.Subject.ActivityClass

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ClassTest.Class.ClassActivity.ClassActivity
import com.vs.schoolmessenger.School.ClassTest.Class.Models.SelectedSubject
import com.vs.schoolmessenger.School.ClassTest.Report.ExamReportActivity
import com.vs.schoolmessenger.School.ClassTest.Standard.StandardAdapter
import com.vs.schoolmessenger.School.ClassTest.StepIndicatorHelper
import com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass.SectionDataDetail
import com.vs.schoolmessenger.School.ExamReview.Activity.ExamStandardActivity
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SelectStandardCreateBinding
import com.vs.schoolmessenger.databinding.SubjectListBinding

class SubjectActivity : BaseActivity<SubjectListBinding>(), View.OnClickListener {
    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: SubjectAdapter

    private var sectionIds: String = ""
    private var totalSelectedCount = 0

    override fun getViewBinding(): SubjectListBinding {
        return SubjectListBinding.inflate(layoutInflater)
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
        sectionIds = intent.getStringExtra("SECTION_IDS") ?: ""
        binding.imgBack.setOnClickListener(this)
        binding.viewreporttext.setOnClickListener(this)
        binding.viewmarkanalysis.setOnClickListener(this)
        setupStepIndicator()
        setupViewModel()
        setupContinueButton()
    }


    private fun setupStepIndicator() {
        StepIndicatorHelper.setStep(binding.stepIndicator.root, currentStep = 3)
    }

    private fun setupViewModel() {
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken  = isStaffDetails!!.access_token

        isGetSectionWiseSubjects()


        appViewModel!!.isSectionwisesubjectsdetail?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    buildSectionList(response.data)
                    showData()
                } else {
                    showError(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        }
    }

    private fun isGetSectionWiseSubjects() {
        appViewModel!!.isSectionwisesubjectsdetail(isAccessToken!!, sectionIds,this)
    }


    private fun setupContinueButton() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinue.setOnClickListener {
            if (totalSelectedCount == 0) {
                Toast.makeText(this, "Please select atleast one subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val allSelected: List<SelectedSubject> = adapter.getAllSelectedSubjectsWithSection()
                .flatMap { section ->
                    section.subjects.map { subject ->
                        SelectedSubject(
                            subjectId   = subject.id,
                            subjectName = subject.name,
                            sectionId   = section.section_id,
                            sectionName = section.section_name
                        )
                    }
                }

            Constant.isSelectedSubjectss = allSelected
            startActivity(Intent(this, ClassActivity::class.java))
        }

    }


    private fun buildSectionList(data: List<SectionDataDetail>) {
        showStandardCards(data)
    }


    private fun showStandardCards(data: List<SectionDataDetail>) {
        adapter = SubjectAdapter(data) { selectedCount ->
            totalSelectedCount = selectedCount
            updateSelectionBadge(selectedCount)
        }
        binding.rcSubjectList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SubjectActivity.adapter
        }
    }


    private fun updateSelectionBadge(count: Int) {
        if (count == 0) {
            binding.lblSelectionCount.visibility = View.GONE
        } else {
            val totalSubjects = adapter.getTotalSubjectCount()
            binding.lblSelectionCount.text = "✓ $count of $totalSubjects subjects selected"
            binding.lblSelectionCount.visibility = View.VISIBLE
        }
    }
    private fun showData() {
        binding.rcSubjectList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.rcSubjectList.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
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
            R.id.imgBack -> onBackPressedDispatcher.onBackPressed()
            R.id.viewreporttext-> RedirectToReport()
            R.id.viewmarkanalysis-> RedirectToAnalysis()
        }
    }
}